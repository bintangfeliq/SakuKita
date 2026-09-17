package com.example.SakuKita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import com.example.SakuKita.model.Kategori;
import com.example.SakuKita.model.Saldo;
import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;
import com.example.SakuKita.service.SaldoService;
import com.example.SakuKita.service.TransaksiService;
import com.example.SakuKita.service.UserService;

@SpringBootTest
@Transactional
class EndToEndAuditTest {

    @Autowired
    private UserService userService;

    @Autowired
    private SaldoService saldoService;

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private KategoriService kategoriService;

    @Test
    void testAlurUserBaru_RegisterLogin_SaldoPemasukanPengeluaranNol() {
        String email = "audit_baru_" + System.currentTimeMillis() + "@test.com";
        User userBaru = userService.tambahUser(new User(null, "Audit User Baru", email, "password123"));

        assertNotNull(userBaru.getId());
        assertTrue(userBaru.getPassword().startsWith("$2a$") || userBaru.getPassword().startsWith("$2b$"));

        User loggedIn = userService.login(email, "password123");
        assertNotNull(loggedIn);
        assertEquals("Audit User Baru", loggedIn.getName());

        Saldo saldo = saldoService.cariSaldoUser(loggedIn).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(saldo.getSaldo()));

        BigDecimal pemasukan = transaksiService.pemasukanBulanan(loggedIn);
        BigDecimal pengeluaran = transaksiService.pengeluaranBulanan(loggedIn);
        assertEquals(0, BigDecimal.ZERO.compareTo(pemasukan));
        assertEquals(0, BigDecimal.ZERO.compareTo(pengeluaran));

        List<Transaksi> transaksiList = transaksiService.cariTransaksiUser(loggedIn);
        assertTrue(transaksiList.isEmpty());

        List<Kategori> kategoriList = kategoriService.cariKategoriUser(loggedIn);
        assertTrue(kategoriList.isEmpty());

        Model model = new ConcurrentModel();
        transaksiService.diagramUang(loggedIn, model);
        kategoriService.muatAlokasiKategori(loggedIn, transaksiList, model);

        assertNotNull(model.getAttribute("arusKasBulan"));
        assertNotNull(model.getAttribute("alokasiKategori"));
    }

    @Test
    void testTransaksi_PemasukanDanPengeluaran_PerhitunganSaldo() {
        String email = "audit_tx_" + System.currentTimeMillis() + "@test.com";
        User user = userService.tambahUser(new User(null, "User Transaksi", email, "pass123"));

        transaksiService.pemasukan(user, new BigDecimal("1000000"), "Gaji", "Pekerjaan");
        Saldo saldoSetelahMasuk = saldoService.cariSaldoUser(user).orElseThrow();
        assertEquals(0, new BigDecimal("1000000").compareTo(saldoSetelahMasuk.getSaldo()));

        transaksiService.pengeluaran(user, new BigDecimal("400000"), "Belanja", "Kebutuhan");
        Saldo saldoSetelahKeluar = saldoService.cariSaldoUser(user).orElseThrow();
        assertEquals(0, new BigDecimal("600000").compareTo(saldoSetelahKeluar.getSaldo()));

        assertThrows(RuntimeException.class, () -> {
            transaksiService.pengeluaran(user, new BigDecimal("9999999"), "Beli Helikopter", "Mewah");
        });

        Saldo saldoTetap = saldoService.cariSaldoUser(user).orElseThrow();
        assertEquals(0, new BigDecimal("600000").compareTo(saldoTetap.getSaldo()));
    }

    @Test
    void testIsolasiData_AntarUserTidakTercampur() {
        String emailA = "user_a_" + System.currentTimeMillis() + "@test.com";
        String emailB = "user_b_" + System.currentTimeMillis() + "@test.com";

        User userA = userService.tambahUser(new User(null, "User A", emailA, "passA123"));
        User userB = userService.tambahUser(new User(null, "User B", emailB, "passB123"));

        Kategori katA = kategoriService.tambahKategori(userA, "Bisnis A");
        transaksiService.pemasukan(userA, new BigDecimal("750000"), "Pendapatan A", "Bisnis A");

        List<Kategori> katB = kategoriService.cariKategoriUser(userB);
        assertTrue(katB.isEmpty());

        List<Transaksi> txB = transaksiService.cariTransaksiUser(userB);
        assertTrue(txB.isEmpty());

        Saldo saldoB = saldoService.cariSaldoUser(userB).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(saldoB.getSaldo()));

        assertThrows(RuntimeException.class, () -> {
            kategoriService.hapusKategori(userB, katA.getId());
        });

        assertThrows(RuntimeException.class, () -> {
            kategoriService.updateKategori(userB, katA.getId(), "Dibajak B");
        });
    }

    @Test
    void testKategoriCRUD_SesuaiUser() {
        String email = "kat_crud_" + System.currentTimeMillis() + "@test.com";
        User user = userService.tambahUser(new User(null, "User Kategori", email, "pass123"));

        Kategori kat = kategoriService.tambahKategori(user, "Investasi");
        assertNotNull(kat.getId());
        assertEquals("Investasi", kat.getNama());

        assertThrows(RuntimeException.class, () -> {
            kategoriService.tambahKategori(user, "Investasi");
        });

        Kategori updated = kategoriService.updateKategori(user, kat.getId(), "Saham & Reksdana");
        assertEquals("Saham & Reksdana", updated.getNama());

        kategoriService.hapusKategori(user, kat.getId());
        List<Kategori> sisa = kategoriService.cariKategoriUser(user);
        assertTrue(sisa.isEmpty());
    }
}

