package com.community.cms.domain.model.people;

import com.community.cms.domain.model.content.Project;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Сущность члена команды организации "ЛАДА".
 *
 * <p>Представляет участника команды с информацией о должности, биографии
 * и участии в различных проектах. Используется для отображения команды
 * на сайте и управления ролями в проектах.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Основная должность и биография</li>
 *   <li>Аватарка для отображения</li>
 *   <li>Гибкая система ролей в разных проектах</li>
 *   <li>Возможность участия в нескольких проектах</li>
 *   <li>Порядок отображения в списке команды</li>
 * </ul>
 *
 * <p>Важно: Эта сущность НЕ связана с User для аутентификации.
 * TeamMember используется только для публичного отображения на сайте.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Полное имя члена команды.
     * Отображается на сайте в карточке участника.
     */
    @NotBlank(message = "Полное имя обязательно / Full name is required")
    @Size(min = 2, max = 100, message = "Полное имя должно быть от 2 до 100 символов / Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /**
     * Основная должность в организации.
     * Пример: "Художественный руководитель", "Координатор проектов".
     */
    @NotBlank(message = "Должность обязательна / Position is required")
    @Size(max = 100, message = "Должность не должна превышать 100 символов / Position must not exceed 100 characters")
    @Column(nullable = false)
    private String position;

    /**
     * Биография члена команды.
     * Поддерживает HTML разметку для форматирования.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * Путь к аватарке (фотографии) члена команды.
     * Используется в карточках и на страницах проекта.
     */
    @Column(name = "avatar_path")
    private String avatarPath;

    /**
     * Порядок сортировки в списке команды.
     * Меньшее значение = выше в списке.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Флаг активности члена команды.
     * Неактивные участники не отображаются на сайте.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Электронная почта для контактов (опционально).
     * Может использоваться для формы "Связаться с командой".
     */
    @Size(max = 100, message = "Email не должен превышать 100 символов / Email must not exceed 100 characters")
    private String email;

    /**
     * Телефон для контактов (опционально).
     */
    @Size(max = 50, message = "Телефон не должен превышать 50 символов / Phone must not exceed 50 characters")
    private String phone;

    /**
     * Ссылки на социальные сети (опционально).
     * Хранится в формате JSON: {"facebook": "url", "instagram": "url"}
     */
    @Lob
    @Column(name = "social_links", columnDefinition = "TEXT")
    private String socialLinks;

    // ================== СВЯЗИ С ПРОЕКТАМИ ==================

    /**
     * Проекты, в которых участвует член команды.
     * Связь одного-ко-многим через промежуточную таблицу.
     */
    @ManyToMany(mappedBy = "teamMembers")
    private Set<Project> projects = new HashSet<>();

    /**
     * Роли члена команды в конкретных проектах.
     * Хранится в формате Map<projectId, role>
     * Пример: {1: "Руководитель проекта", 2: "Координатор"}
     *
     * <p>Это поле НЕ маппится на базу данных через JPA.
     * Управляется через сервисный слой и хранится в отдельной таблице.
     */
    @Transient
    private Map<Long, String> projectRoles = new HashMap<>();

      // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * Дата и время создания записи.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию.
     */
    public TeamMember() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами члена команды.
     *
     * @param fullName полное имя
     * @param position должность
     */
    public TeamMember(String fullName, String position) {
        this();
        this.fullName = fullName;
        this.position = position;
        this.active = true;
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

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public Map<Long, String> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(Map<Long, String> projectRoles) {
        this.projectRoles = projectRoles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Добавляет проект к члену команды.
     *
     * @param project проект для добавления
     */
    public void addProject(Project project) {
        this.projects.add(project);
    }

    /**
     * Удаляет проект из списка участия члена команды.
     *
     * @param project проект для удаления
     * @return true если проект был удален, false если не найден
     */
    public boolean removeProject(Project project) {
        return this.projects.remove(project);
    }

    /**
     * Проверяет участвует ли член команды в указанном проекте.
     *
     * @param project проект для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Project project) {
        return this.projects.contains(project);
    }

    /**
     * Проверяет участвует ли член команды в проекте по ID.
     *
     * @param projectId ID проекта для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Long projectId) {
        return this.projects.stream()
                .anyMatch(project -> project.getId().equals(projectId));
    }

    /**
     * Устанавливает роль члена команды в конкретном проекте.
     *
     * @param projectId ID проекта
     * @param role роль в проекте
     */
    public void setRoleForProject(Long projectId, String role) {
        this.projectRoles.put(projectId, role);
    }

    /**
     * Получает роль члена команды в конкретном проекте.
     *
     * @param projectId ID проекта
     * @return роль в проекте или null если не участвует
     */
    public String getRoleForProject(Long projectId) {
        return this.projectRoles.get(projectId);
    }

    /**
     * Удаляет роль члена команды из проекта.
     *
     * @param projectId ID проекта
     * @return удаленная роль или null если не найдена
     */
    public String removeRoleForProject(Long projectId) {
        return this.projectRoles.remove(projectId);
    }

    /**
     * Проверяет имеет ли член команды аватарку.
     *
     * @return true если avatarPath не пустой, иначе false
     */
    public boolean hasAvatar() {
        return avatarPath != null && !avatarPath.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли член команды биографию.
     *
     * @return true если bio не пустой, иначе false
     */
    public boolean hasBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    /**
     * Получает количество проектов, в которых участвует член команды.
     *
     * @return количество проектов
     */
    public int getProjectsCount() {
        return projects != null ? projects.size() : 0;
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

    @Override
    public String toString() {
        return "TeamMember{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", active=" + active +
                ", projectsCount=" + getProjectsCount() +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     */
    @PrePersist
    @PreUpdate
    protected void validate() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}
