package com.example.onlinelearning.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')") // Весь контроллер только для админов
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("currentPage", "admin"); // Для подсветки в навигации
        return "admin-dashboard"; // Имя HTML-файла
    }
}