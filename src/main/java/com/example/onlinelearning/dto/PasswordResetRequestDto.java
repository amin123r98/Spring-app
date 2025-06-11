package com.example.onlinelearning.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class PasswordResetRequestDto {
    @NotEmpty(message = "Email не может быть пустым")
    @Email(message = "Введите корректный Email")
    private String email;

    // Getter and Setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}