package com.example.SakuKita.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.Saldo;
import com.example.SakuKita.model.User;

public interface SaldoRepository extends  JpaRepository<Saldo, Long>{
    Optional<Saldo> findByUser(User user);
}
