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
public String halamanLaporan( @RequestParam(defaultValue = "bulan") String periode, @RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/login";
    }
    List<Transaksi> userTx = transaksiService.cariTransaksiUser(user);
    List<Transaksi> filteredTx = transaksiService.filterTransaksi(userTx, periode);
    int pageSize = 10;
    int totalItems = filteredTx.size();
    int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    page = Math.max(1, Math.min(page, totalPages));
    int fromIndex = (page - 1) * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, totalItems);
    List<Transaksi> pagedTx = filteredTx.subList(fromIndex, toIndex);

        model.addAttribute("transaksi", pagedTx);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("fromIndex", totalItems == 0 ? 0 : fromIndex + 1);
        model.addAttribute("toIndex", toIndex);
        model.addAttribute("totalPemasukan", transaksiService.totalPemasukan(user, periode));
        model.addAttribute("totalPengeluaran", transaksiService.totalPengeluaran(user, periode));
        model.addAttribute("periode", periode);
        transaksiService.diagramUang(user, model);
        kategoriService.muatAlokasiKategori(user, filteredTx, model);
        return "laporan";
    }
}
