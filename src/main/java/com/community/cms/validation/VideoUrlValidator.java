package com.community.cms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VideoUrlValidator implements ConstraintValidator<VideoUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Пустые значения разрешены
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String url = value.trim().toLowerCase();

        // 1. Проверяем что это HTTP ссылка
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        // 2. ПРОСТАЯ проверка на основные домены
        boolean isYoutube = url.contains("youtube.com") || url.contains("youtu.be");
        boolean isVimeo = url.contains("vimeo.com");
        boolean isRutube = url.contains("rutube.ru");

        // 3. Если не один из поддерживаемых - ошибка
        return isYoutube || isVimeo || isRutube;
    }
}