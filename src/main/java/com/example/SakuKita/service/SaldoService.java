package com.example.SakuKita.service;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SakuKita.model.Saldo;
import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.SaldoRepository;

@Service 
@Transactional
public class SaldoService {

    private final SaldoRepository saldoRepository;

    public SaldoService(SaldoRepository saldoRepository) {
        this.saldoRepository = saldoRepository;
    }

    public Saldo saldoAwal(User user) {
        Saldo saldo = new Saldo();
        saldo.setSaldo(BigDecimal.ZERO);
        saldo.setUser(user);
        return saldoRepository.save(saldo);
    }

    public Optional<Saldo> cariSaldoUser(User user) {
        if (user == null || user.getId() == null) {
            return Optional.empty();
        }
        return saldoRepository.findByUser(user);
    }

    public Saldo tambahSaldo(User user, BigDecimal jumlah) {
        Saldo saldo = cariSaldoUser(user).orElseGet(() -> saldoAwal(user));
        BigDecimal saldoLama = saldo.getSaldo() != null ? saldo.getSaldo() : BigDecimal.ZERO;
        saldo.setSaldo(saldoLama.add(jumlah));
        return saldoRepository.save(saldo);
    }
    
    public Saldo kurangiSaldo(User user, BigDecimal jumlah) {
        Saldo saldo = cariSaldoUser(user).orElseGet(() -> saldoAwal(user));
        BigDecimal saldoLama = saldo.getSaldo() != null ? saldo.getSaldo() : BigDecimal.ZERO;
        if (saldoLama.compareTo(jumlah) < 0) {
            throw new RuntimeException("Saldo tidak mencukupi");
        }
        saldo.setSaldo(saldoLama.subtract(jumlah));
        return saldoRepository.save(saldo);
    }
}
