package com.example.SakuKita.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.Saldo;

public interface SaldoRepository extends  JpaRepository<Saldo, Long>{
    
}
