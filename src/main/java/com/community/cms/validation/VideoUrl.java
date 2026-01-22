package com.community.cms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Валидация ссылок на видео.
 * Проверяет что URL является корректной ссылкой на YouTube, Vimeo или Rutube.
 *
 * @since 2025
 */
@Documented
@Constraint(validatedBy = VideoUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface VideoUrl {

    /**
     * Сообщение об ошибке.
     */
    String message() default "Некорректная ссылка на видео. Поддерживаются: YouTube, Vimeo, Rutube";

    /**
     * Группы валидации.
     */
    Class<?>[] groups() default {};

    /**
     * Полезная нагрузка.
     */
    Class<? extends Payload>[] payload() default {};
}
