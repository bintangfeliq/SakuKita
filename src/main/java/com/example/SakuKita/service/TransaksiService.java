package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.TransaksiRepository;

@Service
@Transactional
public class TransaksiService {

    @Autowired
    private TransaksiRepository transaksiRepository;

    @Autowired
    private SaldoService saldoService;

    public Transaksi simpan(User user, BigDecimal jumlah, String keterangan, String kategori, String jenis) {
        Transaksi transaksi = new Transaksi();
        transaksi.setUser(user);
        transaksi.setJumlah(jumlah);
        transaksi.setKeterangan(keterangan);
        transaksi.setKategori(kategori == null || kategori.isBlank() ? "Lainnya" : kategori.trim());
        transaksi.setJenis(jenis);
        transaksi.setTanggal(LocalDateTime.now());
        return transaksiRepository.save(transaksi);
    }

    public Transaksi pemasukan(User user, BigDecimal jumlah, String keterangan, String kategori) {
        saldoService.tambahSaldo(user, jumlah);
        return simpan(user, jumlah, keterangan, kategori, "PEMASUKAN");
    }

    public Transaksi pengeluaran(User user, BigDecimal jumlah, String keterangan, String kategori) {
        saldoService.kurangiSaldo(user, jumlah);
        return simpan(user, jumlah, keterangan, kategori, "PENGELUARAN");
    }

    public List<Transaksi> cariTransaksiUser(User user) {
        if (user == null) {
            return List.of();
        }
        return transaksiRepository.findByUserOrderByTanggalDescIdDesc(user);
    }

    public List<Transaksi> cari5TransaksiTerakhir(User user) {
        if (user == null) {
            return List.of();
        }
        return transaksiRepository.findTop5ByUserOrderByTanggalDescIdDesc(user);
    }

    public List<Transaksi> filterTransaksiBulan(List<Transaksi> transaksi, int bulan, int tahun) {
        if (transaksi == null || transaksi.isEmpty()) {
            return List.of();
        }
        List<Transaksi> hasil = new ArrayList<>();
        for (Transaksi t : transaksi) {
            if (t.getTanggal() != null && t.getTanggal().getMonthValue() == bulan && t.getTanggal().getYear() == tahun) {
                hasil.add(t);
            }
        }
        return hasil;
    }

    public List<Transaksi> filterTransaksiRentang(List<Transaksi> transaksi, int bulan, int tahun) {
        return filterTransaksiBulan(transaksi, bulan, tahun);
    }

    public String buatTeksPeriode(int bulan, int tahun) {
        String[] namaBulan = {
            "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        return namaBulan[bulan] + " " + tahun;
    }

    public String buatTeksPeriode(List<Transaksi> transaksi, int bulan, int tahun) {
        return buatTeksPeriode(bulan, tahun);
    }

    public List<Integer> buatDaftarTahun() {
        int tahunSekarang = LocalDate.now().getYear();
        List<Integer> daftarTahun = new ArrayList<>();
        for (int y = tahunSekarang - 4; y <= tahunSekarang; y++) {
            daftarTahun.add(y);
        }
        return daftarTahun;
    }

    public List<Integer> buatDaftarTahun(List<Transaksi> transaksi, int tahunPilihan) {
        List<Integer> daftarTahun = buatDaftarTahun();
        if (tahunPilihan > 0 && !daftarTahun.contains(tahunPilihan)) {
            daftarTahun.add(tahunPilihan);
            daftarTahun.sort(null);
        }
        return daftarTahun;
    }

    public BigDecimal totalNominal(List<Transaksi> transaksi) {
        if (transaksi == null || transaksi.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Transaksi t : transaksi) {
            if (t.getJumlah() != null) {
                total = total.add(t.getJumlah());
            }
        }
        return total;
    }

    public BigDecimal pemasukanBulanan(User user) {
        List<Transaksi> transaksi = cariTransaksiUser(user);
        YearMonth sekarang = YearMonth.now();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaksi t : transaksi) {
            if ("PEMASUKAN".equalsIgnoreCase(t.getJenis()) && t.getTanggal() != null && t.getJumlah() != null) {
                if (YearMonth.from(t.getTanggal()).equals(sekarang)) {
                    total = total.add(t.getJumlah());
                }
            }
        }
        return total;
    }

    public BigDecimal pengeluaranBulanan(User user) {
        List<Transaksi> transaksi = cariTransaksiUser(user);
        YearMonth sekarang = YearMonth.now();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaksi t : transaksi) {
            if ("PENGELUARAN".equalsIgnoreCase(t.getJenis()) && t.getTanggal() != null && t.getJumlah() != null) {
                if (YearMonth.from(t.getTanggal()).equals(sekarang)) {
                    total = total.add(t.getJumlah());
                }
            }
        }
        return total;
    }

    public void diagramUang(User user, Model model) {
        List<Transaksi> transaksi = cariTransaksiUser(user);
        List<String> bulan = new ArrayList<>();
        List<BigDecimal> pemasukan = new ArrayList<>();
        List<BigDecimal> pengeluaran = new ArrayList<>();
        String[] namaBulan = {
                "Jan", "Feb", "Mar", "Apr",
                "Mei", "Jun", "Jul", "Agu",
                "Sep", "Okt", "Nov", "Des"
        };
        YearMonth sekarang = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth bulanSekarang = sekarang.minusMonths(i);
            bulan.add(namaBulan[bulanSekarang.getMonthValue() - 1]);
            BigDecimal totalMasuk = BigDecimal.ZERO;
            BigDecimal totalKeluar = BigDecimal.ZERO;
            for (Transaksi t : transaksi) {
                if (t.getTanggal() == null || t.getJumlah() == null) {
                    continue;
                }
                if (!YearMonth.from(t.getTanggal()).equals(bulanSekarang)) {
                    continue;
                }
                if ("PEMASUKAN".equalsIgnoreCase(t.getJenis())) {
                    totalMasuk = totalMasuk.add(t.getJumlah());
                } else if ("PENGELUARAN".equalsIgnoreCase(t.getJenis())) {
                    totalKeluar = totalKeluar.add(t.getJumlah());
                }
            }

            pemasukan.add(totalMasuk);
            pengeluaran.add(totalKeluar);
        }

        model.addAttribute("arusKasBulan", bulan);
        model.addAttribute("arusKasPemasukan", pemasukan);
        model.addAttribute("arusKasPengeluaran", pengeluaran);
    }
}
