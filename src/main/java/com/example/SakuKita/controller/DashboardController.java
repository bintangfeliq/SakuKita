package com.example.SakuKita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class DashboardController {
    @GetMapping ("/dashboard")
    public String halamanDashboard(){
        return "dashboard";
    }
}
