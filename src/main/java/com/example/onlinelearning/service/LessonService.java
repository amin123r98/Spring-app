// Откройте файл: src/main/java/com/example/onlinelearning/service/LessonService.java
package com.example.onlinelearning.service;

import com.example.onlinelearning.dto.LessonDto; // <--- Импортируем DTO
import com.example.onlinelearning.model.Lesson;
import com.example.onlinelearning.model.Section; // <--- Импортируем Section
import java.util.Optional;

/**
 * Интерфейс для бизнес-логики, связанной с уроками.
 */
public interface LessonService {

    /**
     * Находит урок по его идентификатору, включая информацию о разделе и курсе.
     * @param id Идентификатор урока.
     * @return Optional<Lesson>, содержащий урок и его контекст, если найден.
     */
    Optional<Lesson> findLessonByIdWithContext(Long id);

    // --- НОВЫЕ МЕТОДЫ ---

    /**
     * Сохраняет новый урок для указанного раздела.
     * @param lessonDto DTO с данными урока.
     * @param section Раздел, к которому относится урок.
     * @return Сохраненный объект Lesson.
     */
    Lesson saveLesson(LessonDto lessonDto, Section section);

    /**
     * Обновляет существующий урок.
     * @param lessonId ID урока для обновления.
     * @param lessonDto DTO с новыми данными урока.
     * @return Обновленный объект Lesson.
     * @throws jakarta.persistence.EntityNotFoundException если урок не найден.
     */
    Lesson updateLesson(Long lessonId, LessonDto lessonDto);

    /**
     * Удаляет урок по его ID.
     * @param lessonId ID урока для удаления.
     * @throws jakarta.persistence.EntityNotFoundException если урок не найден.
     */
    void deleteLesson(Long lessonId);

    /**
     * Находит урок по ID (без глубокой загрузки контекста, для редактирования/удаления).
     * @param id Идентификатор урока.
     * @return Optional<Lesson>.
     */
    Optional<Lesson> findById(Long id); // <-- Добавим простой findById
}