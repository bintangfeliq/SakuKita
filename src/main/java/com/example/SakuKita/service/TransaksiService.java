package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.TransaksiRepository;

@Service
@Transactional
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final SaldoService saldoService;

    public TransaksiService(TransaksiRepository transaksiRepository, SaldoService saldoService) {
        this.transaksiRepository = transaksiRepository;
        this.saldoService = saldoService;
    }

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

    public Transaksi pemasukan( User user, BigDecimal jumlah, String keterangan, String kategori) {
        saldoService.tambahSaldo(user, jumlah);
        return simpan( user, jumlah, keterangan, kategori, "PEMASUKAN");
    }

    public Transaksi pengeluaran(User user, BigDecimal jumlah, String keterangan, String kategori) {
        saldoService.kurangiSaldo(user, jumlah);
        return simpan( user, jumlah, keterangan, kategori, "PENGELUARAN");
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

    public List<Transaksi> filterTransaksi(List<Transaksi> transaksi, String periode) {
        if (transaksi == null || transaksi.isEmpty()) {
            return List.of();
        }
        if ("semua".equalsIgnoreCase(periode)) {
            return transaksi;
        }
        LocalDateTime sekarang = LocalDateTime.now();
         return transaksi.stream()
            .filter(t -> t.getTanggal() != null)
            .filter(t -> t.getTanggal().getYear() == sekarang.getYear())
            .filter(t -> "tahun".equalsIgnoreCase(periode) || t.getTanggal().getMonth() == sekarang.getMonth()).toList();
    }

    public BigDecimal totalNominal(List<Transaksi> transaksi) {
        if (transaksi == null || transaksi.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return transaksi.stream().map(t -> t.getJumlah()).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalPemasukan(User user, String periode) {
        List<Transaksi> transaksi = cariTransaksiUser(user).stream().filter(t -> "PEMASUKAN".equalsIgnoreCase(t.getJenis())).toList();
        return totalNominal(filterTransaksi(transaksi, periode));
    }

    public BigDecimal totalPengeluaran(User user, String periode) {
        List<Transaksi> transaksi = cariTransaksiUser(user).stream().filter(t -> "PENGELUARAN".equalsIgnoreCase(t.getJenis())).toList();
        return totalNominal(filterTransaksi(transaksi, periode));
    }

    public BigDecimal pemasukanBulanan(User user) {
        return totalPemasukan(user, "bulan");
    }

    public BigDecimal pengeluaranBulanan(User user) {
        return totalPengeluaran(user, "bulan");
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
        for (int i = 5; i >= 0; i--) {
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