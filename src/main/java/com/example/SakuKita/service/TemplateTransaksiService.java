package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.SakuKita.model.TemplateTransaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.TemplateTransaksiRepository;

@Service
public class TemplateTransaksiService {

    private final TemplateTransaksiRepository repository;

    public TemplateTransaksiService(TemplateTransaksiRepository repository) {
        this.repository = repository;
    }

    public List<TemplateTransaksi> semuaTemplate(User user) {
        return user == null ? List.of() : repository.findByUserOrderByIdDesc(user);
    }

    public List<TemplateTransaksi> cariBerdasarkanJenis(User user, String jenis) {
        return user == null || jenis == null ? List.of() : repository.findByUserAndJenisOrderByIdDesc(user, jenis.toUpperCase());
    }

    public Optional<TemplateTransaksi> cariBerdasarkanId(Long id, User user) {
        return id == null || user == null ? Optional.empty() : repository.findByIdAndUser(id, user);
    }

    public TemplateTransaksi simpanTemplate(TemplateTransaksi template, User user) {
        validasi(template, user);
        template.setNama(template.getNama().trim());
        template.setJenis(template.getJenis().toUpperCase());
        template.setUser(user);
        return repository.save(template);
    }

    public void hapusTemplate(Long id, User user) {
        if (id == null || user == null) {
            return;
        }

        TemplateTransaksi template = cariBerdasarkanId(id, user).orElseThrow(() -> new RuntimeException("Template tidak ditemukan"));
        repository.delete(template);
    }

    public void hapusTemplate(TemplateTransaksi template) {
        if (template != null) {
            repository.delete(template);
        }
    }

    public TemplateTransaksi updateTemplate(Long id, TemplateTransaksi data, User user) {
        if (id == null || user == null) {
            throw new RuntimeException("Data tidak valid");
        }

        TemplateTransaksi template = cariBerdasarkanId(id, user).orElseThrow(() -> new RuntimeException("Template tidak ditemukan"));
        validasi(data, user);
        template.setNama(data.getNama().trim());
        template.setJumlah(data.getJumlah());
        template.setKategori(data.getKategori());
        template.setKeterangan(data.getKeterangan());
        return repository.save(template);
    }

    private void validasi(TemplateTransaksi template, User user) {
        if (user == null) {
            throw new RuntimeException("Sesi telah berakhir, silakan login kembali");
        }

        if (template.getNama() == null || template.getNama().trim().isEmpty()) {
            throw new RuntimeException("Nama template wajib diisi");
        }

        if (template.getJumlah() == null || template.getJumlah().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Jumlah nominal harus lebih dari 0");
        }

        if (template.getJenis() == null || (!template.getJenis().equalsIgnoreCase("PEMASUKAN") && !template.getJenis().equalsIgnoreCase("PENGELUARAN"))) {
            throw new RuntimeException("Jenis transaksi template tidak valid");
        }

        if (template.getKategori() != null && template.getKategori().getUser() != null && !template.getKategori().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Kategori tidak valid");
        }
    }
}
