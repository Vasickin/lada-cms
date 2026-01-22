package com.community.cms.web.mvc.dto.people;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO для публичного отображения члена команды.
 * Содержит только данные для отображения в разделе "Наша команда".
 * Проекты не включаем - это отдельный раздел сайта.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamMemberDTO {

    // ================== ОСНОВНЫЕ ДАННЫЕ ДЛЯ ОТОБРАЖЕНИЯ ==================

    private Long id;
    private String fullName;
    private String position;
    private String bio;
    private String avatarPath;
    private String initials;
    private Integer sortOrder;

    // ================== КОНТАКТНАЯ ИНФОРМАЦИЯ ==================

    private String email;
    private String phone;
    private Map<String, String> socialLinks;

    // ================== КОНСТРУКТОРЫ ==================

    public TeamMemberDTO() {
    }

    public TeamMemberDTO(String fullName, String position, String avatarPath) {
        this.fullName = fullName;
        this.position = position;
        this.avatarPath = avatarPath;
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
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

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public Map<String, String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(Map<String, String> socialLinks) {
        this.socialLinks = socialLinks;
    }

    // ================== ВЫЧИСЛЯЕМЫЕ СВОЙСТВА (для шаблонов) ==================

    @JsonProperty("hasAvatar")
    public boolean hasAvatar() {
        return avatarPath != null && !avatarPath.trim().isEmpty();
    }

    @JsonProperty("hasBio")
    public boolean hasBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    @JsonProperty("hasContacts")
    public boolean hasContacts() {
        return (email != null && !email.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty());
    }

    @JsonProperty("hasSocialLinks")
    public boolean hasSocialLinks() {
        return socialLinks != null && !socialLinks.isEmpty();
    }

    @Override
    public String toString() {
        return "TeamMemberDTO{" +
                "fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", hasAvatar=" + hasAvatar() +
                '}';
    }
}