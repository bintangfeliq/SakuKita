package com.example.SakuKita;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;
import com.example.SakuKita.service.TransaksiService;
import com.example.SakuKita.service.UserService;

@SpringBootTest
@Transactional
class WebPageAuditTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private KategoriService kategoriService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testHalamanPublik_LoginRegisterHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void testProteksiHalamanTanpaLogin_RedirectKeLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/pemasukan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/pengeluaran"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/kategori"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/laporan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testUserBaru_SemuaHalamanWebRenderSuksesTanpaError() throws Exception {
        String email = "web_baru_" + System.currentTimeMillis() + "@test.com";
        User user = userService.tambahUser(new User(null, "User Baru Web", email, "secret123"));

        mockMvc.perform(get("/dashboard").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("User Baru Web")))
                .andExpect(content().string(containsString("Rp 0")))
                .andExpect(content().string(containsString("Belum ada aktivitas transaksi")));

        mockMvc.perform(get("/pemasukan").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("pemasukan"))
                .andExpect(content().string(containsString("Rp 0")))
                .andExpect(content().string(containsString("Belum Ada Data Pemasukan")));

        mockMvc.perform(get("/pengeluaran").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("pengeluaran"))
                .andExpect(content().string(containsString("Rp 0")))
                .andExpect(content().string(containsString("Belum Ada Data Pengeluaran")));

        mockMvc.perform(get("/kategori").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("kategori"))
                .andExpect(content().string(containsString("Belum Ada Kategori")));

        mockMvc.perform(get("/laporan").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("laporan"))
                .andExpect(content().string(containsString("Rp 0")))
                .andExpect(content().string(containsString("Belum ada kategori yang tersedia")));

        mockMvc.perform(get("/tambahPemasukan").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("tambahPemasukan"));

        mockMvc.perform(get("/tambahPengeluaran").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("tambahPengeluaran"));
    }

    @Test
    void testUserLamaDenganData_SemuaHalamanWebRenderDataLengkap() throws Exception {
        String email = "web_lama_" + System.currentTimeMillis() + "@test.com";
        User user = userService.tambahUser(new User(null, "User Lama Web", email, "secret123"));

        kategoriService.tambahKategori(user, "Gaji Bulanan");
        kategoriService.tambahKategori(user, "Konsumsi Makanan");

        transaksiService.pemasukan(user, new BigDecimal("5000000"), "Transfer Gaji", "Gaji Bulanan");
        transaksiService.pengeluaran(user, new BigDecimal("1500000"), "Makan Siang", "Konsumsi Makanan");

        mockMvc.perform(get("/dashboard").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("3.500.000")))
                .andExpect(content().string(containsString("Transfer Gaji")))
                .andExpect(content().string(containsString("Makan Siang")));

        mockMvc.perform(get("/pemasukan").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("pemasukan"))
                .andExpect(content().string(containsString("5.000.000")))
                .andExpect(content().string(containsString("Transfer Gaji")));

        mockMvc.perform(get("/pengeluaran").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("pengeluaran"))
                .andExpect(content().string(containsString("1.500.000")))
                .andExpect(content().string(containsString("Makan Siang")));

        mockMvc.perform(get("/kategori").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("kategori"))
                .andExpect(content().string(containsString("Gaji Bulanan")))
                .andExpect(content().string(containsString("Konsumsi Makanan")));

        mockMvc.perform(get("/laporan").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name("laporan"))
                .andExpect(content().string(containsString("5.000.000")))
                .andExpect(content().string(containsString("1.500.000")));
    }

    @Test
    void testIsolasiDataWeb_UserATidakMelihatDataUserB() throws Exception {
        String emailA = "web_a_" + System.currentTimeMillis() + "@test.com";
        String emailB = "web_b_" + System.currentTimeMillis() + "@test.com";
        User userA = userService.tambahUser(new User(null, "User A Eksklusif", emailA, "secretA123"));
        User userB = userService.tambahUser(new User(null, "User B Eksklusif", emailB, "secretB123"));

        kategoriService.tambahKategori(userA, "Kategori Rahasia A");
        transaksiService.pemasukan(userA, new BigDecimal("8888000"), "Setoran Rahasia A", "Kategori Rahasia A");

        kategoriService.tambahKategori(userB, "Kategori Publik B");
        transaksiService.pemasukan(userB, new BigDecimal("1111000"), "Setoran Publik B", "Kategori Publik B");

        mockMvc.perform(get("/pemasukan").sessionAttr("user", userB))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Setoran Publik B")))
                .andExpect(content().string(not(containsString("Setoran Rahasia A"))));

        mockMvc.perform(get("/kategori").sessionAttr("user", userB))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kategori Publik B")))
                .andExpect(content().string(not(containsString("Kategori Rahasia A"))));

        mockMvc.perform(get("/pemasukan").sessionAttr("user", userA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Setoran Rahasia A")))
                .andExpect(content().string(not(containsString("Setoran Publik B"))));
    }

    @Test
    void testKondisiFilterPeriodeLaporanDanTransaksi() throws Exception {
        String email = "filter_" + System.currentTimeMillis() + "@test.com";
        User user = userService.tambahUser(new User(null, "User Filter", email, "secret123"));

        mockMvc.perform(get("/laporan").sessionAttr("user", user).param("periode", "bulan"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/laporan").sessionAttr("user", user).param("periode", "tahun"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/laporan").sessionAttr("user", user).param("periode", "semua"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pemasukan").sessionAttr("user", user).param("periode", "tahun"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pengeluaran").sessionAttr("user", user).param("periode", "semua"))
                .andExpect(status().isOk());
    }
}

