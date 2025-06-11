package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Импортируем Query
import org.springframework.data.repository.query.Param; // Импортируем Param
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Импортируем Optional

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> { // <Course, Long>

    // Поиск курсов по автору
    List<Course> findByAuthorId(Long authorId);

    // Поиск по части названия (регистронезависимый)
    List<Course> findByTitleContainingIgnoreCase(String titleKeyword);

    /**
     * Находит курс по ID и сразу подгружает связанные разделы и уроки.
     * Используется LEFT JOIN FETCH, чтобы курс загрузился, даже если у него нет разделов или уроков.
     * @param id ID искомого курса.
     * @return Optional с курсом и его структурой, если найден.
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.sections s LEFT JOIN FETCH s.lessons WHERE c.id = :id")
    Optional<Course> findByIdWithSectionsAndLessons(@Param("id") Long id);
}