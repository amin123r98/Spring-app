package com.example.onlinelearning.config;

import com.example.onlinelearning.model.*; // Импортируем все модели
import com.example.onlinelearning.repository.*; // Импортируем все репозитории
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
// import java.util.concurrent.atomic.AtomicInteger; // Для нумерации уроков/разделов - НЕ ИСПОЛЬЗУЕТСЯ

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    // Добавляем репозитории для новых сущностей
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;


    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ADMIN_EMAIL = "admin@example.com"; // Используем email как username

    @Autowired
    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CourseRepository courseRepository,
                           // Добавляем в конструктор
                           SectionRepository sectionRepository,
                           LessonRepository lessonRepository,
                           AssignmentRepository assignmentRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseRepository = courseRepository;
        // Инициализируем новые поля
        this.sectionRepository = sectionRepository;
        this.lessonRepository = lessonRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional // Важно для корректной работы с несколькими репозиториями и связями
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // 1. Создание ролей
        log.info("Checking and creating roles...");
        createRoleIfNotFound(ROLE_USER);
        Role adminRole = createRoleIfNotFound(ROLE_ADMIN);
        log.info("Role initialization finished.");

        // 2. Создание пользователя-администратора
        log.info("Checking and creating default admin user...");
        User adminUser = createAdminUserIfNotFound(adminRole);
        log.info("Admin user check finished.");

        // 3. Создание курсов, разделов, уроков и заданий
        log.info("Checking and creating sample courses with content...");

        // --- Курс 1: Java ---
        Course javaCourse = createCourseIfNotFound("Введение в Java", "Основы языка Java для начинающих. Познакомьтесь с синтаксисом, переменными, классами и объектами.", adminUser, "https://via.placeholder.com/300x150.png?text=Java+Basics");
        if (javaCourse != null) {
            // Разделы для курса Java
            Section javaSection1 = createSectionIfNotFound("Основы языка", 1, javaCourse);
            Section javaSection2 = createSectionIfNotFound("Объектно-ориентированное программирование", 2, javaCourse);

            if (javaSection1 != null) {
                // Уроки для раздела "Основы языка"
                Lesson javaLesson1_1 = createLessonIfNotFound("Переменные и типы данных", 1, LessonType.TEXT, "Здесь будет текст про переменные, примитивные типы (int, float, boolean) и ссылочные типы.", javaSection1);
                Lesson javaLesson1_2 = createLessonIfNotFound("Управляющие конструкции", 2, LessonType.TEXT, "Изучаем if-else, switch, циклы for, while, do-while.", javaSection1);
                Lesson javaLesson1_3 = createLessonIfNotFound("Вводное видео", 3, LessonType.VIDEO, "dQw4w9WgXcQ", javaSection1); // Пример YouTube ID

                if (javaLesson1_1 != null) {
                    // Задания для урока "Переменные и типы данных"
                    createAssignmentIfNotFound("Тест: Типы данных", "Выберите правильные утверждения о типах данных в Java.", AssignmentType.QUIZ, javaLesson1_1);
                }
                if (javaLesson1_2 != null) {
                    createAssignmentIfNotFound("Практика: Циклы", "Напишите цикл для вывода чисел от 1 до 10.", AssignmentType.PRACTICAL_WORK_TEXT, javaLesson1_2);
                }
            }

            if (javaSection2 != null) {
                // Уроки для раздела "ООП"
                Lesson javaLesson2_1 = createLessonIfNotFound("Классы и объекты", 1, LessonType.TEXT, "Понятия классов, объектов, полей и методов.", javaSection2);
                Lesson javaLesson2_2 = createLessonIfNotFound("Наследование", 2, LessonType.EXTERNAL_RESOURCE, "https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html", javaSection2);
            }
        }

        // --- Курс 2: Spring Boot ---
        Course springCourse = createCourseIfNotFound("Spring Boot Основы", "Создание веб-приложений с использованием Spring Boot. Конфигурация, контроллеры, сервисы, работа с данными.", adminUser, "https://via.placeholder.com/300x150.png?text=Spring+Boot");
        if (springCourse != null) {
            Section springSection1 = createSectionIfNotFound("Начало работы", 1, springCourse);
            if (springSection1 != null) {
                Lesson springLesson1_1 = createLessonIfNotFound("Создание проекта", 1, LessonType.TEXT, "Использование Spring Initializr для создания базового проекта.", springSection1);
                Lesson springLesson1_2 = createLessonIfNotFound("Первый контроллер", 2, LessonType.FILE, "/path/to/your/dummy/presentation.pdf", springSection1); // Пример пути к файлу
                if (springLesson1_2 != null) {
                    createAssignmentIfNotFound("Задание: REST Controller", "Создайте простой REST контроллер.", AssignmentType.PRACTICAL_WORK_UPLOAD, springLesson1_2);
                }
            }
        }

        // --- Курс 3: Docker (без картинки и контента) ---
        createCourseIfNotFound("Изучаем Docker", "Контейнеризация приложений с Docker.", adminUser, null);

        log.info("Data initialization finished.");
    }

    // --- Вспомогательные методы ---

    private Role createRoleIfNotFound(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    log.info("Created role: {}", roleName);
                    return roleRepository.save(newRole);
                });
    }

    private User createAdminUserIfNotFound(Role adminRole) {
        return userRepository.findByUsername(ADMIN_EMAIL) // Ищем по email, который теперь username
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setUsername(ADMIN_EMAIL); // username = email
                    admin.setEmail(ADMIN_EMAIL);
                    admin.setPassword(passwordEncoder.encode("password")); // !!! ИЗМЕНИТЕ НА НАДЕЖНЫЙ ПАРОЛЬ !!!
                    admin.setFirstName("Администратор");
                    admin.setLastName("Сайта");
                    admin.setRoles(Collections.singleton(adminRole));
                    log.info("Created admin user: {}", ADMIN_EMAIL);
                    return userRepository.save(admin);
                });
    }

    private Course createCourseIfNotFound(String title, String description, User author, String imageUrl) {
        // Проверяем по названию (можно сделать проверку надежнее, если нужно)
        Optional<Course> existingCourse = courseRepository.findByTitleContainingIgnoreCase(title).stream()
                .filter(c -> c.getTitle().equalsIgnoreCase(title)) // Точное совпадение после регистронезависимого поиска
                .findFirst();
        if (existingCourse.isEmpty()) {
            Course course = new Course();
            course.setTitle(title);
            course.setDescription(description);
            course.setAuthor(author);
            course.setImageUrl(imageUrl);
            Course savedCourse = courseRepository.save(course);
            log.info("Created course: '{}' (ID: {})", savedCourse.getTitle(), savedCourse.getId());
            return savedCourse;
        } else {
            log.info("Course '{}' already exists.", title);
            return existingCourse.get(); // Возвращаем существующий курс
        }
    }

    private Section createSectionIfNotFound(String title, int order, Course course) {
        // Проверка существования раздела с таким названием ВНУТРИ этого курса
        Optional<Section> existingSection = sectionRepository.findByCourseIdAndTitleIgnoreCase(course.getId(), title);

        if (existingSection.isEmpty()) {
            Section section = new Section();
            section.setTitle(title);
            section.setOrder(order);
            section.setCourse(course); // Связываем с курсом
            Section savedSection = sectionRepository.save(section);
            log.info("Created section: '{}' for course '{}'", savedSection.getTitle(), course.getTitle());
            return savedSection;
        } else {
            log.info("Section '{}' already exists for course '{}'.", title, course.getTitle());
            return existingSection.get();
        }
    }

    private Lesson createLessonIfNotFound(String title, int order, LessonType type, String content, Section section) {
        // Проверка существования урока с таким названием ВНУТРИ этого раздела
        Optional<Lesson> existingLesson = lessonRepository.findBySectionIdOrderByLessonOrderAsc(section.getId()).stream()
                .filter(l -> l.getTitle().equalsIgnoreCase(title))
                .findFirst();

        if (existingLesson.isEmpty()) {
            Lesson lesson = new Lesson();
            lesson.setTitle(title);
            lesson.setLessonOrder(order);
            lesson.setLessonType(type);
            lesson.setContent(content);
            lesson.setSection(section); // Связываем с разделом
            Lesson savedLesson = lessonRepository.save(lesson);
            log.info("Created lesson: '{}' for section '{}'", savedLesson.getTitle(), section.getTitle());
            return savedLesson;
        } else {
            log.info("Lesson '{}' already exists for section '{}'.", title, section.getTitle());
            return existingLesson.get();
        }
    }

    private void createAssignmentIfNotFound(String title, String description, AssignmentType type, Lesson lesson) {
        // Проверка существования задания с таким названием ВНУТРИ этого урока
        boolean exists = assignmentRepository.findByLessonId(lesson.getId()).stream()
                .anyMatch(a -> a.getTitle().equalsIgnoreCase(title));

        if (!exists) {
            Assignment assignment = new Assignment();
            assignment.setTitle(title);
            assignment.setDescription(description);
            assignment.setAssignmentType(type);
            assignment.setLesson(lesson); // Связываем с уроком
            assignmentRepository.save(assignment);
            log.info("Created assignment: '{}' for lesson '{}'", title, lesson.getTitle());
        } else {
            log.info("Assignment '{}' already exists for lesson '{}'.", title, lesson.getTitle());
        }
    }
}
