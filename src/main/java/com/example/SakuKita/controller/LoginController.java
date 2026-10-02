package com.example.SakuKita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.SakuKita.model.User;
import com.example.SakuKita.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller 
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String halamanLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userService.login(email, password);
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("pesanError", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String halamanRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String nama, @RequestParam String email, @RequestParam String password, Model model) {
        try {
            userService.tambahUser(new User(null, nama, email, password));
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("pesanError", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
