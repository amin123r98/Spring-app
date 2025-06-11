// src/main/java/com/example/onlinelearning/controller/UserProfileController.java
package com.example.onlinelearning.controller;

import com.example.onlinelearning.model.User;
import com.example.onlinelearning.model.UserCourseProgress;
import com.example.onlinelearning.repository.UserCourseProgressRepository;
import com.example.onlinelearning.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserService userService;
    private final UserCourseProgressRepository progressRepository;

    @Autowired
    public UserProfileController(UserService userService, UserCourseProgressRepository progressRepository) {
        this.userService = userService;
        this.progressRepository = progressRepository;
    }

    @GetMapping
    public String viewProfile(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            // Загружаем прогресс пользователя по курсам
            List<UserCourseProgress> progressList = progressRepository.findByUserId(user.getId());
            model.addAttribute("progressList", progressList);
            model.addAttribute("currentPage", "profile"); // <--- ИЗМЕНЕНИЕ

            log.info("Displaying profile for user: {}", username);
            return "profile"; // Имя шаблона profile.html
        } else {
            log.error("Authenticated user {} not found in database.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось загрузить профиль пользователя.");
            return "redirect:/";
        }
    }
}