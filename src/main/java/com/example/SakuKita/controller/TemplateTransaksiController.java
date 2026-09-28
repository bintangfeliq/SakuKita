package com.example.SakuKita.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.SakuKita.model.Kategori;
import com.example.SakuKita.model.TemplateTransaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;
import com.example.SakuKita.service.TemplateTransaksiService;
import com.example.SakuKita.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TemplateTransaksiController {

    private final TemplateTransaksiService templateTransaksiService;
    private final KategoriService kategoriService;
    private final TransaksiService transaksiService;

    public TemplateTransaksiController(TemplateTransaksiService templateTransaksiService, KategoriService kategoriService, TransaksiService transaksiService) {
        this.templateTransaksiService = templateTransaksiService;
        this.kategoriService = kategoriService;
        this.transaksiService = transaksiService;
    }

    @GetMapping({"/templateTransaksi", "/template-transaksi"})
    public String halamanTemplate(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("templatePemasukan", templateTransaksiService.cariBerdasarkanJenis(user, "PEMASUKAN"));
        model.addAttribute("templatePengeluaran", templateTransaksiService.cariBerdasarkanJenis(user, "PENGELUARAN"));
        model.addAttribute("kategoriList", kategoriService.cariKategoriUser(user));
        return "TemplateTransaksi";
    }

    @PostMapping({"/templateTransaksi/simpan", "/template-transaksi/simpan"})
    public String simpanTemplate(@RequestParam String nama, @RequestParam String jenis, @RequestParam BigDecimal jumlah, @RequestParam(required = false) String keterangan, @RequestParam(required = false) Long kategoriId, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            TemplateTransaksi template = new TemplateTransaksi();
            template.setNama(nama);
            template.setJenis(jenis);
            template.setJumlah(jumlah);
            template.setKeterangan(keterangan);
            template.setKategori(cariKategori(user, kategoriId));
            templateTransaksiService.simpanTemplate(template, user);
            String labelJenis = "PEMASUKAN".equalsIgnoreCase(jenis) ? "pemasukan" : "pengeluaran";
            redirect.addFlashAttribute("pesanSukses", "Template " + labelJenis + " berhasil disimpan!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/templateTransaksi";
    }

    @PostMapping({"/templateTransaksi/hapus/{id}", "/template-transaksi/hapus/{id}"})
    public String hapusTemplate( @PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            templateTransaksiService.hapusTemplate(id, user);
            redirect.addFlashAttribute("pesanSukses", "Template berhasil dihapus!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/templateTransaksi";
    }

    @PostMapping({"/templateTransaksi/gunakan/{id}", "/template-transaksi/gunakan/{id}"})
    public String gunakanTemplate(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            TemplateTransaksi template = templateTransaksiService.cariBerdasarkanId(id, user).orElseThrow(() -> new RuntimeException("Template tidak ditemukan"));
            String kategori = template.getKategori() != null ? template.getKategori().getNama() : "Lainnya";
            String keterangan = template.getKeterangan() != null && !template.getKeterangan().trim().isEmpty() ? template.getKeterangan() : template.getNama();
            if ("PEMASUKAN".equalsIgnoreCase(template.getJenis())) {
                transaksiService.pemasukan( user, template.getJumlah(), keterangan, kategori);
                redirect.addFlashAttribute("pesanPemasukan", "Pemasukan berhasil dicatat dari template: " + template.getNama());
                return "redirect:/pemasukan";
            }
            transaksiService.pengeluaran(user, template.getJumlah(), keterangan, kategori);
            redirect.addFlashAttribute("pesanPengeluaran", "Pengeluaran berhasil dicatat dari template: " + template.getNama());
            return "redirect:/pengeluaran";

        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
            return "redirect:/templateTransaksi";
        }
    }

    @GetMapping({"/templateTransaksi/gunakan/{id}", "/template-transaksi/gunakan/{id}"})
    public String gunakanTemplateGet(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        return gunakanTemplate(id, session, redirect);
    }

    @PostMapping({"/templateTransaksi/update/{id}", "/template-transaksi/update/{id}"})
    public String updateTemplate(@PathVariable Long id, @RequestParam String nama, @RequestParam BigDecimal jumlah, @RequestParam(required = false) String keterangan, @RequestParam(required = false) Long kategoriId, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            TemplateTransaksi templateBaru = new TemplateTransaksi();
            templateBaru.setNama(nama);
            templateBaru.setJumlah(jumlah);
            templateBaru.setKeterangan(keterangan);
            templateBaru.setKategori(cariKategori(user, kategoriId));
            templateTransaksiService.updateTemplate(id, templateBaru, user);
            redirect.addFlashAttribute("pesanSukses", "Template berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/templateTransaksi";
    }
    private Kategori cariKategori(User user, Long kategoriId) {
        if (kategoriId == null) {
            return null;
        }
        List<Kategori> kategoriUser = kategoriService.cariKategoriUser(user);
        for (Kategori kategori : kategoriUser) {
            if (kategori.getId().equals(kategoriId)) {
                return kategori;
            }
        }
        return null;
    }
}