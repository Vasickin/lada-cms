package com.community.cms.domain.model.people;

import com.community.cms.domain.enums.PartnerType;
import com.community.cms.domain.model.content.Project;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность партнера организации "ЛАДА".
 *
 * <p>Представляет партнерскую организацию с информацией о типе партнерства,
 * контактных данных и участии в различных проектах. Используется для отображения
 * партнеров на сайте и управления партнерскими отношениями.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Название организации и описание</li>
 *   <li>Тип партнерства (спонсор, информационный и т.д.)</li>
 *   <li>Логотип и ссылка на сайт</li>
 *   <li>Контактные данные (лицо, email, телефон)</li>
 *   <li>Возможность участия в нескольких проектах</li>
 *   <li>Порядок отображения в списке партнеров</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "partners")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название партнерской организации.
     * Отображается на сайте в карточке партнера.
     */
    @NotBlank(message = "Название организации обязательно / Organization name is required")
    @Size(min = 2, max = 255, message = "Название организации должно быть от 2 до 255 символов / Organization name must be between 2 and 255 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Описание партнера.
     * Поддерживает HTML разметку для форматирования.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Тип партнерства.
     * Определяется перечислением PartnerType.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PartnerType type;

    /**
     * Путь к логотипу партнера.
     * Сохраняется в формате: "/uploads/имя_файла"
     * Используется в карточках и на страницах проекта.
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * Сайт партнера.
     */
    @Size(max = 500, message = "Сайт не должен превышать 500 символов / Website must not exceed 500 characters")
    private String website;

    /**
     * Электронная почта для контактов.
     * Может использоваться для связи с представителем партнера.
     */
    @Size(max = 100, message = "Email не должен превышать 100 символов / Email must not exceed 100 characters")
    @Column(name = "contact_email")
    private String contactEmail;

    /**
     * Телефон для контактов.
     */
    @Size(max = 50, message = "Телефон не должен превышать 50 символов / Phone must not exceed 50 characters")
    @Column(name = "contact_phone")
    private String contactPhone;

    /**
     * Контактное лицо.
     * Имя представителя партнерской организации.
     */
    @Size(max = 100, message = "Контактное лицо не должно превышать 100 символов / Contact person must not exceed 100 characters")
    @Column(name = "contact_person")
    private String contactPerson;

    /**
     * Порядок сортировки в списке партнеров.
     * Меньшее значение = выше в списке.
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * Флаг активности партнера.
     * Неактивные партнеры не отображаются на сайте.
     */
    private boolean active = true;

    // ================== СВЯЗИ С ПРОЕКТАМИ ==================

    /**
     * Проекты, в которых участвует партнер.
     * Связь многие-ко-многим через промежуточную таблицу project_partner_associations.
     */
    @ManyToMany
    @JoinTable(
            name = "project_partner_associations",
            joinColumns = @JoinColumn(name = "partner_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();

    // ================== ПОЛЕ ДЛЯ ЗАГРУЗКИ ФАЙЛА ==================

    /**
     * Файл логотипа для загрузки.
     * Не сохраняется в БД, используется только для передачи файла.
     */
    @Transient
    private MultipartFile logoFile;

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * Дата и время создания записи.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию.
     */
    public Partner() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами партнера.
     *
     * @param name название организации
     * @param type тип партнерства
     */
    public Partner(String name, PartnerType type) {
        this();
        this.name = name;
        this.type = type;
        this.active = true;
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PartnerType getType() {
        return type;
    }

    public void setType(PartnerType type) {
        this.type = type;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
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

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
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

    public MultipartFile getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(MultipartFile logoFile) {
        this.logoFile = logoFile;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Добавляет проект к партнеру.
     *
     * @param project проект для добавления
     */
    public void addProject(Project project) {
        this.projects.add(project);
    }

    /**
     * Удаляет проект из списка участия партнера.
     *
     * @param project проект для удаления
     * @return true если проект был удален, false если не найден
     */
    public boolean removeProject(Project project) {
        return this.projects.remove(project);
    }

    /**
     * Проверяет участвует ли партнер в указанном проекте.
     *
     * @param project проект для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Project project) {
        return this.projects.contains(project);
    }

    /**
     * Проверяет участвует ли партнер в проекте по ID.
     *
     * @param projectId ID проекта для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Long projectId) {
        return this.projects.stream()
                .anyMatch(project -> project.getId().equals(projectId));
    }

    /**
     * Проверяет имеет ли партнер логотип.
     *
     * @return true если logoUrl не пустой, иначе false
     */
    public boolean hasLogo() {
        return logoUrl != null && !logoUrl.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли партнер описание.
     *
     * @return true если description не пустой, иначе false
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли партнер сайт.
     *
     * @return true если website не пустой, иначе false
     */
    public boolean hasWebsite() {
        return website != null && !website.trim().isEmpty();
    }

    /**
     * Проверяет указано ли контактное лицо.
     *
     * @return true если contactPerson не пустой, иначе false
     */
    public boolean hasContactPerson() {
        return contactPerson != null && !contactPerson.trim().isEmpty();
    }

    /**
     * Получает количество проектов, в которых участвует партнер.
     *
     * @return количество проектов
     */
    public int getProjectsCount() {
        return projects != null ? projects.size() : 0;
    }

    /**
     * Получает отображаемое название типа партнера.
     *
     * @return отображаемое название типа или пустая строка если тип не указан
     */
    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : "";
    }

    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + (type != null ? type.name() : "null") +
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