package com.example.onlinelearning.service;

import com.example.onlinelearning.model.Role;
import com.example.onlinelearning.model.User;
import com.example.onlinelearning.repository.RoleRepository; // Импортируем RoleRepository
import com.example.onlinelearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections; // Для Collections.singleton()
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap; // Для имитации хранения токенов
import java.util.Map;    // Для имитации хранения токенов
import java.util.UUID;
@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER"; // Константа для имени роли по умолчанию

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Добавляем репозиторий ролей
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> passwordResetTokens = new HashMap<>();
    @Override
    public String createPasswordResetTokenForUser(User user) {
        // Генерируем простой уникальный токен
        String token = UUID.randomUUID().toString();
        // Сохраняем связь токен -> имя пользователя (в реальном приложении - в БД с таймстемпом)
        passwordResetTokens.put(token, user.getUsername());
        // TODO: В реальном приложении здесь должна быть отправка email с ссылкой /reset-password?token=...
        System.out.println("!!! Password reset token for " + user.getUsername() + ": " + token + " !!!"); // Выводим в консоль для теста
        return token;
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        // Ищем имя пользователя по токену в нашей карте
        String username = passwordResetTokens.get(token);
        if (username != null) {
            // Если нашли, ищем пользователя в репозитории
            // TODO: В реальном приложении проверять срок действия токена
            return userRepository.findByUsername(username);
        }
        return Optional.empty();
    }

    @Override
    @Transactional // Метод изменяет данные
    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword)); // Кодируем новый пароль
        userRepository.save(user); // Сохраняем пользователя
        // Удаляем использованный токен из карты
        passwordResetTokens.entrySet().removeIf(entry -> entry.getValue().equals(user.getUsername()));
        System.out.println("!!! Password changed successfully for " + user.getUsername() + " !!!");
    }

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository, // Добавляем в конструктор
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository; // Инициализируем
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(user.getRoles());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        // Важно проверить на null или пустоту, если связь ленивая и не была загружена
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        // 1. Кодируем пароль
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Находим роль по умолчанию
        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RuntimeException("Error: Role " + DEFAULT_ROLE + " is not found.")); // Должна быть создана инициализатором!

        // 3. Добавляем роль пользователю (убедись, что в User.java поле roles инициализировано, например, new HashSet<>())
        user.setRoles(Collections.singleton(userRole)); // Используем Set с одной ролью

        // 4. Сохраняем пользователя
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}