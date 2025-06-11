package com.example.onlinelearning.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING) // Храним тип урока как строку в базе данных
    @Column(nullable = false, length = 50)
    private LessonType lessonType;

    @Lob // Для хранения основного контента урока (текст, ссылка на видео, путь к файлу)
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "lesson_order") // Порядок урока в разделе
    private int lessonOrder;

    // Связь Многие-к-Одному с разделом
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // Связь Один-ко-Многим с заданиями (если задания привязаны к урокам)
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Assignment> assignments = new ArrayList<>();

    // Конструкторы
    public Lesson() {
    }

    public Lesson(String title, LessonType lessonType, String content, int lessonOrder, Section section) {
        this.title = title;
        this.lessonType = lessonType;
        this.content = content;
        this.lessonOrder = lessonOrder;
        this.section = section;
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

    public LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(LessonType lessonType) {
        this.lessonType = lessonType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLessonOrder() {
        return lessonOrder;
    }

    public void setLessonOrder(int lessonOrder) {
        this.lessonOrder = lessonOrder;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public void addAssignment(Assignment assignment) {
        this.assignments.add(assignment);
        assignment.setLesson(this);
    }

    public void removeAssignment(Assignment assignment) {
        this.assignments.remove(assignment);
        assignment.setLesson(null);
    }
}