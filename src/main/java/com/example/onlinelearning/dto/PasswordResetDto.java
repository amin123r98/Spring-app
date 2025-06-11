// src/main/java/com/example/onlinelearning/dto/PasswordResetDto.java
package com.example.onlinelearning.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class PasswordResetDto {

    @NotEmpty
    private String token; // "Секретный" токен

    @NotEmpty(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    @NotEmpty(message = "Подтвердите пароль")
    private String confirmPassword;

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}