package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.UserAssignmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssignmentResultRepository extends JpaRepository<UserAssignmentResult, Long> { // <UserAssignmentResult, Long>

    // Поиск результата конкретного пользователя для конкретного задания
    Optional<UserAssignmentResult> findByUserIdAndAssignmentId(Long userId, Long assignmentId);

    // Поиск всех результатов для конкретного пользователя
    List<UserAssignmentResult> findByUserId(Long userId);

    // Поиск всех результатов для конкретного задания (например, для проверки преподавателем)
    List<UserAssignmentResult> findByAssignmentId(Long assignmentId);
}