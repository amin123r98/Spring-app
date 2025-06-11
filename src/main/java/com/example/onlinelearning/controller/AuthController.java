package com.example.onlinelearning.controller;

import com.example.onlinelearning.dto.PasswordResetDto;
import com.example.onlinelearning.dto.PasswordResetRequestDto;
import com.example.onlinelearning.dto.UserRegistrationDto;
import com.example.onlinelearning.model.User;
import com.example.onlinelearning.service.UserService;
import jakarta.validation.Valid;
// import org.springframework.security.core.userdetails.UsernameNotFoundException; // Не используется напрямую здесь
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- Регистрация ---

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto()); // Имя объекта userDto
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String registerUserAccount(
            @ModelAttribute("userDto") @Valid UserRegistrationDto registrationDto, // Имя объекта userDto
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.userDto", "Пароли не совпадают");
        }
        // Проверяем email, который теперь также является username
        if (userService.existsByEmail(registrationDto.getEmail())) {
            result.rejectValue("email", "error.userDto", "Пользователь с таким email уже существует");
        }

        if (result.hasErrors()) {
            // model.addAttribute("userDto", registrationDto); // Уже в модели из-за @ModelAttribute
            return "register";
        }

        User newUser = new User();
        newUser.setEmail(registrationDto.getEmail());
        newUser.setUsername(registrationDto.getEmail()); // Username теперь равен email
        newUser.setPassword(registrationDto.getPassword()); // Пароль будет закодирован в сервисе
        // firstName и lastName остаются null/empty

        try {
            userService.save(newUser); // Сохранение с кодированием пароля и присвоением роли
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация прошла успешно! Теперь вы можете войти.");
            return "redirect:/login";
        } catch (Exception e) {
            // log.error("Error during user registration", e);
            model.addAttribute("errorMessage", "Произошла ошибка при регистрации. Пожалуйста, попробуйте снова.");
            // model.addAttribute("userDto", registrationDto); // Уже в модели
            return "register";
        }
    }

    // --- Вход ---

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        // Сообщения об успехе/ошибке передаются через параметры URL (Spring Security)
        // или RedirectAttributes (после регистрации)
        return "login"; // login.html
    }

    // --- Восстановление пароля (Упрощенное) ---

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("passwordResetRequest", new PasswordResetRequestDto());
        return "forgot-password"; // forgot-password.html
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@ModelAttribute("passwordResetRequest") @Valid PasswordResetRequestDto requestDto,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "forgot-password";
        }
        Optional<User> userOpt = userService.findByEmail(requestDto.getEmail());
        if (userOpt.isPresent()) {
            // Имитируем генерацию токена (вывод в консоль в UserServiceImpl)
            userService.createPasswordResetTokenForUser(userOpt.get());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Инструкции по сбросу пароля (токен) были выведены в консоль сервера (для теста).");
        } else {
            // Не раскрываем информацию о существовании email
            redirectAttributes.addFlashAttribute("successMessage",
                    "Если email зарегистрирован, инструкции (токен) отправлены (выведены в консоль).");
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "token", required = false) String token, Model model, RedirectAttributes redirectAttributes) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Отсутствует токен сброса пароля.");
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.getUserByPasswordResetToken(token);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Недействительный или истекший токен сброса пароля.");
            return "redirect:/login";
        }

        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken(token);
        model.addAttribute("passwordReset", passwordResetDto);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@ModelAttribute("passwordReset") @Valid PasswordResetDto resetDto,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        if (!resetDto.getPassword().equals(resetDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.passwordReset", "Пароли не совпадают");
        }

        Optional<User> userOpt = userService.getUserByPasswordResetToken(resetDto.getToken());
        if (userOpt.isEmpty()) {
            result.reject("error.passwordReset", "Недействительный или истекший токен сброса пароля.");
        }

        if (result.hasErrors()) {
            // Атрибут passwordReset (с токеном и введенными паролями) уже в модели из-за @ModelAttribute
            return "reset-password";
        }

        userService.changeUserPassword(userOpt.get(), resetDto.getPassword());

        redirectAttributes.addFlashAttribute("successMessage", "Ваш пароль был успешно изменен. Теперь вы можете войти.");
        return "redirect:/login";
    }
}
