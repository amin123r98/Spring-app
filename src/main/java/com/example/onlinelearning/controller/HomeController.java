package com.example.onlinelearning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// Можно также использовать Authentication для получения больше деталей
import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/") // Обрабатываем GET запросы на корневой URL
    public String homePage(Model model, Principal principal) {
        // Principal будет не null, если пользователь аутентифицирован
        if (principal != null) {
            // Добавляем имя пользователя в модель, чтобы его можно было использовать в шаблоне
            // Хотя sec:authentication="name" делает то же самое проще
            model.addAttribute("username", principal.getName());
        }
        // Добавляем атрибут для определения активной страницы
        model.addAttribute("currentPage", "home"); // <--- ИЗМЕНЕНИЕ
        // Возвращаем имя шаблона для главной страницы (index.html)
        return "index";
    }
}