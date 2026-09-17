package com.example.SakuKita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.SakuKita.model.User;
import com.example.SakuKita.service.KategoriService;

import jakarta.servlet.http.HttpSession;

@Controller
public class KategoriController {

    private final KategoriService kategoriService;

    public KategoriController(KategoriService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @GetMapping("/kategori")
    public String kategori(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("kategori", kategoriService.cariKategoriUser(user));
        return "kategori";
    }

    @PostMapping("/kategori/tambah")
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

    @PostMapping("/kategori/edit")
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

    @PostMapping("/kategori/hapus/{id}")
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