package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { // <User, Long>

    // Методы для поиска пользователя по уникальным полям
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Методы для проверки существования пользователя по уникальным полям
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}