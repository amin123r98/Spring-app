package com.example.onlinelearning; // Убедись, что пакет правильный

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Эта аннотация включает автоконфигурацию Spring Boot, сканирование компонентов и т.д.
public class OnlineLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineLearningApplication.class, args);
    }
}