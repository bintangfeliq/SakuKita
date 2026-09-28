package com.example.SakuKita.controller;

import java.math.BigDecimal;
import java.util.List;

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
public class PengeluaranController {

    private final TransaksiService transaksiService;
    private final KategoriService kategoriService;
    private final TemplateTransaksiService templateTransaksiService;

    public PengeluaranController(TransaksiService transaksiService, KategoriService kategoriService, TemplateTransaksiService templateTransaksiService) {
        this.transaksiService = transaksiService;
        this.kategoriService = kategoriService;
        this.templateTransaksiService = templateTransaksiService;
    }

    @GetMapping("/pengeluaran")
    public String halamanPengeluaran(@RequestParam(defaultValue = "bulan") String periode, @RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Transaksi> transaksi = transaksiService.cariTransaksiUser(user).stream().filter(t -> "PENGELUARAN".equalsIgnoreCase(t.getJenis())).toList();
        List<Transaksi> filtered = transaksiService.filterTransaksi(transaksi, periode);
        int pageSize = 10;
        int totalItems = filtered.size();
        int totalPages = Math.max(1,(int) Math.ceil((double) totalItems / pageSize));
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
        model.addAttribute("totalPengeluaran", transaksiService.totalPengeluaran(user, periode));
        model.addAttribute("periode", periode);
        model.addAttribute("templatesPengeluaran",templateTransaksiService.cariBerdasarkanJenis(user, "PENGELUARAN"));
        return "pengeluaran";
    }

    @GetMapping("/tambahPengeluaran")
    public String halamanTambahPengeluaran(@RequestParam(required = false) Long templateId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        model.addAttribute("templatesPengeluaran", templateTransaksiService.cariBerdasarkanJenis(user, "PENGELUARAN"));
        if (templateId != null) {
            templateTransaksiService.cariBerdasarkanId(templateId, user).filter(tpl -> "PENGELUARAN".equalsIgnoreCase(tpl.getJenis())).ifPresent(tpl -> model.addAttribute("templateDipilih", tpl));
        }
        return "tambahPengeluaran";
    }

    @PostMapping("/pengeluaran/tambah")
    public String tambahPengeluaran(@RequestParam BigDecimal jumlah, @RequestParam String keterangan, @RequestParam(required = false, defaultValue = "Lainnya") String kategori, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            transaksiService.pengeluaran(user, jumlah, keterangan, kategori);
            redirectAttributes.addFlashAttribute("pesanPengeluaran", "Pengeluaran berhasil dicatat!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/pengeluaran";
    }
}