package com.example.SakuKita.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SakuKita.model.User;
import com.example.SakuKita.repository.UserRepository;

@Service 
public class UserService {
    @Autowired 
    private  UserRepository userRepository;
    @Autowired 
    private SaldoService saldoService;

    public UserService(UserRepository userRepository, SaldoService saldoService) {
        this.userRepository = userRepository;
        this.saldoService = saldoService;
    }

    public User tambahUser(User user){
        Optional<User> userLama = userRepository.findByEmail(user.getEmail());
        if(userLama.isPresent()){
            throw new RuntimeException("Email sudah ada");
        }
        User userBaru = userRepository.save(user);
        saldoService.saldoAwal(userBaru);
        return userBaru;
    }

    public Optional<User> cariSesuaiId(Long id){
        return userRepository.findById(id);
    }
    
    public User updateUser(Long id, User dataBaru){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        user.setName(dataBaru.getName());
        user.setEmail(dataBaru.getEmail());
        user.setPassword(dataBaru.getPassword());
        return userRepository.save(user);
    }

    public void hapusUser(Long id){
        userRepository.deleteById(id);
    }

    public  User login(String email, String password){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email belum Terdaftar"));
        if(!user.getPassword().equals(password)){
            throw new RuntimeException("Password Salah");
        }
        return user;
    }
}
 