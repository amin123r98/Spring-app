package com.example.onlinelearning.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_course_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id"}) // Пользователь может иметь только одну запись прогресса для курса
})
public class UserCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь Многие-к-Одному с пользователем
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Связь Многие-к-Одному с курсом
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Можно добавить поля для отслеживания прогресса:
    // Например, список пройденных уроков (сложнее реализовать)
    // Или просто процент завершения
    @Column(name = "completed_percentage", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int completedPercentage = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Конструкторы
    public UserCourseProgress() {
    }

    public UserCourseProgress(User user, Course course) {
        this.user = user;
        this.course = course;
        this.lastAccessedAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getCompletedPercentage() {
        return completedPercentage;
    }

    public void setCompletedPercentage(int completedPercentage) {
        this.completedPercentage = completedPercentage;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}