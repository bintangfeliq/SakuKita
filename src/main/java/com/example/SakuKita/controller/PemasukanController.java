package com.example.SakuKita.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;
import com.example.SakuKita.service.TemplateTransaksiService;
import com.example.SakuKita.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PemasukanController {

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private KategoriService kategoriService;

    @Autowired
    private TemplateTransaksiService templateTransaksiService;

    @GetMapping("/pemasukan")
    public String halamanPemasukan(@RequestParam(required = false) Integer bulan, @RequestParam(required = false) Integer tahun, @RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        LocalDate hariIni = LocalDate.now();
        int bulanPilih = (bulan != null && bulan >= 1 && bulan <= 12) ? bulan : hariIni.getMonthValue();
        int tahunPilih = (tahun != null && tahun >= 1900 && tahun <= 2100) ? tahun : hariIni.getYear();
        List<Transaksi> semuaPemasukan = new ArrayList<>();
        for (Transaksi t : transaksiService.cariTransaksiUser(user)) {
            if ("PEMASUKAN".equalsIgnoreCase(t.getJenis())) {
                semuaPemasukan.add(t);
            }
        }

        List<Transaksi> filtered = transaksiService.filterTransaksiBulan(semuaPemasukan, bulanPilih, tahunPilih);
        BigDecimal totalPemasukan = transaksiService.totalNominal(filtered);
        String periodeJudul = transaksiService.buatTeksPeriode(bulanPilih, tahunPilih);
        List<Integer> daftarTahun = transaksiService.buatDaftarTahun();
        int pageSize = 10;
        int totalItems = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        page = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Transaksi> paged = filtered.subList(fromIndex, toIndex);

        model.addAttribute("transaksi", paged);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("fromIndex", totalItems == 0 ? 0 : fromIndex + 1);
        model.addAttribute("toIndex", toIndex);
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        model.addAttribute("totalPemasukan", totalPemasukan);
        model.addAttribute("bulan", bulanPilih);
        model.addAttribute("tahun", tahunPilih);
        model.addAttribute("daftarTahun", daftarTahun);
        model.addAttribute("periodeJudul", periodeJudul);
        model.addAttribute("templatesPemasukan", templateTransaksiService.cariBerdasarkanJenis(user, "PEMASUKAN"));
        return "pemasukan";
    }

    @GetMapping("/tambahPemasukan")
    public String halamanTambahPemasukan(@RequestParam(required = false) Long templateId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        model.addAttribute("templatesPemasukan",templateTransaksiService.cariBerdasarkanJenis(user, "PEMASUKAN"));
        if (templateId != null) {
            templateTransaksiService.cariBerdasarkanId(templateId, user).filter(tpl -> "PEMASUKAN".equalsIgnoreCase(tpl.getJenis())).ifPresent(tpl -> model.addAttribute("templateDipilih", tpl));
        }
        return "tambahPemasukan";
    }

    @PostMapping("/pemasukan/tambah")
    public String tambahPemasukan(@RequestParam BigDecimal jumlah, @RequestParam String keterangan, @RequestParam(required = false, defaultValue = "Lainnya") String kategori, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        transaksiService.pemasukan(user, jumlah, keterangan, kategori);
        redirectAttributes.addFlashAttribute("pesanPemasukan", "Pemasukan berhasil dicatat!");
        return "redirect:/pemasukan";
    }
}