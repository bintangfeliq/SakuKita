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
import com.example.SakuKita.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller 
public class PengeluaranController {

    private final TransaksiService transaksiService;
    private final KategoriService kategoriService;

    public PengeluaranController(TransaksiService transaksiService, KategoriService kategoriService) {
        this.transaksiService = transaksiService;
        this.kategoriService = kategoriService;
    }

    @GetMapping("/pengeluaran")
    public String dashboardPengeluaran(@RequestParam(required = false, defaultValue = "bulan") String periode, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<Transaksi> userTx = transaksiService.cariTransaksiUser(user).stream().filter(t -> "PENGELUARAN".equalsIgnoreCase(t.getJenis())).toList();
        model.addAttribute("transaksi", transaksiService.filterTransaksi(userTx, periode));
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        model.addAttribute("totalPengeluaran", transaksiService.totalPengeluaran(user, periode));
        model.addAttribute("periode", periode);
        return "pengeluaran";
    }

    @GetMapping("/tambahPengeluaran")
    public String halamanTambahPengeluaran(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        return "tambahPengeluaran";
    }

    @PostMapping("/pengeluaran/tambah")
    public String tambahPengeluaran(@RequestParam BigDecimal jumlah, @RequestParam String keterangan, @RequestParam(required = false, defaultValue = "Lainnya") String kategori, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            transaksiService.pengeluaran(user, jumlah, keterangan, kategori);
            redirectAttributes.addFlashAttribute("pesanPengeluaran", "Pengeluaran berhasil dicatat!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/pengeluaran";
    }
}

