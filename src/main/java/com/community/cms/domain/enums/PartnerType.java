package com.community.cms.domain.enums;

/**
 * Перечисление типов партнеров в системе.
 * Определяет категории партнерских отношений, которые могут быть
 * установлены с организациями или частными лицами.
 *
 * Каждый тип имеет:
 * - Техническое имя (для БД и логики)
 * - Отображаемое название (для интерфейса пользователя)
 *
 * @version 1.0
 * @since 2024
 */
public enum PartnerType {

    /**
     * Финансовый спонсор проекта или мероприятия.
     * Партнер, предоставляющий денежные средства или материальную поддержку.
     */
    SPONSOR("Спонсор"),

    /**
     * Информационный партнер.
     * Организация, предоставляющая информационную поддержку:
     * публикации в СМИ, размещение информации на своих ресурсах,
     * продвижение в социальных сетях.
     */
    INFORMATION("Информационный партнёр"),

    /**
     * Технический партнер.
     * Компания, предоставляющая техническое оборудование, ПО,
     * инфраструктуру или техническую экспертизу.
     */
    TECHNICAL("Технический партнёр"),

    /**
     * Организатор мероприятия или проекта.
     * Партнер, непосредственно участвующий в организации
     * и координации деятельности.
     */
    ORGANIZER("Организатор"),

    /**
     * Общий партнер.
     * Универсальная категория для партнеров, не подходящих
     * под другие специализированные типы, но участвующих
     * в совместной деятельности.
     */
    PARTNER("Партнёр"),

    /**
     * Другая форма партнерства.
     * Используется для кастомизированных или редко встречающихся
     * типов сотрудничества.
     */
    OTHER("Другое");

    private final String displayName;

    /**
     * Конструктор типа партнера.
     *
     * @param displayName Локализованное отображаемое название,
     *                    используемое в пользовательском интерфейсе
     */
    PartnerType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает отображаемое название типа партнера.
     *
     * @return Локализованная строка для отображения в UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Поиск типа партнера по строковому значению.
     * Регистронезависимый поиск как по техническому имени (enum name),
     * так и по отображаемому названию.
     *
     * @param value Строка для поиска (название enum или displayName)
     * @return Найденный PartnerType или {@code null} если не найден
     */
    public static PartnerType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        // Пробуем найти по имени enum (регистронезависимо)
        for (PartnerType type : PartnerType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        // Пробуем найти по displayName (регистронезависимо)
        for (PartnerType type : PartnerType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        return null;
    }

    /**
     * Проверяет, является ли данный тип финансовым спонсором.
     *
     * @return {@code true} если тип SPONSOR, иначе {@code false}
     */
    public boolean isSponsor() {
        return this == SPONSOR;
    }

    /**
     * Проверяет, является ли данный тип информационным партнером.
     *
     * @return {@code true} если тип INFORMATION, иначе {@code false}
     */
    public boolean isInformation() {
        return this == INFORMATION;
    }

    /**
     * Проверяет, является ли данный тип техническим партнером.
     *
     * @return {@code true} если тип TECHNICAL, иначе {@code false}
     */
    public boolean isTechnical() {
        return this == TECHNICAL;
    }

    /**
     * Проверяет, является ли данный тип организатором.
     *
     * @return {@code true} если тип ORGANIZER, иначе {@code false}
     */
    public boolean isOrganizer() {
        return this == ORGANIZER;
    }



    /**
     * Возвращает массив всех типов партнеров с их отображаемыми названиями.
     * Удобно для заполнения выпадающих списков в UI.
     *
     * @return Массив строк в формате "TECHNICAL_NAME - Display Name"
     */
    public static String[] getAllTypesWithDisplayNames() {
        PartnerType[] types = values();
        String[] result = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = types[i].name() + " - " + types[i].getDisplayName();
        }
        return result;
    }

    /**
     * Возвращает CSS класс цвета для данного типа партнера.
     * Используется для визуального выделения в интерфейсе.
     *
     * @return Название CSS класса цвета (без префикса "bg-")
     */
    public String getColorClass() {
        switch (this) {
            case SPONSOR:
                return "success"; // Зеленый
            case INFORMATION:
                return "info"; // Голубой
            case TECHNICAL:
                return "warning"; // Желтый/оранжевый
            case ORGANIZER:
                return "primary"; // Синий
            case PARTNER:
                return "secondary"; // Серый
            case OTHER:
                return "dark"; // Темный
            default:
                return "secondary"; // По умолчанию
        }
    }

    /**
     * Возвращает иконку Bootstrap Icons для данного типа партнера.
     *
     * @return Название класса иконки
     */
    public String getIconClass() {
        switch (this) {
            case SPONSOR:
                return "bi-cash-coin";
            case INFORMATION:
                return "bi-megaphone";
            case TECHNICAL:
                return "bi-gear";
            case ORGANIZER:
                return "bi-people";
            default:
                return "bi-shake";
        }
    }
}
