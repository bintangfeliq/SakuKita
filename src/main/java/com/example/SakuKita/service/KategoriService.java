package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.example.SakuKita.model.Kategori;
import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.KategoriRepository;

@Service
@Transactional
public class KategoriService {

    private final KategoriRepository kategoriRepository;

    public KategoriService(KategoriRepository kategoriRepository) {
        this.kategoriRepository = kategoriRepository;
    }

    public List<Kategori> cariKategoriUser(User user) {
        if (user == null) {
            return List.of();
        }
        return kategoriRepository.findByUserOrderByIdDesc(user);
    }

    public Kategori tambahKategori(User user, String nama) {
        String namaBersih = nama.trim();
        if (kategoriRepository.existsByUserIdAndNamaIgnoreCase(user.getId(), namaBersih)) {
            throw new RuntimeException("Kategori '" + namaBersih + "' sudah ada");
        }
        Kategori kategori = new Kategori();
        kategori.setNama(namaBersih);
        kategori.setUser(user);
        return kategoriRepository.save(kategori);
    }

    public Kategori updateKategori(User user, Long id, String namaBaru) {
        Kategori kategori = kategoriRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        kategori.setNama(namaBaru.trim());
        return kategoriRepository.save(kategori);
    }

    public void hapusKategori(User user, Long id) {
        Kategori kategori = kategoriRepository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        kategoriRepository.delete(kategori);
    }

    public void muatAlokasiKategori(User user, List<Transaksi> transaksi, Model model) {
        List<Kategori> kategoriUser = cariKategoriUser(user);
        List<String> warna = List.of(
            "#0F3D32",
            "#25A18E",
            "#34D399",
            "#F59E0B",
            "#6366F1",
            "#EC4899",
            "#8B5CF6",
            "#14B8A6",
            "#F97316",
            "#64748B"
        );

        List<Map<String, Object>> alokasi = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartSeries = new ArrayList<>();
        List<String> chartColors = new ArrayList<>();
        int index = 0;
        for (Kategori kategori : kategoriUser) {
            BigDecimal total = BigDecimal.ZERO;
            if (transaksi != null) {
                total = transaksi.stream().filter(t -> t.getJumlah() != null && kategori.getNama() != null && kategori.getNama().equalsIgnoreCase(t.getKategori())).map(Transaksi::getJumlah).reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            String warnaKategori = warna.get(index % warna.size());
            Map<String, Object> data = new HashMap<>();
            data.put("nama", kategori.getNama());
            data.put("total", total);
            data.put("warna", warnaKategori);
            alokasi.add(data);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                chartLabels.add(kategori.getNama());
                chartSeries.add(total);
                chartColors.add(warnaKategori);
            }
            index++;
        }
        model.addAttribute("alokasiKategori", alokasi);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartSeries", chartSeries);
        model.addAttribute("chartColors", chartColors);
    }
}
