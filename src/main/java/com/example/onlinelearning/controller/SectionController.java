// src/main/java/com/example/onlinelearning/controller/SectionController.java
package com.example.onlinelearning.controller;

import com.example.onlinelearning.dto.SectionDto;
import com.example.onlinelearning.model.Course;
import com.example.onlinelearning.model.Section;
// Добавляем нужные импорты
import com.example.onlinelearning.service.CourseService;
import com.example.onlinelearning.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Добавлен Model
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
// Убрали @RequestMapping("/sections"), т.к. пути теперь включают /courses/{courseId}/sections
@PreAuthorize("hasRole('ROLE_ADMIN')") // Весь контроллер только для админов
public class SectionController {
    private static final Logger log = LoggerFactory.getLogger(SectionController.class);

    private final SectionService sectionService;
    private final CourseService courseService; // Нужен для получения курса

    @Autowired
    public SectionController(SectionService sectionService, CourseService courseService) {
        this.sectionService = sectionService;
        this.courseService = courseService;
    }

    /**
     * Обрабатывает создание нового раздела для курса.
     */
    @PostMapping("/courses/{courseId}/sections")
    public String addSectionToCourse(@PathVariable Long courseId,
                                     // ИЗМЕНЕНИЕ: Используем имя "newSectionDto"
                                     @Valid @ModelAttribute("newSectionDto") SectionDto sectionDto,
                                     BindingResult bindingResult, // Ассоциирован с newSectionDto
                                     RedirectAttributes redirectAttributes) {
        log.info("Attempting to add section to course id {}", courseId);

        // Находим курс, к которому добавляем раздел (делаем это позже, если нет ошибок валидации)


        if (bindingResult.hasErrors()) {
            log.warn("Validation errors adding section: {}", bindingResult.getAllErrors());
            // Передаем ошибки и DTO обратно через flash атрибуты
            // Ключ для BindingResult должен быть 'org.springframework.validation.BindingResult.' + имя атрибута модели
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newSectionDto", bindingResult);
            redirectAttributes.addFlashAttribute("newSectionDto", sectionDto); // Используем то же имя
            // redirectAttributes.addFlashAttribute("errorSectionId", "new"); // Этот флаг больше не нужен

            return "redirect:/courses/" + courseId; // Возвращаемся на страницу деталей курса
        }

        // Если валидация прошла, находим курс и сохраняем раздел
        try {
            Course course = courseService.findCourseById(courseId)
                    .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
            sectionService.saveSection(sectionDto, course);
            redirectAttributes.addFlashAttribute("successMessage", "Раздел '" + sectionDto.getTitle() + "' успешно добавлен!");
        } catch (EntityNotFoundException e) {
            log.error("Course not found {}", courseId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: Курс не найден.");
        } catch (Exception e) {
            log.error("Error adding section to course {}", courseId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении раздела: " + e.getMessage());
            // Если ошибка после валидации, можно вернуть DTO для информации
            redirectAttributes.addFlashAttribute("newSectionDto", sectionDto);
        }

        return "redirect:/courses/" + courseId; // Возвращаемся на страницу деталей курса
    }

    /**
     * Обрабатывает удаление раздела.
     */
    @PostMapping("/sections/{sectionId}/delete")
    public String deleteSection(@PathVariable Long sectionId,
                                @RequestParam Long courseId, // ID курса нужен для редиректа
                                RedirectAttributes redirectAttributes) {
        log.warn("Attempting to delete section id {}", sectionId);
        try {
            sectionService.deleteSection(sectionId);
            redirectAttributes.addFlashAttribute("successMessage", "Раздел успешно удален.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить: Раздел не найден.");
        } catch (Exception e) {
            log.error("Error deleting section {}", sectionId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении раздела: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId; // Возвращаемся на страницу курса
    }

    /**
     * Отображает форму редактирования раздела.
     */
    @GetMapping("/sections/{sectionId}/edit")
    public String showEditSectionForm(@PathVariable Long sectionId, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Showing edit form for section id {}", sectionId);
        Optional<Section> sectionOpt = sectionService.findById(sectionId);

        if (sectionOpt.isPresent()) {
            Section section = sectionOpt.get();
            // Создаем DTO из существующего раздела
            SectionDto sectionDto = new SectionDto();
            sectionDto.setTitle(section.getTitle());
            sectionDto.setOrder(section.getOrder());

            model.addAttribute("sectionDto", sectionDto); // Имя объекта для формы редактирования
            model.addAttribute("sectionId", sectionId);
            model.addAttribute("courseId", section.getCourse().getId());
            model.addAttribute("currentPage", "courses"); // Добавляем для навбара
            return "section-form"; // Имя шаблона для формы раздела
        } else {
            log.warn("Section id {} not found for editing.", sectionId);
            redirectAttributes.addFlashAttribute("errorMessage", "Раздел с ID " + sectionId + " не найден.");
            return "redirect:/"; // Редирект на главную
        }
    }

    /**
     * Обрабатывает обновление раздела.
     */
    @PostMapping("/sections/{sectionId}/edit")
    public String updateSection(@PathVariable Long sectionId,
                                @Valid @ModelAttribute("sectionDto") SectionDto sectionDto, // Имя объекта совпадает с формой
                                BindingResult bindingResult,
                                @RequestParam Long courseId, // ID курса для редиректа
                                RedirectAttributes redirectAttributes,
                                Model model) {

        log.info("Attempting to update section id {}", sectionId);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors updating section {}: {}", sectionId, bindingResult.getAllErrors());
            model.addAttribute("sectionId", sectionId);
            model.addAttribute("courseId", courseId);
            model.addAttribute("currentPage", "courses"); // Добавляем для навбара
            // Атрибут sectionDto уже в модели из-за @ModelAttribute
            return "section-form"; // Возвращаемся на форму редактирования
        }

        try {
            sectionService.updateSection(sectionId, sectionDto);
            redirectAttributes.addFlashAttribute("successMessage", "Раздел '" + sectionDto.getTitle() + "' успешно обновлен!");
            return "redirect:/courses/" + courseId; // Возвращаемся на страницу курса
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка обновления: Раздел не найден.");
            return "redirect:/courses/" + courseId;
        } catch (Exception e) {
            log.error("Error updating section {}", sectionId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении раздела: " + e.getMessage());
            model.addAttribute("sectionId", sectionId);
            model.addAttribute("courseId", courseId);
            model.addAttribute("currentPage", "courses"); // Добавляем для навбара
            return "section-form"; // Возвращаемся на форму при других ошибках
        }
    }
}