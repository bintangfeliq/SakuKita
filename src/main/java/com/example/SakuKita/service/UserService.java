package com.example.SakuKita.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final SaldoService saldoService;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, SaldoService saldoService) {
        this.userRepository = userRepository;
        this.saldoService = saldoService;
    }

    public User tambahUser(User user) {
        String email = user.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar");
        }
        user.setEmail(email);
        if (user.getName() != null) {
            user.setName(user.getName().trim());
        }
        user.setPassword(bcrypt.encode(user.getPassword().trim()));
        User userBaru = userRepository.save(user);
        saldoService.saldoAwal(userBaru);
        return userBaru;
    }

    public User login(String email, String password) {
        email = email.trim().toLowerCase();
        password = password.trim();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email belum terdaftar"));
        String passwordTersimpan = user.getPassword();
        if (!bcrypt.matches(password, passwordTersimpan)) {
            throw new RuntimeException("Kata sandi salah");
        }
        return user;
    }
}