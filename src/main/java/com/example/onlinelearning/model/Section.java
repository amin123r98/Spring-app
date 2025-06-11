package com.example.onlinelearning.model;

import jakarta.persistence.*;
// Заменяем импорты List/ArrayList на Set/LinkedHashSet
// import java.util.ArrayList;
// import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    // Поле для порядка раздела в курсе
    // Имя поля в Java - 'order', имя колонки в БД - 'section_order'
    @Column(name = "section_order")
    private int order;

    // Связь Многие-к-Одному с курсом
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Связь Один-ко-Многим с уроками
    // Заменяем List на Set (используем LinkedHashSet для сохранения порядка)
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lessonOrder ASC") // Сортируем уроки по их порядку (поле lessonOrder в Lesson.java)
    private Set<Lesson> lessons = new LinkedHashSet<>(); // Инициализируем как LinkedHashSet

    // Конструкторы
    public Section() {
    }

    public Section(String title, int order, Course course) {
        this.title = title;
        this.order = order;
        this.course = course;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    // Геттер и Сеттер для lessons теперь используют Set
    public Set<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(Set<Lesson> lessons) {
        this.lessons = lessons;
    }

    // Методы добавления/удаления (логика остается прежней, т.к. Set имеет add/remove)
    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
        lesson.setSection(this); // Устанавливаем обратную связь
    }

    public void removeLesson(Lesson lesson) {
        this.lessons.remove(lesson);
        lesson.setSection(null); // Убираем обратную связь
    }
}