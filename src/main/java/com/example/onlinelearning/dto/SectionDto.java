// src/main/java/com/example/onlinelearning/dto/SectionDto.java
package com.example.onlinelearning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SectionDto {

    @NotEmpty(message = "Название раздела не может быть пустым")
    @Size(max = 255, message = "Название слишком длинное")
    private String title;

    @NotNull(message = "Порядок не может быть пустым")
    @Min(value = 1, message = "Порядок должен быть положительным числом")
    private Integer order; // Порядок отображения

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}