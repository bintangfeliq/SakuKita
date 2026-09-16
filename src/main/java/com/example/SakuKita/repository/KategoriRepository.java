package com.example.SakuKita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.Kategori;
import com.example.SakuKita.model.User;

public interface KategoriRepository extends JpaRepository<Kategori, Long> {
    List<Kategori> findByUserOrderByIdDesc(User user);
    Optional<Kategori> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNamaIgnoreCase(Long userId, String nama);
}