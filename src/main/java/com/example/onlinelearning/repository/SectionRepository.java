package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCourseIdOrderByOrderAsc(Long courseId);
    // Можно добавить метод для поиска по названию, если нужно
    Optional<Section> findByCourseIdAndTitleIgnoreCase(Long courseId, String title);
}