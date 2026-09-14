package com.example.SakuKita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.Transaksi;
import com.example.SakuKita.model.User;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long>{
    List<Transaksi> findByUser(User user);
}
