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
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            throw new RuntimeException("Data pendaftaran tidak lengkap");
        }
        String emailBersih = user.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(emailBersih).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar");
        }
        user.setEmail(emailBersih);
        if (user.getName() != null) {
            user.setName(user.getName().trim());
        }
        user.setPassword(bcrypt.encode(user.getPassword().trim()));
        User userBaru = userRepository.save(user);
        saldoService.saldoAwal(userBaru);
        return userBaru;
    }

    public User login(String email, String password) {
        if (email == null || password == null) {
            throw new RuntimeException("Email dan kata sandi wajib diisi");
        }
        String emailBersih = email.trim().toLowerCase();
        User user = userRepository.findByEmail(emailBersih)
                .orElseThrow(() -> new RuntimeException("Email belum terdaftar"));

        String inputPassword = password.trim();
        String storedPassword = user.getPassword();

        boolean passwordCocok = false;
        if (storedPassword != null) {
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                passwordCocok = bcrypt.matches(inputPassword, storedPassword);
            } else {
                passwordCocok = storedPassword.equals(inputPassword);
                if (passwordCocok) {
                    user.setPassword(bcrypt.encode(inputPassword));
                    userRepository.save(user);
                }
            }
        }

        if (!passwordCocok) { 
            throw new RuntimeException("Kata sandi salah");
        }
        return user;
    }
}