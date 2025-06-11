package com.example.onlinelearning.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL; // Для валидации URL

/**
 * DTO для создания/редактирования курса.
 */
public class CourseDto {

    @NotEmpty(message = "Название курса не может быть пустым")
    @Size(min = 3, max = 255, message = "Название должно содержать от 3 до 255 символов")
    private String title;

    // Описание не обязательно, но можно добавить ограничение по длине, если нужно
    @Size(max = 5000, message = "Описание слишком длинное (макс 5000 символов)")
    private String description;

    // URL картинки не обязателен, но если указан - должен быть валидным URL
    @URL(message = "Введите корректный URL изображения", protocol = "https", flags = {}) // Требуем https
    @Size(max = 255, message = "URL слишком длинный")
    private String imageUrl;

    // --- Геттеры и сеттеры ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}