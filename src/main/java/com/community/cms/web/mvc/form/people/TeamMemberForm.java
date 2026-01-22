package com.community.cms.web.mvc.form.people;

import com.community.cms.domain.model.people.TeamMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object (DTO) для формы создания и редактирования членов команды.
 *
 * <p>Используется в административной панели для управления участниками команды организации.
 * Включает основную информацию, контакты, участие в проектах и роли в них.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Валидация обязательных полей (имя, должность)</li>
 *   <li>Управление участием в проектах (выбор из списка)</li>
 *   <li>Настройка ролей в конкретных проектах</li>
 *   <li>Валидация контактных данных и соцсетей</li>
 *   <li>Управление порядком отображения в списке команды</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see TeamMember
 */
public class TeamMemberForm {

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * ID члена команды.
     * Используется только при редактировании существующего участника.
     */
    private Long id;

    // ================== ОСНОВНАЯ ИНФОРМАЦИЯ ==================

    /**
     * Полное имя члена команды.
     * Обязательное поле, от 2 до 100 символов.
     * Отображается на сайте в карточке участника.
     */
    @NotBlank(message = "Полное имя обязательно / Full name is required")
    @Size(min = 2, max = 100, message = "Полное имя должно быть от 2 до 100 символов / Full name must be between 2 and 100 characters")
    private String fullName;

    /**
     * Основная должность в организации.
     * Обязательное поле, максимум 100 символов.
     * Пример: "Художественный руководитель", "Координатор проектов".
     */
    @NotBlank(message = "Должность обязательна / Position is required")
    @Size(max = 100, message = "Должность не должна превышать 100 символов / Position must not exceed 100 characters")
    private String position;

    /**
     * Биография члена команды.
     * Поддерживает HTML разметку для форматирования.
     * Используется для подробного описания опыта и достижений.
     */
    private String bio;

    // ================== АВАТАР И КОНТАКТЫ ==================

    /**
     * Путь к аватарке (фотографии) члена команды.
     * Используется в карточках и на страницах проекта.
     * Если не указан, генерируются инициалы на цветном фоне.
     */
    private String avatarPath;

    /**
     * Электронная почта для контактов (опционально).
     * Максимум 100 символов.
     * Может использоваться для формы "Связаться с командой".
     */
    @Size(max = 100, message = "Email не должен превышать 100 символов / Email must not exceed 100 characters")
    private String email;

    /**
     * Телефон для контактов (опционально).
     * Максимум 50 символов.
     */
    @Size(max = 50, message = "Телефон не должен превышать 50 символов / Phone must not exceed 50 characters")
    private String phone;

    /**
     * Ссылки на социальные сети в формате JSON.
     * Пример: {"facebook": "https://...", "instagram": "https://..."}
     * Хранится как строка, парсится при необходимости.
     */
    private String socialLinks;

    // ================== УЧАСТИЕ В ПРОЕКТАХ ==================

    /**
     * ID проектов, в которых участвует член команды.
     * Используется для выбора проектов в форме (мультиселект).
     */
    private Set<Long> projectIds = new HashSet<>();

    /**
     * Роли члена команды в конкретных проектах.
     * Формат: Map<projectId, role>
     * Пример: {1: "Руководитель проекта", 2: "Координатор"}
     * Для каждого выбранного проекта можно указать свою роль.
     */
    private Map<Long, String> projectRoles;

    // ================== НАСТРОЙКИ ОТОБРАЖЕНИЯ ==================

    /**
     * Порядок сортировки в списке команды.
     * Меньшее значение = выше в списке.
     * По умолчанию: 0.
     */
    private Integer sortOrder = 0;

    /**
     * Флаг активности члена команды.
     * Неактивные участники не отображаются на сайте.
     * По умолчанию: true (активен).
     */
    private boolean active = true;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     * Инициализирует значения по умолчанию.
     */
    public TeamMemberForm() {
        this.sortOrder = 0;
        this.active = true;
        this.projectIds = new HashSet<>();
    }

