package com.community.cms.web.mvc.dto;

/**
 * DTO для передачи статистики по страницам.
 * Используется для отображения данных на дашборде.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
public record PageStatistics(long totalPages, long publishedCount, long draftCount) {
}
