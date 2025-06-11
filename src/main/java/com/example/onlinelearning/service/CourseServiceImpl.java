package com.example.onlinelearning.service;

import com.example.onlinelearning.dto.CourseDto;
import com.example.onlinelearning.model.Course;
import com.example.onlinelearning.model.User;
import com.example.onlinelearning.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger; // Импортируем логгер
import org.slf4j.LoggerFactory; // Импортируем логгер
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException; // Для отлова ошибки при удалении несущ. ID
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class); // Добавляем логгер
    private final CourseRepository courseRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAllCourses() {
        log.debug("Finding all courses");
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findCourseById(Long id) {
        log.debug("Finding course by id: {}", id);
        // Используем метод с JOIN FETCH для деталей
        return courseRepository.findByIdWithSectionsAndLessons(id);
    }

    @Override
    @Transactional
    public Course saveCourse(CourseDto courseDto, User author) {
        log.info("Saving new course with title: {}", courseDto.getTitle());
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null when saving a course.");
        }
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setImageUrl(
                (courseDto.getImageUrl() != null && !courseDto.getImageUrl().trim().isEmpty())
                        ? courseDto.getImageUrl().trim()
                        : null
        );
        course.setAuthor(author);
        Course saved = courseRepository.save(course);
        log.info("Saved course with id: {}", saved.getId());
        return saved;
    }


    @Override
    @Transactional
    public Course updateCourse(Long id, CourseDto courseDto) {
        log.info("Updating course with id: {}", id);
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found for update with id: {}", id);
                    return new EntityNotFoundException("Course not found with id: " + id);
                });

        existingCourse.setTitle(courseDto.getTitle());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setImageUrl(
                (courseDto.getImageUrl() != null && !courseDto.getImageUrl().trim().isEmpty())
                        ? courseDto.getImageUrl().trim()
                        : null
        );
        Course updated = courseRepository.save(existingCourse);
        log.info("Updated course with id: {}", updated.getId());
        return updated;
    }

    /**
     * Удаляет курс по ID.
     * Проверяет существование перед удалением.
     */
    @Override
    @Transactional // Метод изменяет данные
    public void deleteCourse(Long id) {
        log.info("Attempting to delete course with id: {}", id);
        // Проверяем, существует ли курс перед удалением
        if (courseRepository.existsById(id)) {
            try {
                courseRepository.deleteById(id);
                log.info("Successfully deleted course with id: {}", id);
            } catch (EmptyResultDataAccessException e) {
                // Эта ошибка не должна возникать из-за existsById, но на всякий случай
                log.error("Error deleting course with id {}: Course not found (unexpected).", id, e);
                throw new EntityNotFoundException("Course not found with id: " + id);
            } catch (Exception e) {
                // Ловим другие возможные ошибки (например, нарушение ограничений внешнего ключа, если каскадное удаление не настроено)
                log.error("Error deleting course with id {}: {}", id, e.getMessage(), e);
                // Можно выбросить более специфичное исключение или обернуть существующее
                throw new RuntimeException("Could not delete course with id " + id + ". Reason: " + e.getMessage(), e);
            }
        } else {
            log.warn("Course with id {} not found for deletion.", id);
            throw new EntityNotFoundException("Course not found with id: " + id); // Бросаем исключение, если курс не найден
        }
        // Каскадное удаление (разделов, уроков, заданий, результатов) должно сработать
        // автоматически, если CascadeType.ALL и orphanRemoval=true настроены правильно
        // в связях @OneToMany в сущностях Course, Section, Lesson.
    }
}