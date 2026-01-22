package com.community.cms.infrastructure.config;

import com.community.cms.domain.model.people.User;
import com.community.cms.domain.service.people.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Конфигурация безопасности Spring Security для Community CMS.
 *
 * <p>Этот класс настраивает систему аутентификации и авторизации приложения,
 * определяет правила доступа к URL, настраивает форму входа и обработку выхода.</p>
 *
 * <p>Основные функции конфигурации:
 * <ul>
 *   <li>Защита административных URL от несанкционированного доступа</li>
 *   <li>Настройка кастомной формы аутентификации</li>
 *   <li>Управление ролями и правами пользователей</li>
 *   <li>Обработка выхода из системы</li>
 *   <li>Настройка исключений безопасности</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @since 2025
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ✅ Включаем поддержку @PreAuthorize
public class SecurityConfig {

    /**
     * Настраивает цепочку фильтров безопасности для HTTP запросов.
     *
     * <p>Определяет правила доступа к различным URL приложения, настраивает
     * форму входа, обработку выхода и исключения безопасности.</p>
     *
     * @param http объект для настройки веб-безопасности Spring Security
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception если произошла ошибка в процессе конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //
                // AUTHORIZATION CONFIGURATION / КОНФИГУРАЦИЯ АВТОРИЗАЦИИ
                // Define URL access rules / Определяем правила доступа к URL
                //
                .authorizeHttpRequests(authorize -> authorize
                        //
                        // PUBLIC ENDPOINTS / ПУБЛИЧНЫЕ ENDPOINTS
                        // Accessible without authentication / Доступны без аутентификации
                        //
                        .requestMatchers("/", "/h2-console/**", "/css/**", "/js/**", "/images/**","/uploads/**", "/error", "/test-auth").permitAll()

                        //
                        // PUBLIC PAGES / ПУБЛИЧНЫЕ СТРАНИЦЫ
                        // About and Contact pages are public / Страницы "О нас" и "Контакты" публичные
                        //
                        .requestMatchers("/about",
                                "/contact",
                                "/projects",
                                "/projects/{slug}",
                                "/gallery",
                                "/photo-gallery",
                                "/photo-gallery/{id}",
                                "/patrons",
                                "/team",
                                "/api/calendar/events").permitAll()

                        //
                        // PUBLIC PAGE VIEWING / ПУБЛИЧНЫЙ ПРОСМОТР СТРАНИЦ
                        // CustomPage viewing by slug is public / Просмотр страниц по slug доступен всем
                        //
                        .requestMatchers("/pages/{slug}").permitAll()

                        //
                        // ADMIN ENDPOINTS / АДМИНИСТРАТИВНЫЕ ENDPOINTS
                        // Require authentication / Требуют аутентификации
                        // These URLs manage content and require login /
                        // Эти URL управляют контентом и требуют входа в систему
                        //
                        .requestMatchers("/pages", "/pages/create", "/pages/edit/**",
                                "/pages/delete/**", "/pages/publish/**", "/pages/unpublish/**").authenticated()

                        //
                        // ADMIN DASHBOARD / АДМИНИСТРАТИВНЫЙ ДАШБОРД
                        // Dashboard and admin panel access / Доступ к дашборду и админ панели
                        //
                        .requestMatchers("/admin", "/admin/**").authenticated()

                        //
                        // ALL OTHER REQUESTS / ВСЕ ОСТАЛЬНЫЕ ЗАПРОСЫ
                        // Require authentication / Требуют аутентификации
                        //
                        .anyRequest().authenticated()
                )

                //
                // LOGIN CONFIGURATION / КОНФИГУРАЦИЯ ЛОГИНА
                // Custom login page and processing / Кастомная страница логина и обработка
                //
                .formLogin(form -> form
                        .loginPage("/login")          // Custom login page URL / URL кастомной страницы логина
                        .loginProcessingUrl("/login") // URL for processing login / URL для обработки логина
                        .defaultSuccessUrl("/admin")  // Redirect after successful login / Перенаправление после успешного логина
                        .failureUrl("/login?error")   // Redirect after failed login / Перенаправление после неудачного логина
                        .permitAll()                  // Login page is accessible to all / Страница логина доступна всем
                )

                //
                // LOGOUT CONFIGURATION / КОНФИГУРАЦИЯ ЛОГАУТА
                // Logout URL and redirect / URL выхода и перенаправление
                //
                .logout(logout -> logout
                        .logoutUrl("/logout")         // URL for logout / URL для выхода
                        .logoutSuccessUrl("/login?logout") // Redirect after logout / Перенаправление после выхода
                        .invalidateHttpSession(true)  // Invalidate session / Инвалидировать сессию
                        .deleteCookies("JSESSIONID")  // Delete cookies / Удалить cookies
                        .permitAll()                  // Logout accessible to all / Выход доступен всем
                )

                //
                // EXCEPTION HANDLING / ОБРАБОТКА ИСКЛЮЧЕНИЙ
                // Custom access denied page / Кастомная страница отказа в доступе
                //
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied") // Custom access denied page / Кастомная страница отказа в доступе
                );

        //
        // H2 CONSOLE CONFIGURATION (for development only) / КОНФИГУРАЦИЯ H2 CONSOLE (только для разработки)
        // Disable CSRF and frame options for H2 console /
        // Отключаем CSRF и frame options для H2 console
        //
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Создает сервис для работы с пользователями из базы данных.
     * Заменяет InMemory аутентификацию на работу с реальной БД.
     *
     * @param userService сервис для работы с пользователями
     * @param passwordEncoder кодировщик паролей для безопасного хранения
     * @return сервис деталей пользователя с загрузкой из базы данных
     */
    @Bean
    public UserDetailsService userDetailsService(UserService userService, PasswordEncoder passwordEncoder) {
        return username -> {
            System.out.println("=== DEBUG: Поиск пользователя: " + username + " ===");

            // Ищем пользователя в базе данных по имени пользователя
            User user = userService.findUserByUsername(username)
                    .orElseThrow(() -> {
                        System.out.println("=== DEBUG: Пользователь НЕ НАЙДЕН: " + username + " ===");
                        return new UsernameNotFoundException("Пользователь не найден: " + username);
                    });

            // Проверяем, активна ли учетная запись
            if (!user.isEnabled()) {
                System.out.println("=== DEBUG: Учетная запись отключена: " + username + " ===");
                throw new UsernameNotFoundException("Учетная запись заблокирована: " + username);
            }

            // Преобразуем наши роли в authorities (ДОБАВЛЯЕМ ПРЕФИКС ROLE_)
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> "ROLE_" + role)  // Добавляем префикс
                    .map(role -> new SimpleGrantedAuthority(role))
                    .collect(Collectors.toList());

            // Преобразуем нашего User в Spring Security UserDetails
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .disabled(!user.isEnabled())
                    .build();
        };
    }

    /**
     * Создает и настраивает кодировщик паролей BCrypt.
     *
     * <p>BCrypt является надежным алгоритмом хеширования паролей,
     * который автоматически добавляет "соль" и обеспечивает
     * безопасное хранение учетных данных.</p>
     *
     * @return кодировщик паролей BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}