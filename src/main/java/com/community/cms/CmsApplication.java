package com.community.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс Spring Boot приложения Community CMS.
 *
 * <p>Этот класс является точкой входа в приложение и запускает
 * весь Spring Boot контекст. Аннотация {@code @SpringBootApplication}
 * объединяет три основные аннотации:</p>
 *
 * <ul>
 *   <li>{@code @Configuration} - помечает класс как источник конфигурации</li>
 *   <li>{@code @EnableAutoConfiguration} - включает авто конфигурацию Spring Boot</li>
 *   <li>{@code @ComponentScan} - включает сканирование компонентов в текущем пакете</li>
 * </ul>
 *
 * <p>Приложение предназначено для управления контентом общественных организаций
 * и предоставляет функционал создания, редактирования и публикации страниц.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @see <a href="https://spring.io/projects/spring-boot">Spring Boot Documentation</a>
 */

@SpringBootApplication
@ComponentScan(basePackages = "com.community.cms")
public class CmsApplication {

    /**
     * Главный метод, запускающий Spring Boot приложение.
     *
     * <p>Метод инициализирует Spring ApplicationContext и запускает
     * встроенный Tomcat сервер на порту 8080 (по умолчанию).</p>
     *
     * @param args аргументы командной строки, передаваемые при запуске
     */
    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }
}