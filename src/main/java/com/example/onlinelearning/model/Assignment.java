package com.example.onlinelearning.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description; // Описание задания, вопросы теста и т.д.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentType assignmentType;

    // Связь Многие-к-Одному с уроком (или разделом/курсом, если нужно)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false) // Привязываем к уроку
    private Lesson lesson;

    // Связь Один-ко-Многим с результатами выполнения заданий пользователями
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserAssignmentResult> results = new ArrayList<>();


    // Конструкторы
    public Assignment() {
    }

    public Assignment(String title, String description, AssignmentType assignmentType, Lesson lesson) {
        this.title = title;
        this.description = description;
        this.assignmentType = assignmentType;
        this.lesson = lesson;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public List<UserAssignmentResult> getResults() {
        return results;
    }

    public void setResults(List<UserAssignmentResult> results) {
        this.results = results;
    }
}