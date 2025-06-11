package com.example.onlinelearning.service;

import com.example.onlinelearning.model.Assignment;
import java.util.Optional;

public interface AssignmentService {
    /**
     * Находит задание по ID вместе с его контекстом (урок, раздел, курс).
     * @param assignmentId ID задания.
     * @return Optional<Assignment>.
     */
    Optional<Assignment> findAssignmentByIdWithDetails(Long assignmentId);

    // TODO: Добавить методы для сохранения/проверки ответа пользователя
}