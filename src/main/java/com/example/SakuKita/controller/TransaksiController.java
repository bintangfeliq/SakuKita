package com.example.SakuKita.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.UserRepository;
import com.example.SakuKita.service.TransaksiService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController 
@RequestMapping ("/api/transaksi")
public class TransaksiController {
    @Autowired 
    private TransaksiService transaksiService;
    @Autowired 
    private UserRepository userRepository;

    public TransaksiController(TransaksiService transaksiService, UserRepository userRepository){
        this.transaksiService = transaksiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/pemasukan")
    public Transaksi pemasukan(@RequestParam Long userId, @RequestParam BigDecimal jumlah, @RequestParam String keterangan){ 
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return transaksiService.pemasukan(user, jumlah, keterangan);
    }

    @PostMapping ("/pengeluaran")
    public  Transaksi pengeluaran(@RequestParam Long userId, @RequestParam BigDecimal jumlah, @RequestParam String keterangan){
        User user =  userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return transaksiService.pengeluaran(user, jumlah, keterangan);
    }
    
    @GetMapping ("/{userId}")
    public List<Transaksi> cariTransaksiUser(@PathVariable Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return transaksiService.cariTransaksiUser(user);
    }

    @DeleteMapping ("/{id}")
    public String hapusTransaksi(@PathVariable Long id){
        transaksiService.hapusTransaksi(id);
        return "Transaksi dengan ID " + id + " berhasil dihapus";
    }
}
