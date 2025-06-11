// Откройте файл: src/main/java/com/example/onlinelearning/service/LessonServiceImpl.java
package com.example.onlinelearning.service;

import com.example.onlinelearning.dto.LessonDto; // <--- Добавляем импорт
import com.example.onlinelearning.model.Lesson;
import com.example.onlinelearning.model.Section; // <--- Добавляем импорт
import com.example.onlinelearning.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException; // <--- Добавляем импорт
import org.hibernate.Hibernate;
import org.slf4j.Logger; // <--- Добавляем импорт
import org.slf4j.LoggerFactory; // <--- Добавляем импорт
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LessonServiceImpl implements LessonService {

    private static final Logger log = LoggerFactory.getLogger(LessonServiceImpl.class); // <--- Добавляем логгер
    private final LessonRepository lessonRepository;

    @Autowired
    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lesson> findLessonByIdWithContext(Long id) {
        Optional<Lesson> lessonOpt = lessonRepository.findByIdWithSectionAndCourse(id);
        lessonOpt.ifPresent(lesson -> Hibernate.initialize(lesson.getAssignments()));
        return lessonOpt;
    }

    // --- РЕАЛИЗАЦИЯ НОВЫХ МЕТОДОВ ---

    @Override
    @Transactional
    public Lesson saveLesson(LessonDto lessonDto, Section section) {
        if (section == null) {
            log.error("Attempted to save lesson with null section. DTO: {}", lessonDto.getTitle());
            throw new IllegalArgumentException("Section cannot be null when saving a lesson.");
        }
        log.info("Saving new lesson '{}' for section id {}", lessonDto.getTitle(), section.getId());
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setLessonType(lessonDto.getLessonType());
        lesson.setContent(lessonDto.getContent()); // Позже можно добавить обработку URL/файлов
        lesson.setLessonOrder(lessonDto.getLessonOrder());
        lesson.setSection(section); // Связываем с разделом

        Lesson saved = lessonRepository.save(lesson);
        log.info("Saved lesson with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long lessonId, LessonDto lessonDto) {
        log.info("Updating lesson id {}", lessonId);
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> {
                    log.error("Lesson not found for update with id: {}", lessonId);
                    return new EntityNotFoundException("Lesson not found with id: " + lessonId);
                });

        existingLesson.setTitle(lessonDto.getTitle());
        existingLesson.setLessonType(lessonDto.getLessonType());
        existingLesson.setContent(lessonDto.getContent());
        existingLesson.setLessonOrder(lessonDto.getLessonOrder());
        // Связь с разделом не меняем при редактировании урока

        Lesson updated = lessonRepository.save(existingLesson);
        log.info("Updated lesson with id: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        log.warn("Attempting to delete lesson with id: {}", lessonId);
        if (lessonRepository.existsById(lessonId)) {
            lessonRepository.deleteById(lessonId);
            log.info("Successfully deleted lesson with id: {}", lessonId);
        } else {
            log.error("Lesson not found for deletion with id: {}", lessonId);
            throw new EntityNotFoundException("Lesson not found with id: " + lessonId);
        }
        // Каскадное удаление заданий/результатов должно сработать (если настроено в Lesson/Assignment)
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lesson> findById(Long id) {
        log.debug("Finding lesson by id (simple): {}", id);
        return lessonRepository.findById(id);
    }
}