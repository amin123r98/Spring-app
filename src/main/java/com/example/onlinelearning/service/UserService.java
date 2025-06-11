package com.example.onlinelearning.service;

import com.example.onlinelearning.model.User; // Импортируем нашу модель User
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

// Наследуем UserDetailsService для интеграции с Spring Security
public interface UserService extends UserDetailsService {

    /**
     * Создает (или имитирует создание) токена сброса пароля для пользователя.
     * @param user Пользователь
     * @return Сгенерированный токен (в реальном приложении должен быть сохранен и иметь срок действия).
     */
    String createPasswordResetTokenForUser(User user); // Упрощенный метод

    /**
     * Проверяет валидность токена (имитация).
     * @param token Токен
     * @return Optional с пользователем, если токен валиден.
     */
    Optional<User> getUserByPasswordResetToken(String token); // Упрощенный метод

    /**
     * Обновляет пароль пользователя.
     * @param user Пользователь
     * @param newPassword Новый пароль (незакодированный)
     */
    void changeUserPassword(User user, String newPassword);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user); // Метод для сохранения нового пользователя (регистрация)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Метод loadUserByUsername(String username) будет унаследован от UserDetailsService
}