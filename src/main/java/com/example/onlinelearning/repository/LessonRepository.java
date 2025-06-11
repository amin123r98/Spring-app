package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Импортируем Query
import org.springframework.data.repository.query.Param; // Импортируем Param
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Импортируем Optional

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> { // <Lesson, Long>

    // Поиск всех уроков для конкретного раздела, отсортированных по полю 'lessonOrder'
    List<Lesson> findBySectionIdOrderByLessonOrderAsc(Long sectionId);

    /**
     * Находит урок по ID и сразу подгружает связанный раздел и курс.
     * @param id ID искомого урока.
     * @return Optional с уроком и его контекстом (раздел, курс), если найден.
     */
    @Query("SELECT les FROM Lesson les LEFT JOIN FETCH les.section sec LEFT JOIN FETCH sec.course WHERE les.id = :id")
    Optional<Lesson> findByIdWithSectionAndCourse(@Param("id") Long id);
}