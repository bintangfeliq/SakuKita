package com.example.SakuKita.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.Transaksi;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long>{
    
}
