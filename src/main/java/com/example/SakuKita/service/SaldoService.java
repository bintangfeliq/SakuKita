package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SakuKita.model.Saldo;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.SaldoRepository;

@Service 
public class SaldoService {
    @Autowired 
    private SaldoRepository saldoRepository;

    public SaldoService(SaldoRepository saldoRepository){
        this.saldoRepository = saldoRepository;
    }

    public Saldo saldoAwal(User user){
        Saldo saldo = new  Saldo();
        saldo.setSaldo(BigDecimal.ZERO);
        saldo.setUser(user);
        return saldoRepository.save(saldo);
    }

    public Optional<Saldo> cariSaldoUser(User user){
        return saldoRepository.findByUser(user);
    }

    public Saldo tambahSaldo(User user, BigDecimal jumlah){
        Saldo saldo = saldoRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Saldo tidak ditemukan"));
        saldo.setSaldo(saldo.getSaldo().add(jumlah));
        return saldoRepository.save(saldo);
    }
    
    public Saldo kurangiSaldo(User user, BigDecimal jumlah){
        Saldo saldo = saldoRepository.findByUser(user).orElseThrow(() -> new RuntimeException("saldo tidak ditemmukan"));
        if(saldo.getSaldo().compareTo(jumlah) < 0) {
            throw new  RuntimeException("Saldo tidak mencukupi");
        }
        saldo.setSaldo(saldo.getSaldo().subtract(jumlah));
        return saldoRepository.save(saldo);
    }

    public void hapusSaldo(Long id){
        saldoRepository.deleteById(id);
    }
}
