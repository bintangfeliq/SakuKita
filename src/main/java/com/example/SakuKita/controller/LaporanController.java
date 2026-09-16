package com.example.SakuKita.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;
import com.example.SakuKita.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LaporanController {

    private final TransaksiService transaksiService;
    private final KategoriService kategoriService;

    public LaporanController(TransaksiService transaksiService, KategoriService kategoriService) {
        this.transaksiService = transaksiService;
        this.kategoriService = kategoriService;
    }

    @GetMapping("/laporan")
    public String halamanLaporan(@RequestParam(required = false, defaultValue = "bulan") String periode, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<Transaksi> userTx = transaksiService.cariTransaksiUser(user);
        List<Transaksi> filteredTx = transaksiService.filterTransaksi(userTx, periode);
        model.addAttribute("totalPemasukan", transaksiService.totalPemasukan(user, periode));
        model.addAttribute("totalPengeluaran", transaksiService.totalPengeluaran(user, periode));
        model.addAttribute("periode", periode);
        transaksiService.muatTrenArusKas(user, model);
        kategoriService.muatAlokasiKategori(user, filteredTx, model);
        return "laporan";
    }
}
