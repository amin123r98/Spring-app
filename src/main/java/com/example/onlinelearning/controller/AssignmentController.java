package com.example.onlinelearning.controller;

import com.example.onlinelearning.model.*;
import com.example.onlinelearning.repository.UserAssignmentResultRepository;
import com.example.onlinelearning.service.AssignmentService;
import com.example.onlinelearning.service.ProgressService;
import com.example.onlinelearning.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private static final Logger log = LoggerFactory.getLogger(AssignmentController.class);
    // Директория для сохранения загруженных файлов (нужно создать!)
    private static final String UPLOAD_DIR = "./uploads/";

    private final AssignmentService assignmentService;
    private final UserAssignmentResultRepository resultRepository;
    private final UserService userService;
    private final ProgressService progressService; // <-- Добавили сервис прогресса

    @Autowired
    public AssignmentController(AssignmentService assignmentService,
                                UserAssignmentResultRepository resultRepository,
                                UserService userService,
                                ProgressService progressService) { // <-- Добавили в конструктор
        this.assignmentService = assignmentService;
        this.resultRepository = resultRepository;
        this.userService = userService;
        this.progressService = progressService; // <-- Инициализировали
        // Создаем директорию uploads при запуске, если ее нет
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", UPLOAD_DIR);
            } catch (IOException e) {
                log.error("Could not create upload directory: {}", UPLOAD_DIR, e);
            }
        }
    }

    @GetMapping("/{id}")
    public String viewAssignment(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("Fetching assignment view for id: {}", id);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User not authenticated. Redirecting to login.");
            return "redirect:/login";
        }

        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentByIdWithDetails(id);

        if (assignmentOpt.isPresent()) {
            Assignment assignment = assignmentOpt.get();
            model.addAttribute("assignment", assignment);
            model.addAttribute("lesson", assignment.getLesson());
            model.addAttribute("section", assignment.getLesson().getSection());
            model.addAttribute("course", assignment.getLesson().getSection().getCourse());

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isPresent()) {
                Optional<UserAssignmentResult> previousResultOpt = resultRepository.findByUserIdAndAssignmentId(userOpt.get().getId(), id);
                model.addAttribute("previousResult", previousResultOpt.orElse(null));
                model.addAttribute("userAnswer", new UserAnswerDto());

            } else {
                log.error("Could not find user details for authenticated user: {}", username);
                redirectAttributes.addFlashAttribute("errorMessage", "Error finding user details.");
                return "redirect:/";
            }

            log.info("Assignment found: {}. Returning assignment-view.", assignment.getTitle());
            return "assignment-view";
        } else {
            log.warn("Assignment with id {} not found. Redirecting back to courses.", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Assignment with id " + id + " not found.");
            return "redirect:/courses";
        }
    }

    @PostMapping("/{id}/submit")
    public String submitAssignmentAnswer(@PathVariable Long id,
                                         @ModelAttribute("userAnswer") UserAnswerDto userAnswer,
                                         @RequestParam(value = "file", required = false) MultipartFile file,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        log.info("Received submission for assignment id: {}", id);

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentByIdWithDetails(id);
        Optional<User> userOpt = userService.findByUsername(authentication.getName());

        if (assignmentOpt.isPresent() && userOpt.isPresent()) {
            Assignment assignment = assignmentOpt.get();
            User user = userOpt.get();

            UserAssignmentResult result = resultRepository.findByUserIdAndAssignmentId(user.getId(), assignment.getId())
                    .orElse(new UserAssignmentResult(user, assignment, null));

            String answerContent = null;

            if (assignment.getAssignmentType() == AssignmentType.PRACTICAL_WORK_UPLOAD) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String fileExtension = "";
                        if (originalFilename != null && originalFilename.contains(".")) {
                            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        }
                        String storedFilename = UUID.randomUUID().toString() + fileExtension;
                        Path filePath = Paths.get(UPLOAD_DIR + storedFilename);

                        Files.copy(file.getInputStream(), filePath);
                        answerContent = storedFilename;
                        log.info("File uploaded successfully: {}", storedFilename);

                        if (result.getAnswer() != null && !result.getAnswer().equals(answerContent)) {
                            try {
                                Files.deleteIfExists(Paths.get(UPLOAD_DIR + result.getAnswer()));
                                log.info("Deleted old file: {}", result.getAnswer());
                            } catch (IOException e) {
                                log.warn("Could not delete old file: {}", result.getAnswer(), e);
                            }
                        }

                    } catch (IOException e) {
                        log.error("Failed to upload file for assignment {}", id, e);
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке файла.");
                        return "redirect:/assignments/" + id;
                    }
                } else if (result.getId() == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Необходимо прикрепить файл для этого задания.");
                    return "redirect:/assignments/" + id;
                } else {
                    answerContent = result.getAnswer();
                }
            } else if (assignment.getAssignmentType() == AssignmentType.PRACTICAL_WORK_TEXT) {
                answerContent = userAnswer.getTextAnswer();
            } else {
                answerContent = "[QUIZ_SUBMITTED]";
            }

            result.setAnswer(answerContent);
            result.setSubmittedAt(LocalDateTime.now());
            result.setGrade(null);
            result.setGradedAt(null);

            try {
                resultRepository.save(result);
                // Обновляем прогресс курса после сдачи задания
                progressService.updateUserCourseProgress(user.getId(), assignment.getLesson().getSection().getCourse().getId());
                log.info("Saved assignment result and updated progress for user {} and assignment {}", user.getUsername(), assignment.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Ваш ответ на задание '" + assignment.getTitle() + "' успешно отправлен!");
            } catch (Exception e) {
                log.error("Error saving assignment result", e);
                redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при сохранении вашего ответа.");
            }

            return "redirect:/lessons/" + assignment.getLesson().getId();

        } else {
            log.warn("Assignment or User not found during submission. Assignment ID: {}, User: {}", id, authentication.getName());
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось найти задание или пользователя для отправки ответа.");
            return "redirect:/courses";
        }
    }

    @PostMapping("/{id}/grade")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String gradeAssignment(@PathVariable Long id,
                                  @RequestParam Long resultId,
                                  @RequestParam(required = false) Integer grade,
                                  RedirectAttributes redirectAttributes) {
        log.info("Attempting to grade assignment result {} for assignment {}", resultId, id);

        Optional<UserAssignmentResult> resultOpt = resultRepository.findById(resultId);

        if (resultOpt.isPresent()) {
            UserAssignmentResult result = resultOpt.get();
            if (!result.getAssignment().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: Результат не соответствует заданию.");
                return "redirect:/assignments/" + id;
            }

            result.setGrade(grade);
            result.setGradedAt(LocalDateTime.now());
            try {
                resultRepository.save(result);
                // Обновляем прогресс курса после выставления оценки
                progressService.updateUserCourseProgress(result.getUser().getId(), result.getAssignment().getLesson().getSection().getCourse().getId());
                redirectAttributes.addFlashAttribute("successMessage", "Оценка успешно сохранена.");
                log.info("Grade {} set for result {} and progress updated.", grade, resultId);

            } catch (Exception e) {
                log.error("Error saving grade for result {}", resultId, e);
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении оценки.");
            }
        } else {
            log.warn("Assignment result not found for grading: {}", resultId);
            redirectAttributes.addFlashAttribute("errorMessage", "Результат для оценки не найден.");
        }

        return "redirect:/assignments/" + id;
    }

    public static class UserAnswerDto {
        private String textAnswer;
        public String getTextAnswer() { return textAnswer; }
        public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
    }
}