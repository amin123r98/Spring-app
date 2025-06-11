// Откройте файл: src/main/java/com/example/onlinelearning/controller/LessonController.java
package com.example.onlinelearning.controller;

import com.example.onlinelearning.dto.LessonDto; // <--- Импорт DTO
import com.example.onlinelearning.model.Lesson;
import com.example.onlinelearning.model.LessonType; // <--- Импорт LessonType
import com.example.onlinelearning.model.Section;   // <--- Импорт Section
import com.example.onlinelearning.service.LessonService;
import com.example.onlinelearning.service.SectionService; // <--- Импорт SectionService
import jakarta.persistence.EntityNotFoundException; // <--- Импорт
import jakarta.validation.Valid; // <--- Импорт
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // <--- Импорт
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // <--- Импорт
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
// Уберем @RequestMapping("/lessons"), т.к. пути будут включать ID раздела или урока
public class LessonController {

    private static final Logger log = LoggerFactory.getLogger(LessonController.class);
    private final LessonService lessonService;
    private final SectionService sectionService; // <--- Добавляем зависимость

    @Autowired
    public LessonController(LessonService lessonService, SectionService sectionService) { // <--- Обновляем конструктор
        this.lessonService = lessonService;
        this.sectionService = sectionService;
    }

    // --- ПРОСМОТР УРОКА (без изменений) ---
    @GetMapping("/lessons/{id}")
    public String viewLesson(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Fetching lesson view for id: {}", id);
        Optional<Lesson> lessonOpt = lessonService.findLessonByIdWithContext(id);

        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            model.addAttribute("lesson", lesson);
            model.addAttribute("course", lesson.getSection().getCourse());
            model.addAttribute("currentPage", "courses"); // Для подсветки навбара
            log.info("Lesson found: {}. Returning lesson-view.", lesson.getTitle());
            return "lesson-view";
        } else {
            log.warn("Lesson with id {} not found. Redirecting back.", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Lesson with id " + id + " not found.");
            return "redirect:/courses";
        }
    }

    // --- СОЗДАНИЕ УРОКА ---

    /**
     * Отображает форму создания нового урока для указанного раздела.
     */
    @GetMapping("/sections/{sectionId}/lessons/new")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showCreateLessonForm(@PathVariable Long sectionId, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Showing create lesson form for section id {}", sectionId);
        Optional<Section> sectionOpt = sectionService.findById(sectionId);

        if (sectionOpt.isEmpty()) {
            log.error("Section with id {} not found when trying to create lesson.", sectionId);
            redirectAttributes.addFlashAttribute("errorMessage", "Раздел для добавления урока не найден.");
            return "redirect:/courses"; // Или на страницу курса, если бы у нас был courseId
        }

        model.addAttribute("lessonDto", new LessonDto());
        model.addAttribute("sectionId", sectionId);
        model.addAttribute("courseId", sectionOpt.get().getCourse().getId()); // Для хлебных крошек и отмены
        model.addAttribute("isEditMode", false);
        model.addAttribute("lessonTypes", LessonType.values()); // Передаем все типы уроков
        model.addAttribute("currentPage", "courses");
        return "lesson-form"; // Имя шаблона формы
    }

    /**
     * Обрабатывает создание нового урока в разделе.
     */
    @PostMapping("/sections/{sectionId}/lessons")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createLesson(@PathVariable Long sectionId,
                               @Valid @ModelAttribute("lessonDto") LessonDto lessonDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        log.info("Attempting to create lesson in section id {}", sectionId);
        Optional<Section> sectionOpt = sectionService.findById(sectionId);

        if (sectionOpt.isEmpty()) {
            log.error("Section with id {} not found during lesson creation.", sectionId);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: Раздел для добавления урока не найден.");
            return "redirect:/courses"; // Редирект на список курсов
        }

        Section section = sectionOpt.get();
        Long courseId = section.getCourse().getId(); // Получаем ID курса для редиректа

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors creating lesson: {}", bindingResult.getAllErrors());
            model.addAttribute("sectionId", sectionId);
            model.addAttribute("courseId", courseId);
            model.addAttribute("isEditMode", false);
            model.addAttribute("lessonTypes", LessonType.values());
            model.addAttribute("currentPage", "courses");
            // lessonDto уже в модели из-за @ModelAttribute
            return "lesson-form"; // Возвращаем на форму
        }

        try {
            lessonService.saveLesson(lessonDto, section);
            redirectAttributes.addFlashAttribute("successMessage", "Урок '" + lessonDto.getTitle() + "' успешно добавлен!");
            return "redirect:/courses/" + courseId; // Редирект на страницу деталей курса
        } catch (Exception e) {
            log.error("Error creating lesson in section {}: {}", sectionId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании урока: " + e.getMessage());
            // Добавляем DTO обратно, чтобы не терять введенные данные
            redirectAttributes.addFlashAttribute("lessonDto", lessonDto);
            return "redirect:/sections/" + sectionId + "/lessons/new"; // Редирект обратно на форму создания
        }
    }


