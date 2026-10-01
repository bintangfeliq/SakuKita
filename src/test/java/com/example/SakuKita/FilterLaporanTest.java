package com.example.SakuKita;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.TransaksiRepository;
import com.example.SakuKita.service.TransaksiService;
import com.example.SakuKita.service.UserService;

@SpringBootTest
@Transactional
class FilterLaporanTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private TransaksiRepository transaksiRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testFilterRentang_TransaksiAwalSampaiBulanTahunPilihan() {
        User user = userService.tambahUser(new User(null, "User Filter", "filter_rentang_" + System.currentTimeMillis() + "@test.com", "pass123"));

        Transaksi t1 = new Transaksi(null, "PEMASUKAN", new BigDecimal("100000"), "Gaji Awal", LocalDateTime.of(2024, 1, 15, 10, 0), "Gaji", user);
        Transaksi t2 = new Transaksi(null, "PEMASUKAN", new BigDecimal("200000"), "Bonus Tengah", LocalDateTime.of(2025, 5, 10, 11, 0), "Bonus", user);
        Transaksi t3 = new Transaksi(null, "PEMASUKAN", new BigDecimal("300000"), "Proyek Depan", LocalDateTime.of(2026, 7, 20, 14, 0), "Proyek", user);
        transaksiRepository.saveAll(List.of(t1, t2, t3));

        List<Transaksi> semua = transaksiService.cariTransaksiUser(user);

        List<Transaksi> hasil = transaksiService.filterTransaksiBulan(semua, 1, 2024);
        assertEquals(1, hasil.size());

        BigDecimal total = transaksiService.totalNominal(hasil);
        assertEquals(new BigDecimal("100000"), total);

        String periode = transaksiService.buatTeksPeriode(1, 2024);
        assertEquals("Januari 2024", periode);

        List<Integer> daftarTahun = transaksiService.buatDaftarTahun(semua, 2026);
        assertEquals(5, daftarTahun.size());
        assertTrue(daftarTahun.contains(2026));
    }

    @Test
    void testFilterRentang_PilihanSebelumTransaksiAwal_HasilKosong() {
        User user = userService.tambahUser(new User(null, "User Sebelum Awal", "sebelum_awal_" + System.currentTimeMillis() + "@test.com", "pass123"));

        Transaksi t1 = new Transaksi(null, "PEMASUKAN", new BigDecimal("500000"), "Gaji Maret", LocalDateTime.of(2025, 3, 1, 9, 0), "Gaji", user);
        transaksiRepository.save(t1);

        List<Transaksi> semua = transaksiService.cariTransaksiUser(user);

        List<Transaksi> hasil = transaksiService.filterTransaksiBulan(semua, 1, 2025);
        assertTrue(hasil.isEmpty());

        String periode = transaksiService.buatTeksPeriode(1, 2025);
        assertEquals("Januari 2025", periode);
    }

    @Test
    void testFilterRentang_UserTanpaTransaksi_AmanDanTotalNol() {
        User user = userService.tambahUser(new User(null, "User Baru Nol", "user_nol_" + System.currentTimeMillis() + "@test.com", "pass123"));
        List<Transaksi> semua = transaksiService.cariTransaksiUser(user);

        List<Transaksi> hasil = transaksiService.filterTransaksiBulan(semua, 4, 2026);
        assertTrue(hasil.isEmpty());

        BigDecimal total = transaksiService.totalNominal(hasil);
        assertEquals(BigDecimal.ZERO, total);

        String periode = transaksiService.buatTeksPeriode(4, 2026);
        assertEquals("April 2026", periode);

        List<Integer> daftarTahun = transaksiService.buatDaftarTahun(semua, 2026);
        assertFalse(daftarTahun.isEmpty());
        assertEquals(5, daftarTahun.size());
    }

    @Test
    void testWebController_FilterBulanDanTahun_RenderSukses() throws Exception {
        User user = userService.tambahUser(new User(null, "User Web Filter", "web_filter_" + System.currentTimeMillis() + "@test.com", "pass123"));

        Transaksi tMasuk = new Transaksi(null, "PEMASUKAN", new BigDecimal("1500000"), "Pemasukan Uji", LocalDateTime.of(2024, 2, 10, 10, 0), "Bisnis", user);
        Transaksi tKeluar = new Transaksi(null, "PENGELUARAN", new BigDecimal("500000"), "Pengeluaran Uji", LocalDateTime.of(2024, 3, 15, 11, 0), "Operasional", user);
        transaksiRepository.saveAll(List.of(tMasuk, tKeluar));

        mockMvc.perform(get("/pemasukan")
                .sessionAttr("user", user)
                .param("bulan", "2")
                .param("tahun", "2024"))
                .andExpect(status().isOk())
                .andExpect(view().name("pemasukan"))
                .andExpect(model().attribute("bulan", 2))
                .andExpect(model().attribute("tahun", 2024))
                .andExpect(model().attributeExists("daftarTahun", "periodeJudul"))
                .andExpect(content().string(containsString("Riwayat Pemasukan")))
                .andExpect(content().string(containsString("Pemasukan Uji")))
                .andExpect(content().string(containsString("1.500.000")));

        mockMvc.perform(get("/pengeluaran")
                .sessionAttr("user", user)
                .param("bulan", "3")
                .param("tahun", "2024"))
                .andExpect(status().isOk())
                .andExpect(view().name("pengeluaran"))
                .andExpect(model().attribute("bulan", 3))
                .andExpect(model().attribute("tahun", 2024))
                .andExpect(model().attributeExists("daftarTahun", "periodeJudul"))
                .andExpect(content().string(containsString("Riwayat Pengeluaran")))
                .andExpect(content().string(containsString("Pengeluaran Uji")))
                .andExpect(content().string(containsString("500.000")));

        mockMvc.perform(get("/laporan")
                .sessionAttr("user", user)
                .param("bulan", "2")
                .param("tahun", "2024"))
                .andExpect(status().isOk())
                .andExpect(view().name("laporan"))
                .andExpect(model().attribute("bulan", 2))
                .andExpect(model().attribute("tahun", 2024))
                .andExpect(model().attributeExists("daftarTahun", "periodeJudul"))
                .andExpect(content().string(containsString("Laporan Keuangan")))
                .andExpect(content().string(containsString("1.500.000")));
    }
}
