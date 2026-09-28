package com.example.SakuKita.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.SakuKita.model.Kategori;
import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;

import jakarta.servlet.http.HttpSession;

@RequestMapping("/kategori")
@Controller
public class KategoriController {

    private final KategoriService kategoriService;

    public KategoriController(KategoriService kategoriService) {
        this.kategoriService = kategoriService;
    }

   @GetMapping
public String kategori(@RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/login";
    }
    List<Kategori> semuaKategori = kategoriService.cariKategoriUser(user);
    int pageSize = 10;
    int totalItems = semuaKategori.size();
    int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    page = Math.max(1, Math.min(page, totalPages));
    int fromIndex = (page - 1) * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, totalItems);
    List<Kategori> pagedKategori = semuaKategori.subList(fromIndex, toIndex);

    model.addAttribute("kategori", pagedKategori);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", totalPages);
    model.addAttribute("totalItems", totalItems);
    model.addAttribute("fromIndex", totalItems == 0 ? 0 : fromIndex + 1);
    model.addAttribute("toIndex", toIndex);
    return "kategori";
}
    @PostMapping("/tambah")
    public String tambah(@RequestParam String nama, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            kategoriService.tambahKategori(user, nama);
            redirect.addFlashAttribute("pesanKategori", "Kategori berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/kategori";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam Long id, @RequestParam String nama, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            kategoriService.updateKategori(user, id, nama);
            redirect.addFlashAttribute("pesanKategori", "Kategori berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/kategori";
    }

    @PostMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            kategoriService.hapusKategori(user, id);
            redirect.addFlashAttribute("pesanKategori", "Kategori berhasil dihapus!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/kategori";
    }
}