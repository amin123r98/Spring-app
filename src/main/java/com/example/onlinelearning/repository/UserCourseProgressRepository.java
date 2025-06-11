package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.UserCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, Long> { // <UserCourseProgress, Long>

    // Поиск прогресса конкретного пользователя для конкретного курса
    Optional<UserCourseProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    // Поиск всего прогресса для конкретного пользователя
    List<UserCourseProgress> findByUserId(Long userId);

    // Поиск всего прогресса для конкретного курса (например, для статистики)
    List<UserCourseProgress> findByCourseId(Long courseId);
}