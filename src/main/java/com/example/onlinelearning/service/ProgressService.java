// src/main/java/com/example/onlinelearning/service/ProgressService.java
package com.example.onlinelearning.service;

public interface ProgressService {
    /**
     * Обновляет прогресс пользователя по курсу на основе выполненных/оцененных заданий.
     * @param userId ID пользователя
     * @param courseId ID курса
     */
    void updateUserCourseProgress(Long userId, Long courseId);
}