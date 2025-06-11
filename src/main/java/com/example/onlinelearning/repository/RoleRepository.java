package com.example.onlinelearning.repository;

import com.example.onlinelearning.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Помечаем интерфейс как Spring компонент репозитория
public interface RoleRepository extends JpaRepository<Role, Long> { // <Role, Long> - тип сущности и тип её ID

    // Spring Data JPA автоматически создаст метод для поиска роли по имени
    Optional<Role> findByName(String name);
}