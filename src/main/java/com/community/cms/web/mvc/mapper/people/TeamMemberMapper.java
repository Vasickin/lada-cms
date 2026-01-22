package com.community.cms.web.mvc.mapper.people;

import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.web.mvc.dto.people.TeamMemberDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Маппер для преобразования TeamMember Entity в TeamMemberDTO.
 * Для публичного раздела "Наша команда".
 */
@Component
public class TeamMemberMapper {

    private final ObjectMapper objectMapper;

    public TeamMemberMapper() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Преобразует TeamMember Entity в TeamMemberDTO.
     * Для списка команды на сайте.
     */
    public TeamMemberDTO toDTO(TeamMember teamMember) {
        if (teamMember == null) {
            return null;
        }

        TeamMemberDTO dto = new TeamMemberDTO();

        // Основные данные для отображения
        dto.setId(teamMember.getId());
        dto.setFullName(teamMember.getFullName());
        dto.setPosition(teamMember.getPosition());
        dto.setBio(teamMember.getBio());
        dto.setAvatarPath(teamMember.getAvatarPath());

        // ФИКС: Добавляем префикс к пути аватара
        String avatarPath = teamMember.getAvatarPath();
        if (avatarPath != null && !avatarPath.trim().isEmpty()) {
            if (!avatarPath.startsWith("/uploads/") && !avatarPath.startsWith("uploads/")) {
                dto.setAvatarPath("/uploads/" + avatarPath);
            } else {
                dto.setAvatarPath(avatarPath);
            }
        }

        dto.setInitials(teamMember.getInitials());
        dto.setSortOrder(teamMember.getSortOrder());

        // Контактная информация (опционально)
        dto.setEmail(teamMember.getEmail());
        dto.setPhone(teamMember.getPhone());

        // Социальные сети (парсим JSON)
        dto.setSocialLinks(parseSocialLinks(teamMember.getSocialLinks()));

        return dto;
    }

    /**
     * Преобразует список TeamMember Entity в список TeamMemberDTO.
     */
    public List<TeamMemberDTO> toDTOList(List<TeamMember> teamMembers) {
        if (teamMembers == null) {
            return List.of();
        }

        return teamMembers.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Парсит JSON строку социальных сетей в Map.
     */
    private Map<String, String> parseSocialLinks(String socialLinksJson) {
        if (socialLinksJson == null || socialLinksJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(
                    socialLinksJson,
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (JsonProcessingException e) {
            // Если JSON невалидный, возвращаем пустой Map
            System.err.println("Ошибка парсинга социальных сетей для TeamMember: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