    /**
     * Конструктор на основе существующего члена команды.
     * Используется для редактирования участника.
     *
     * @param teamMember существующий член команды
     */
    public TeamMemberForm(TeamMember teamMember) {
        this();
        this.id = teamMember.getId();
        this.fullName = teamMember.getFullName();
        this.position = teamMember.getPosition();
        this.bio = teamMember.getBio();
        this.avatarPath = teamMember.getAvatarPath();
        this.email = teamMember.getEmail();
        this.phone = teamMember.getPhone();
        this.socialLinks = teamMember.getSocialLinks();
        this.sortOrder = teamMember.getSortOrder();
        this.active = teamMember.isActive();

        // Собираем ID проектов
        if (teamMember.getProjects() != null) {
            teamMember.getProjects().forEach(project ->
                    this.projectIds.add(project.getId()));
        }

        // Копируем роли в проектах
        this.projectRoles = teamMember.getProjectRoles();
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(String socialLinks) {
        this.socialLinks = socialLinks;
    }

    public Set<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(Set<Long> projectIds) {
        this.projectIds = projectIds;
    }

    public Map<Long, String> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(Map<Long, String> projectRoles) {
        this.projectRoles = projectRoles;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет, является ли член команды активным.
     *
     * @return true если active = true, иначе false
     */
    public boolean isMemberActive() {
        return active;
    }

    /**
     * Проверяет, имеет ли член команды аватарку.
     *
     * @return true если avatarPath не пустой, иначе false
     */
    public boolean hasAvatar() {
        return avatarPath != null && !avatarPath.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды биографию.
     *
     * @return true если bio не пустой, иначе false
     */
    public boolean hasBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды email.
     *
     * @return true если email не пустой, иначе false
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды телефон.
     *
     * @return true если phone не пустой, иначе false
     */
    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды ссылки на социальные сети.
     *
     * @return true если socialLinks не пустой, иначе false
     */
    public boolean hasSocialLinks() {
        return socialLinks != null && !socialLinks.trim().isEmpty();
    }

    /**
     * Проверяет, участвует ли член команды в каком-либо проекте.
     *
     * @return true если есть хотя бы один projectId, иначе false
     */
    public boolean hasProjects() {
        return projectIds != null && !projectIds.isEmpty();
    }

    /**
     * Получает количество проектов, в которых участвует член команды.
     *
     * @return количество проектов
     */
    public int getProjectsCount() {
        return projectIds != null ? projectIds.size() : 0;
    }

    /**
     * Получает инициалы члена команды (для аватарок по умолчанию).
     * Пример: "Иван Петров" → "ИП"
     *
     * @return инициалы (2 буквы)
     */
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "TM";
        }

        String[] names = fullName.split("\\s+");
        if (names.length >= 2) {
            // Берем первую букву имени и первую букву фамилии
            return (names[0].charAt(0) + "" + names[names.length - 1].charAt(0)).toUpperCase();
        } else if (names.length == 1) {
            // Если только одно слово, берем первые две буквы
            return names[0].length() >= 2
                    ? names[0].substring(0, 2).toUpperCase()
                    : names[0].toUpperCase();
        }

        return "TM";
    }

    /**
     * Добавляет проект к члену команды.
     *
     * @param projectId ID проекта для добавления
     */
    public void addProjectId(Long projectId) {
        if (projectIds == null) {
            projectIds = new HashSet<>();
        }
        projectIds.add(projectId);
    }

    /**
     * Удаляет проект из списка участия члена команды.
     *
     * @param projectId ID проекта для удаления
     * @return true если проект был удален, false если не найден
     */
    public boolean removeProjectId(Long projectId) {
        return projectIds != null && projectIds.remove(projectId);
    }

    /**
     * Проверяет участвует ли член команды в указанном проекте.
     *
     * @param projectId ID проекта для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Long projectId) {
        return projectIds != null && projectIds.contains(projectId);
    }

    /**
     * Устанавливает роль члена команды в конкретном проекте.
     *
     * @param projectId ID проекта
     * @param role роль в проекте
     */
    public void setRoleForProject(Long projectId, String role) {
        if (projectRoles == null) {
            projectRoles = new java.util.HashMap<>();
        }
        projectRoles.put(projectId, role);
    }

    /**
     * Получает роль члена команды в конкретном проекте.
     *
     * @param projectId ID проекта
     * @return роль в проекте или null если не участвует
     */
    public String getRoleForProject(Long projectId) {
        return projectRoles != null ? projectRoles.get(projectId) : null;
    }

    /**
     * Получает отображаемую роль для проекта.
     * Если роль для проекта не указана, возвращает основную должность.
     *
     * @param projectId ID проекта
     * @return роль для отображения
     */
    public String getDisplayRoleForProject(Long projectId) {
        String projectRole = getRoleForProject(projectId);
        return projectRole != null ? projectRole : position;
    }

    /**
     * Преобразует TeamMemberForm в сущность TeamMember.
     * Не заполняет связи с проектами (нужно установить отдельно по projectIds).
     *
     * @return сущность TeamMember с заполненными базовыми полями
     */
    public TeamMember toEntity() {
        TeamMember teamMember = new TeamMember();
        teamMember.setId(this.id);
        teamMember.setFullName(this.fullName);
        teamMember.setPosition(this.position);
        teamMember.setBio(this.bio);
        teamMember.setAvatarPath(this.avatarPath);
        teamMember.setEmail(this.email);
        teamMember.setPhone(this.phone);
        teamMember.setSocialLinks(this.socialLinks);
        teamMember.setSortOrder(this.sortOrder);
        teamMember.setActive(this.active);

        // Проекты устанавливаются отдельно по projectIds
        // teamMember.setProjects(projects);

        // Роли в проектах устанавливаются отдельно
        if (this.projectRoles != null) {
            teamMember.setProjectRoles(new java.util.HashMap<>(this.projectRoles));
        }

        return teamMember;
    }

    /**
     * Обновляет существующую сущность TeamMember данными из формы.
     * Не обновляет связи с проектами.
     *
     * @param teamMember сущность для обновления
     */
    public void updateEntity(TeamMember teamMember) {
        teamMember.setFullName(this.fullName);
        teamMember.setPosition(this.position);
        teamMember.setBio(this.bio);
        teamMember.setAvatarPath(this.avatarPath);
        teamMember.setEmail(this.email);
        teamMember.setPhone(this.phone);
        teamMember.setSocialLinks(this.socialLinks);
        teamMember.setSortOrder(this.sortOrder);
        teamMember.setActive(this.active);

        // Обновляем роли в проектах, если они были указаны
        if (this.projectRoles != null) {
            teamMember.setProjectRoles(new java.util.HashMap<>(this.projectRoles));
        }
    }

    /**
     * Создает JSON представление ссылок на социальные сети.
     * Используется для удобного хранения в базе данных.
     *
     * @param facebook ссылка на Facebook (опционально)
     * @param instagram ссылка на Instagram (опционально)
     * @param vk ссылка на ВКонтакте (опционально)
     * @param telegram ссылка на Telegram (опционально)
     * @return JSON строка с ссылками
     */
    public static String createSocialLinksJson(String facebook, String instagram,
                                               String vk, String telegram) {
        java.util.Map<String, String> links = new java.util.HashMap<>();

        if (facebook != null && !facebook.trim().isEmpty()) {
            links.put("facebook", facebook.trim());
        }
        if (instagram != null && !instagram.trim().isEmpty()) {
            links.put("instagram", instagram.trim());
        }
        if (vk != null && !vk.trim().isEmpty()) {
            links.put("vk", vk.trim());
        }
        if (telegram != null && !telegram.trim().isEmpty()) {
            links.put("telegram", telegram.trim());
        }

        if (links.isEmpty()) {
            return null;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(links);
        } catch (Exception e) {
            // В случае ошибки возвращаем простой JSON
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<String, String> entry : links.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":\"")
                        .append(entry.getValue().replace("\"", "\\\"")).append("\"");
                first = false;
            }
            json.append("}");
            return json.toString();
        }
    }

    /**
     * Парсит JSON строку социальных ссылок в Map.
     *
     * @param socialLinksJson JSON строка с социальными ссылками
     * @return Map с социальными ссылками или пустая Map
     */
    public static java.util.Map<String, String> parseSocialLinks(String socialLinksJson) {
        if (socialLinksJson == null || socialLinksJson.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(socialLinksJson,
                    mapper.getTypeFactory().constructMapType(java.util.HashMap.class, String.class, String.class));
        } catch (Exception e) {
            // Если не удалось распарсить как JSON, пробуем ручной парсинг
            Map<String, String> result = getStringStringMap(socialLinksJson);
            return result;
        }
    }

    private static Map<String, String> getStringStringMap(String socialLinksJson) {
        Map<String, String> result = new java.util.HashMap<>();
        // Упрощенный парсинг для формата "facebook=url&instagram=url"
        String cleaned = socialLinksJson.replaceAll("[{}\"]", "");
        String[] pairs = cleaned.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "TeamMemberForm{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", active=" + active +
                ", projectsCount=" + getProjectsCount() +
                '}';
    }
}
