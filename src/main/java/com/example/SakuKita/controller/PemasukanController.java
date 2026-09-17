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
public class PemasukanController {

    private final TransaksiService transaksiService;
    private final KategoriService kategoriService;

    public PemasukanController(TransaksiService transaksiService, KategoriService kategoriService) {
        this.transaksiService = transaksiService;
        this.kategoriService = kategoriService;
    }

    @GetMapping("/pemasukan")
    public String halamanPemasukan(@RequestParam(defaultValue = "bulan") String periode, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Transaksi> transaksi = transaksiService.cariTransaksiUser(user).stream().filter(t -> "PEMASUKAN".equalsIgnoreCase(t.getJenis())).toList();
        model.addAttribute("transaksi", transaksiService.filterTransaksi(transaksi, periode));
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        model.addAttribute("totalPemasukan", transaksiService.totalPemasukan(user, periode));
        model.addAttribute("periode", periode);
        return "pemasukan";
    }

    @GetMapping("/tambahPemasukan")
    public String halamanTambahPemasukan(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
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