package com.example.SakuKita.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private KategoriService kategoriService;

    @GetMapping("/laporan")
    public String halamanLaporan( @RequestParam(required = false) Integer bulan, @RequestParam(required = false) Integer tahun, @RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        LocalDate hariIni = LocalDate.now();
        int bulanPilih = (bulan != null && bulan >= 1 && bulan <= 12) ? bulan : hariIni.getMonthValue();
        int tahunPilih = (tahun != null && tahun >= 1900 && tahun <= 2100) ? tahun : hariIni.getYear();
        List<Transaksi> userTx = transaksiService.cariTransaksiUser(user);
        List<Transaksi> filteredTx = transaksiService.filterTransaksiBulan(userTx, bulanPilih, tahunPilih);
        BigDecimal totalPemasukan = BigDecimal.ZERO;
        BigDecimal totalPengeluaran = BigDecimal.ZERO;
        for (Transaksi t : filteredTx) {
            if ("PEMASUKAN".equalsIgnoreCase(t.getJenis()) && t.getJumlah() != null) {
                totalPemasukan = totalPemasukan.add(t.getJumlah());
            } else if ("PENGELUARAN".equalsIgnoreCase(t.getJenis()) && t.getJumlah() != null) {
                totalPengeluaran = totalPengeluaran.add(t.getJumlah());
            }
        }

        String periodeJudul = transaksiService.buatTeksPeriode(bulanPilih, tahunPilih);
        List<Integer> daftarTahun = transaksiService.buatDaftarTahun();

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
        model.addAttribute("totalPemasukan", totalPemasukan);
        model.addAttribute("totalPengeluaran", totalPengeluaran);
        model.addAttribute("bulan", bulanPilih);
        model.addAttribute("tahun", tahunPilih);
        model.addAttribute("daftarTahun", daftarTahun);
        model.addAttribute("periodeJudul", periodeJudul);
        transaksiService.diagramUang(user, model);
        kategoriService.muatAlokasiKategori(user, filteredTx, model);
        return "laporan";
    }
}
