package com.example.onlinelearning.service;

import com.example.onlinelearning.dto.CourseDto; // Убедитесь, что импорт есть
import com.example.onlinelearning.model.User;   // Убедитесь, что импорт есть
import com.example.onlinelearning.model.Course;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для бизнес-логики, связанной с курсами.
 */
public interface CourseService {

    /**
     * Получает список всех курсов.
     * @return List<Course> список всех курсов.
     */
    List<Course> findAllCourses();

    /**
     * Находит курс по его идентификатору.
     * @param id Идентификатор курса.
     * @return Optional<Course>, содержащий курс, если найден, иначе пустой.
     */
    Optional<Course> findCourseById(Long id);

    /**
     * Сохраняет новый курс.
     * @param courseDto DTO с данными курса.
     * @param author Пользователь, создающий курс.
     * @return Сохраненный объект Course.
     */
    Course saveCourse(CourseDto courseDto, User author);

    /**
     * Обновляет существующий курс. // <-- Убедитесь, что эта сигнатура есть!
     * @param id ID курса для обновления.
     * @param courseDto DTO с новыми данными курса.
     * @return Обновленный объект Course.
     * @throws RuntimeException если курс с таким ID не найден или другие ошибки.
     */
    Course updateCourse(Long id, CourseDto courseDto); // <-- Вот эта строка должна быть!

    // TODO: Добавить сигнатуры для удаления
    // void deleteCourse(Long id);
    // ... (findAllCourses, findCourseById, saveCourse, updateCourse) ...

    /**
     * Удаляет курс по его ID.
     * @param id ID курса для удаления.
     * @throws RuntimeException если курс не найден или возникает другая ошибка.
     */
    void deleteCourse(Long id); // Добавляем этот метод
}