    // --- РЕДАКТИРОВАНИЕ УРОКА ---

    /**
     * Отображает форму редактирования урока.
     */
    @GetMapping("/lessons/{lessonId}/edit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showEditLessonForm(@PathVariable Long lessonId, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Showing edit form for lesson id {}", lessonId);
        Optional<Lesson> lessonOpt = lessonService.findById(lessonId); // Используем простой findById

        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            // Создаем DTO из существующего урока
            LessonDto lessonDto = new LessonDto();
            lessonDto.setTitle(lesson.getTitle());
            lessonDto.setLessonType(lesson.getLessonType());
            lessonDto.setContent(lesson.getContent());
            lessonDto.setLessonOrder(lesson.getLessonOrder());

            model.addAttribute("lessonDto", lessonDto);
            model.addAttribute("lessonId", lessonId);
            model.addAttribute("sectionId", lesson.getSection().getId()); // Для информации и возможного использования
            model.addAttribute("courseId", lesson.getSection().getCourse().getId()); // Для отмены/хлебных крошек
            model.addAttribute("isEditMode", true);
            model.addAttribute("lessonTypes", LessonType.values());
            model.addAttribute("currentPage", "courses");
            return "lesson-form";
        } else {
            log.warn("Lesson id {} not found for editing.", lessonId);
            redirectAttributes.addFlashAttribute("errorMessage", "Урок с ID " + lessonId + " не найден.");
            return "redirect:/courses"; // Редирект на список курсов
        }
    }

    /**
     * Обрабатывает обновление урока.
     */
    @PostMapping("/lessons/{lessonId}/edit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String updateLesson(@PathVariable Long lessonId,
                               @Valid @ModelAttribute("lessonDto") LessonDto lessonDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        log.info("Attempting to update lesson id {}", lessonId);

        // Найдем ID курса для редиректа заранее, если урок существует
        Long courseId = lessonService.findById(lessonId)
                .map(l -> l.getSection().getCourse().getId())
                .orElse(null);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors updating lesson {}: {}", lessonId, bindingResult.getAllErrors());
            model.addAttribute("lessonId", lessonId);
            model.addAttribute("courseId", courseId); // Передаем courseId обратно
            model.addAttribute("isEditMode", true);
            model.addAttribute("lessonTypes", LessonType.values());
            model.addAttribute("currentPage", "courses");
            // lessonDto уже в модели
            return "lesson-form"; // Возвращаемся на форму
        }

        try {
            Lesson updatedLesson = lessonService.updateLesson(lessonId, lessonDto);
            redirectAttributes.addFlashAttribute("successMessage", "Урок '" + updatedLesson.getTitle() + "' успешно обновлен!");
            // Редирект на страницу курса
            return "redirect:/courses/" + updatedLesson.getSection().getCourse().getId();
        } catch (EntityNotFoundException e) {
            log.error("Lesson not found during update process, id: {}", lessonId);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка обновления: Урок не найден.");
            return "redirect:/courses"; // Редирект на список курсов
        } catch (Exception e) {
            log.error("Error updating lesson {}", lessonId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении урока: " + e.getMessage());
            // При ошибке лучше редиректить обратно на форму редактирования с данными
            redirectAttributes.addFlashAttribute("lessonDto", lessonDto); // Сохраняем введенные данные
            return "redirect:/lessons/" + lessonId + "/edit";
        }
    }

    // --- УДАЛЕНИЕ УРОКА ---

    /**
     * Обрабатывает удаление урока.
     */
    @PostMapping("/lessons/{lessonId}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteLesson(@PathVariable Long lessonId,
                               // Получим courseId из урока перед удалением для редиректа
                               RedirectAttributes redirectAttributes) {
        log.warn("Attempting to delete lesson id {}", lessonId);

        // Найдем урок и ID его курса ПЕРЕД удалением
        Optional<Lesson> lessonOpt = lessonService.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить: Урок не найден.");
            return "redirect:/courses";
        }
        Long courseId = lessonOpt.get().getSection().getCourse().getId();

        try {
            lessonService.deleteLesson(lessonId);
            redirectAttributes.addFlashAttribute("successMessage", "Урок успешно удален.");
        } catch (EntityNotFoundException e) {
            // Этот блок может быть избыточен из-за проверки выше, но оставим на всякий случай
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить: Урок не найден.");
        } catch (Exception e) {
            log.error("Error deleting lesson {}", lessonId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении урока: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId; // Возвращаемся на страницу курса
    }
}