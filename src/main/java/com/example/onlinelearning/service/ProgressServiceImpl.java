// src/main/java/com/example/onlinelearning/service/ProgressServiceImpl.java
package com.example.onlinelearning.service;

import com.example.onlinelearning.model.*;
import com.example.onlinelearning.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressServiceImpl implements ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressServiceImpl.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserAssignmentResultRepository resultRepository;
    private final UserCourseProgressRepository progressRepository;

    @Autowired
    public ProgressServiceImpl(CourseRepository courseRepository,
                               UserRepository userRepository,
                               UserAssignmentResultRepository resultRepository,
                               UserCourseProgressRepository progressRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    @Transactional // Важно для работы с несколькими репозиториями
    public void updateUserCourseProgress(Long userId, Long courseId) {
        log.debug("Updating progress for user {} in course {}", userId, courseId);

        // Находим пользователя и курс (в реальном приложении нужна обработка ошибок)
        Optional<User> userOpt = userRepository.findById(userId);
        // Используем findByIdWithSectionsAndLessons для загрузки структуры курса
        Optional<Course> courseOpt = courseRepository.findByIdWithSectionsAndLessons(courseId);

        if (userOpt.isEmpty() || courseOpt.isEmpty()) {
            log.warn("User or Course not found. userId={}, courseId={}", userId, courseId);
            return; // Или выбросить исключение
        }

        User user = userOpt.get();
        Course course = courseOpt.get();

        // 1. Считаем общее количество заданий в курсе
        long totalAssignments = 0;
        for (Section section : course.getSections()) {
            for (Lesson lesson : section.getLessons()) {
                totalAssignments += lesson.getAssignments().size(); // Предполагаем, что getAssignments не null
            }
        }

        if (totalAssignments == 0) {
            log.debug("Course {} has no assignments. Progress set to 0.", courseId);
            // Если заданий нет, прогресс 0 или 100? Решим, что 0.
            UserCourseProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId)
                    .orElse(new UserCourseProgress(user, course));
            progress.setCompletedPercentage(0);
            progress.setLastAccessedAt(LocalDateTime.now());
            progressRepository.save(progress);
            return;
        }

        // 2. Считаем количество выполненных (или оцененных) заданий пользователем в этом курсе
        List<UserAssignmentResult> userResults = resultRepository.findByUserId(userId);
        long completedAssignments = userResults.stream()
                .filter(result -> result.getAssignment() != null && // Защита от null
                        result.getAssignment().getLesson() != null &&
                        result.getAssignment().getLesson().getSection() != null &&
                        result.getAssignment().getLesson().getSection().getCourse() != null &&
                        result.getAssignment().getLesson().getSection().getCourse().getId().equals(courseId))
                // Условие завершенности: есть оценка или просто отправлен (можно усложнить)
                .filter(result -> result.getGrade() != null || result.getSubmittedAt() != null)
                .map(result -> result.getAssignment().getId()) // Получаем ID задания
                .distinct() // Считаем каждое задание только один раз
                .count();

        // 3. Считаем процент
        int percentage = (int) Math.round(((double) completedAssignments / totalAssignments) * 100);

        // 4. Обновляем или создаем запись прогресса
        UserCourseProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(new UserCourseProgress(user, course));

        progress.setCompletedPercentage(percentage);
        progress.setLastAccessedAt(LocalDateTime.now());
        if (percentage >= 100 && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now()); // Отмечаем время завершения
        } else if (percentage < 100) {
            progress.setCompletedAt(null); // Сбрасываем время завершения, если прогресс упал
        }

        progressRepository.save(progress);
        log.info("Progress updated for user {} in course {}: {}%", userId, courseId, percentage);
    }
}