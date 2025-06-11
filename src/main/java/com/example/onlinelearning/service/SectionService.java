// src/main/java/com/example/onlinelearning/service/SectionService.java
package com.example.onlinelearning.service;

import com.example.onlinelearning.dto.SectionDto;
import com.example.onlinelearning.model.Course;
import com.example.onlinelearning.model.Section;
import java.util.Optional; // Импорт Optional

public interface SectionService {
    Section saveSection(SectionDto sectionDto, Course course);
    void deleteSection(Long sectionId);
    // Метод для обновления раздела
    Section updateSection(Long sectionId, SectionDto sectionDto);
    // Метод для получения данных раздела (например, для формы редактирования)
    Optional<Section> findById(Long sectionId);
}