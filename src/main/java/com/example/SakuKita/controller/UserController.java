package com.example.SakuKita.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SakuKita.model.User;
import com.example.SakuKita.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;


@RestController 
@RequestMapping ("/api/user")
public class UserController {
    @Autowired 
    private UserService userService;

    public  UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping 
    public User tambahUser(@RequestBody  User user) {
        return userService.tambahUser(user);
    }

    @GetMapping("/{id}")
    public Optional<User> cariSesuaiId(@PathVariable Long id){
        return userService.cariSesuaiId(id);
    }

    @PutMapping("/{id}")
    public User updatUser(@PathVariable Long id, @RequestBody User databaru) {
        return userService.updateUser(id, databaru);
    }

    @DeleteMapping ("/{id}")
    public  String hapusUser(@PathVariable Long id){
        userService.hapusUser(id);
        return "User dengan ID " + id + " berhasil dihapus";
    }
}
