package com.example.onlinelearning.service;

import com.example.onlinelearning.dto.SectionDto;
import com.example.onlinelearning.model.Course;
import com.example.onlinelearning.model.Section;
import com.example.onlinelearning.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class SectionServiceImpl implements SectionService { // Убедитесь, что имя класса здесь SectionServiceImpl

    private static final Logger log = LoggerFactory.getLogger(SectionServiceImpl.class);
    private final SectionRepository sectionRepository;
    // Возможно, понадобится CourseRepository, если нужно проверять существование курса
    // private final CourseRepository courseRepository;

    @Autowired
    public SectionServiceImpl(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Override
    @Transactional
    public Section saveSection(SectionDto sectionDto, Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null when saving a section.");
        }
        log.info("Saving new section '{}' for course id {}", sectionDto.getTitle(), course.getId());
        Section section = new Section();
        section.setTitle(sectionDto.getTitle());
        section.setOrder(sectionDto.getOrder());
        section.setCourse(course); // Связываем с курсом
        Section saved = sectionRepository.save(section);
        log.info("Saved section with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteSection(Long sectionId) {
        log.warn("Attempting to delete section with id: {}", sectionId);
        if (sectionRepository.existsById(sectionId)) {
            sectionRepository.deleteById(sectionId);
            log.info("Successfully deleted section with id: {}", sectionId);
        } else {
            log.error("Section not found for deletion with id: {}", sectionId);
            throw new EntityNotFoundException("Section not found with id: " + sectionId);
        }
        // Каскадное удаление уроков/заданий должно сработать из-за настроек в Section/Lesson
    }

    @Override
    @Transactional(readOnly = true) // Только чтение
    public Optional<Section> findById(Long sectionId) {
        log.debug("Finding section by id: {}", sectionId);
        return sectionRepository.findById(sectionId);
    }


    @Override
    @Transactional
    public Section updateSection(Long sectionId, SectionDto sectionDto) {
        log.info("Updating section id {}", sectionId);
        Section existingSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> {
                    log.error("Section not found for update with id: {}", sectionId);
                    return new EntityNotFoundException("Section not found with id: " + sectionId);
                });

        existingSection.setTitle(sectionDto.getTitle());
        existingSection.setOrder(sectionDto.getOrder());
        // Связь с курсом не меняем при редактировании раздела

        Section updated = sectionRepository.save(existingSection);
        log.info("Updated section with id: {}", updated.getId());
        return updated;
    }
}

