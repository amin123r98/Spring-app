package com.example.onlinelearning.model;

import jakarta.persistence.*;
// Заменяем импорты List/ArrayList на Set/LinkedHashSet
// import java.util.ArrayList;
// import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob // Для хранения больших текстов (может быть CLOB или TEXT в базе данных)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String imageUrl; // Путь к изображению обложки

    // Связь Многие-к-Одному с пользователем (автором)
    @ManyToOne(fetch = FetchType.LAZY) // LAZY - автор загружается только при обращении к нему
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Связь Один-ко-Многим с разделами
    // Заменяем List на Set (используем LinkedHashSet для сохранения порядка)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("order ASC") // Убедитесь, что поле в Section называется 'order'
    private Set<Section> sections = new LinkedHashSet<>(); // Инициализируем как LinkedHashSet

    // Конструкторы
    public Course() {
    }

    public Course(String title, String description, String imageUrl, User author) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.author = author;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    // Геттер и Сеттер для sections теперь используют Set
    public Set<Section> getSections() {
        return sections;
    }

    public void setSections(Set<Section> sections) {
        this.sections = sections;
    }

    // Методы добавления/удаления (логика остается прежней, т.к. Set имеет add/remove)
    public void addSection(Section section) {
        this.sections.add(section);
        section.setCourse(this); // Устанавливаем обратную связь
    }

    public void removeSection(Section section) {
        this.sections.remove(section);
        section.setCourse(null); // Убираем обратную связь
    }
}