// Создайте новый файл: src/main/java/com/example/onlinelearning/dto/LessonDto.java
package com.example.onlinelearning.dto;

import com.example.onlinelearning.model.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LessonDto {

    @NotEmpty(message = "Название урока не может быть пустым")
    @Size(max = 255, message = "Название слишком длинное")
    private String title;

    @NotNull(message = "Необходимо выбрать тип урока")
    private LessonType lessonType;

    // Содержимое урока (текст, URL видео, URL ресурса, путь к файлу)
    // Можно не делать @NotEmpty, т.к. не для всех типов это обязательно
    @Size(max = 5000, message = "Содержимое слишком длинное") // Ограничение для текста
    private String content;

    @NotNull(message = "Порядок не может быть пустым")
    @Min(value = 1, message = "Порядок должен быть положительным числом")
    private Integer lessonOrder;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(LessonType lessonType) {
        this.lessonType = lessonType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLessonOrder() {
        return lessonOrder;
    }

    public void setLessonOrder(Integer lessonOrder) {
        this.lessonOrder = lessonOrder;
    }
}