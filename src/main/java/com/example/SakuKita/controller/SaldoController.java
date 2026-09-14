package com.example.SakuKita.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SakuKita.model.Saldo;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.UserRepository;
import com.example.SakuKita.service.SaldoService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;



@RestController 
@RequestMapping ("/api/saldo")
public class SaldoController {
    @Autowired 
    private  SaldoService saldoService;
    @Autowired 
    private UserRepository userRepository;

    public SaldoController(SaldoService saldoService, UserRepository userRepository){
        this.saldoService = saldoService;
        this.userRepository = userRepository;
    }

    @PostMapping ("/awal")
    public Saldo saldoAwal(@RequestParam Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak di temukan"));
        return saldoService.saldoAwal(user);
    }

    @GetMapping("/{userId}")
    public Optional<Saldo> cariSaldoUser(@PathVariable  Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return saldoService.cariSaldoUser(user);
    }

    @PutMapping("/tambah")
    public Saldo tambahSaldo(@RequestParam Long userId, @RequestParam BigDecimal jumlah) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user tidak ditemukan"));
        return saldoService.tambahSaldo(user, jumlah);
    }

    @PutMapping("/kurangi")
    public Saldo kurangiSaldo(@RequestParam Long userId,@RequestParam BigDecimal jumlah) {
    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    return saldoService.kurangiSaldo(user, jumlah);
    }

    @DeleteMapping ("/{id}")
    public String hapusSaoldo(@PathVariable Long id) {
        saldoService.hapusSaldo(id);
        return "Saldo dengan ID " + id + " berhasil dihapus";
    }
    
}
