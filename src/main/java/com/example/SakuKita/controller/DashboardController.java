package com.example.SakuKita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.SakuKita.model.User;
import com.example.SakuKita.service.SaldoService;
import com.example.SakuKita.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller 
public class DashboardController {

    private final SaldoService saldoService;
    private final TransaksiService transaksiService;

    public DashboardController(SaldoService saldoService, TransaksiService transaksiService) {
        this.saldoService = saldoService;
        this.transaksiService = transaksiService;
    }

    @GetMapping("/dashboard")
    public String halamanDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("saldo", saldoService.cariSaldoUser(user).orElseGet(() -> saldoService.saldoAwal(user)));
        model.addAttribute("pemasukan", transaksiService.pemasukanBulanan(user));
        model.addAttribute("pengeluaran", transaksiService.pengeluaranBulanan(user));
        model.addAttribute("transaksiTerakhir", transaksiService.cari5TransaksiTerakhir(user));
        return "dashboard";
    }
}