package com.community.cms.domain.enums;

/**
 * Статусы проекта для управления жизненным циклом.
 * <p>
 * Используется для определения текущего состояния проекта в системе,
 * фильтрации проектов в публичной части и админ-панели, а также
 * для аналитики и статистики по проектам.
 * </p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 1.0
 */
public enum ProjectStatusType {

    /**
     * Ближайшие проекты — запланированные на ближайшее время.
     * <p>
     * Проекты, которые скоро стартуют. Отображаются в специальном
     * разделе "Скоро" или "Предстоящие проекты". Могут иметь обратный
     * отсчет до даты начала.
     * </p>
     */
    UPCOMING("Ближайшие", "Upcoming"),

    /**
     * Активные проекты — текущие, в работе.
     * <p>
     * Основные проекты, которые прямо сейчас реализуются.
     * Отображаются в первую очередь в публичной части.
     * </p>
     */
    ACTIVE("Активные", "Active"),

    /**
     * Завершённые проекты — успешно реализованные.
     * <p>
     * Проекты, которые доведены до конца и сданы.
     * Хранятся в архиве для истории и отчетности.
     * </p>
     */
    COMPLETED("Завершённые", "Completed"),

    /**
     * Ежегодные проекты — повторяющиеся каждый год.
     * <p>
     * Специальный статус для проектов, которые проводятся
     * регулярно (фестивали, конференции, конкурсы).
     * Автоматически создаются заново каждый год.
     * </p>
     */
    ANNUAL("Ежегодные", "Annual"),

    /**
     * Архивные проекты — старые, неактуальные.
     * <p>
     * Проекты, которые больше не ведутся и не отображаются
     * в основной выдаче, но доступны в архиве.
     * </p>
     */
    ARCHIVED("Архивные", "Archived");

    private final String nameRu;
    private final String nameEn;

    /**
     * Конструктор статуса проекта.
     *
     * @param nameRu название статуса на русском языке
     * @param nameEn название статуса на английском языке
     */
    ProjectStatusType(String nameRu, String nameEn) {
        this.nameRu = nameRu;
        this.nameEn = nameEn;
    }

    /**
     * Возвращает название статуса на русском языке.
     *
     * @return название на русском
     */
    public String getNameRu() {
        return nameRu;
    }

    /**
     * Возвращает название статуса на английском языке.
     *
     * @return название на английском
     */
    public String getNameEn() {
        return nameEn;
    }

    /**
     * Получить статус по русскому названию.
     *
     * @param nameRu русское название статуса
     * @return соответствующий статус или null, если не найден
     */
    public static ProjectStatusType fromNameRu(String nameRu) {
        for (ProjectStatusType status : values()) {
            if (status.getNameRu().equalsIgnoreCase(nameRu)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Получить статус по английскому названию.
     *
     * @param nameEn английское название статуса
     * @return соответствующий статус или null, если не найден
     */
    public static ProjectStatusType fromNameEn(String nameEn) {
        for (ProjectStatusType status : values()) {
            if (status.getNameEn().equalsIgnoreCase(nameEn)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Проверяет, является ли статус "публичным" (должен отображаться на сайте).
     *
     * @return true если статус публичный
     */
    public boolean isPublic() {
        return this == ACTIVE || this == UPCOMING || this == ANNUAL;
    }

    /**
     * Проверяет, является ли статус "архивным" (не должен отображаться в основной выдаче).
     *
     * @return true если статус архивный
     */
    public boolean isArchived() {
        return this == ARCHIVED || this == COMPLETED;
    }

    /**
     * Проверяет, можно ли редактировать проект с данным статусом.
     *
     * @return true если проект можно редактировать
     */
    public boolean isEditable() {
        return this != ARCHIVED && this != COMPLETED;
    }

    /**
     * Проверяет, можно ли удалить проект с данным статусом.
     *
     * @return true если проект можно удалить
     */
    public boolean isDeletable() {
        return this == ARCHIVED || this == COMPLETED;
    }

    @Override
    public String toString() {
        return nameRu;
    }
}
