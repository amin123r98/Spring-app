package com.example.onlinelearning.config;

import com.example.onlinelearning.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Важно для @PreAuthorize в контроллерах
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                // --- РАЗРЕШАЕМ ДОСТУП ДЛЯ ВСЕХ ---
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/register"),
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/forgot-password"),
                                new AntPathRequestMatcher("/reset-password"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/h2-console/**")
                        ).permitAll()

                        .requestMatchers(
                                // --- Эндпоинты для Админа (управление контентом) ---
                                new AntPathRequestMatcher("/admin/**"), // Доступ к админ-панели
                                new AntPathRequestMatcher("/courses/new"),
                                new AntPathRequestMatcher("/courses", "POST"),
                                new AntPathRequestMatcher("/courses/{id}/edit"),
                                new AntPathRequestMatcher("/courses/{id}", "POST"),
                                new AntPathRequestMatcher("/courses/{id}/delete", "POST"),
                                new AntPathRequestMatcher("/courses/{courseId}/sections", "POST"),
                                new AntPathRequestMatcher("/sections/{sectionId}/edit"),
                                new AntPathRequestMatcher("/sections/{sectionId}/delete", "POST"),
                                new AntPathRequestMatcher("/sections/{sectionId}/lessons/new"),
                                new AntPathRequestMatcher("/sections/{sectionId}/lessons", "POST"),
                                new AntPathRequestMatcher("/lessons/{lessonId}/edit"),
                                new AntPathRequestMatcher("/lessons/{lessonId}/delete", "POST"),
                                new AntPathRequestMatcher("/assignments/{id}/grade", "POST")
                        ).hasRole("ADMIN")

                        // --- Все остальные запросы требуют аутентификации ---
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll() // Страница логина доступна всем
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}