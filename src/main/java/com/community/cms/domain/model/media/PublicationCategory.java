package com.community.cms.domain.model.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность категории публикации.
 * Определяет где может быть опубликован контент (главная, о нас, проекты, галерея).
 * Publication category entity.
 * Defines where content can be published (homepage, about us, projects, gallery).
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "publication_categories")
public class PublicationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название категории обязательно / Category name is required")
    @Size(max = 100, message = "Название категории не должно превышать 100 символов / Category name should not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию.
     * Default constructor.
     */
    public PublicationCategory() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Конструктор с параметрами для удобного создания.
     * Parameterized constructor for easy creation.
     *
     * @param name название категории / category name
     * @param description описание категории / category description
     */
    public PublicationCategory(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Геттеры и сеттеры / Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Методы предварительной обработки / Pre-persist methods

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (name == null) {
            name = "";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Вспомогательные методы / Helper methods

    @Override
    public String toString() {
        return "PublicationCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + (description != null && description.length() > 50 ?
                description.substring(0, 50) + "..." : description) + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationCategory that = (PublicationCategory) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
