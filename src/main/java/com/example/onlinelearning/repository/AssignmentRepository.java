package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Импорт
import org.springframework.data.repository.query.Param; // Импорт
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Импорт

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> { // <Assignment, Long>

    // Поиск всех заданий для конкретного урока
    List<Assignment> findByLessonId(Long lessonId);

    /**
     * Находит задание по ID и сразу подгружает связанный Урок, его Раздел и Курс.
     * @param id ID искомого задания.
     * @return Optional с заданием и его полным контекстом, если найдено.
     */
    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.lesson les " +
            "LEFT JOIN FETCH les.section sec " +
            "LEFT JOIN FETCH sec.course c " +
            "WHERE a.id = :id")
    Optional<Assignment> findByIdWithDetails(@Param("id") Long id);
}