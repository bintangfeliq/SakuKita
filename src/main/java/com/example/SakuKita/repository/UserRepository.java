package com.example.SakuKita.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SakuKita.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
