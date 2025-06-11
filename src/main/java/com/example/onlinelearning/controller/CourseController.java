package com.example.onlinelearning.controller;

import com.example.onlinelearning.dto.CourseDto;
// Добавляем импорт SectionDto
import com.example.onlinelearning.dto.SectionDto;
import com.example.onlinelearning.model.Course;
import com.example.onlinelearning.model.User;
import com.example.onlinelearning.service.CourseService;
import com.example.onlinelearning.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    // --- Отображение списка курсов ---
    @GetMapping
    public String listCourses(Model model,
                              @RequestParam(required = false) String error,
                              @RequestParam(required = false) String message) {
        log.info("Fetching all courses");
        List<Course> courses = courseService.findAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", "courses");

        if (error != null) {
            model.addAttribute("errorMessage", error.replace('+', ' '));
        }
        if (message != null) {
            model.addAttribute("successMessage", message.replace('+', ' '));
        }

        log.info("Returning course-list view with {} courses", courses.size());
        return "course-list";
    }

    // --- Отображение деталей курса ---
    @GetMapping("/{id}")
    public String courseDetails(@PathVariable Long id, Model model) {
        log.info("Fetching course details for id: {}", id);
        Optional<Course> courseOpt = courseService.findCourseById(id);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            model.addAttribute("course", course);
            model.addAttribute("currentPage", "courses");

            // ИЗМЕНЕНИЕ: Всегда добавляем DTO для формы добавления раздела
            // Проверяем, нет ли его уже из flash-атрибутов (после редиректа с ошибкой)
            if (!model.containsAttribute("newSectionDto")) {
                model.addAttribute("newSectionDto", new SectionDto());
            }
            // Удаляем старый флаг, он больше не нужен
            // model.addAttribute("errorSectionId", null); // Или просто не добавлять

            log.info("Course found: {}. Returning course-details view.", course.getTitle());
            return "course-details";
        } else {
            log.warn("Course with id {} not found. Redirecting to course list.", id);
            return "redirect:/courses?error=Course+with+id+" + id + "+not+found";
        }
    }

    // --- Отображение формы создания курса ---
    @GetMapping("/new")
    public String showCreateCourseForm(Model model) {
        log.debug("Displaying new course form");
        model.addAttribute("courseDto", new CourseDto());
        model.addAttribute("isEditMode", false);
        model.addAttribute("currentPage", "courses");
        return "course-form";
    }

    // --- Обработка создания курса ---
    @PostMapping
    public String createCourse(@Valid @ModelAttribute("courseDto") CourseDto courseDto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        log.info("Attempting to create new course with title: {}", courseDto.getTitle());
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors present when creating course: {}", bindingResult.getAllErrors());
            model.addAttribute("isEditMode", false);
            model.addAttribute("currentPage", "courses");
            return "course-form";
        }

        String username = authentication.getName();
        Optional<User> authorOpt = userService.findByUsername(username);

        if (authorOpt.isEmpty()) {
            log.error("Authenticated user '{}' not found in database.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: не удалось найти данные автора.");
            return "redirect:/courses";
        }

        try {
            Course savedCourse = courseService.saveCourse(courseDto, authorOpt.get());
            log.info("Successfully created course '{}' with id {}", savedCourse.getTitle(), savedCourse.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Курс '" + savedCourse.getTitle() + "' успешно создан!");
            return "redirect:/courses";
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage(), e);
            bindingResult.reject("saveError", "Произошла ошибка при сохранении курса: " + e.getMessage());
            model.addAttribute("isEditMode", false);
            model.addAttribute("currentPage", "courses");
            return "course-form";
        }
    }

    // --- Отображение формы редактирования курса ---
    @GetMapping("/{id}/edit")
    public String showEditCourseForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Displaying edit course form for id: {}", id);
        Optional<Course> courseOpt = courseService.findCourseById(id);

        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            CourseDto courseDto = new CourseDto();
            courseDto.setTitle(course.getTitle());
            courseDto.setDescription(course.getDescription());
            courseDto.setImageUrl(course.getImageUrl());

            model.addAttribute("courseDto", courseDto);
            model.addAttribute("courseId", id);
            model.addAttribute("isEditMode", true);
            model.addAttribute("currentPage", "courses");
            return "course-form";
        } else {
            log.warn("Course with id {} not found for editing.", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Курс с ID " + id + " не найден.");
            return "redirect:/courses";
        }
    }

    // --- Обработка редактирования курса ---
    @PostMapping("/{id}")
    public String updateCourse(@PathVariable Long id,
                               @Valid @ModelAttribute("courseDto") CourseDto courseDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        log.info("Attempting to update course with id: {}", id);
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors present when updating course {}: {}", id, bindingResult.getAllErrors());
            model.addAttribute("courseId", id);
            model.addAttribute("isEditMode", true);
            model.addAttribute("currentPage", "courses");
            return "course-form";
        }

        try {
            Course updatedCourse = courseService.updateCourse(id, courseDto);
            log.info("Successfully updated course '{}' with id {}", updatedCourse.getTitle(), updatedCourse.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Курс '" + updatedCourse.getTitle() + "' успешно обновлен!");
            return "redirect:/courses/" + id;
        } catch (EntityNotFoundException e) {
            log.error("Error updating course {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses";
        } catch (Exception e) {
            log.error("Error updating course {}: {}", id, e.getMessage(), e);
            bindingResult.reject("saveError", "Ошибка при обновлении курса: " + e.getMessage());
            model.addAttribute("courseId", id);
            model.addAttribute("isEditMode", true);
            model.addAttribute("currentPage", "courses");
            return "course-form";
        }
    }

    // --- Удаление курса ---
    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Attempting to delete course with id: {}", id);
        try {
            courseService.deleteCourse(id);
            log.info("Successfully deleted course with id: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Курс (ID: " + id + ") успешно удален!");
        } catch (EntityNotFoundException e) {
            log.error("Failed to delete course. Course not found with id: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить: Курс с ID " + id + " не найден.");
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete course with id {}. Possible dependencies.", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить курс (ID: " + id + "). Возможно, с ним связаны другие данные, которые не были удалены автоматически.");
        } catch (Exception e) {
            log.error("Error deleting course with id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при удалении курса (ID: " + id + ").");
        }
        return "redirect:/courses";
    }
}