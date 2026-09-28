package com.example.SakuKita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.TemplateTransaksi;
import com.example.SakuKita.model.User;

public interface TemplateTransaksiRepository extends JpaRepository<TemplateTransaksi, Long> {
    List<TemplateTransaksi> findByUserOrderByIdDesc(User user);
    List<TemplateTransaksi> findByUserAndJenisOrderByIdDesc(User user, String jenis);
    Optional<TemplateTransaksi> findByIdAndUser(Long id, User user);
}
