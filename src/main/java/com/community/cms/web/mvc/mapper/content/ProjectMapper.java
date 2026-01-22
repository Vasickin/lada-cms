package com.community.cms.web.mvc.mapper.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.service.content.PhotoGalleryService;
import com.community.cms.web.mvc.dto.content.PhotoGalleryDTO;
import com.community.cms.web.mvc.dto.content.ProjectDTO;
import com.community.cms.web.mvc.dto.people.TeamMemberDTO;
import com.community.cms.web.mvc.mapper.people.TeamMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования Project Entity в ProjectDTO.
 * Использует существующие сервисы из админки.
 */
@Component
public class ProjectMapper {

    private final TeamMemberMapper teamMemberMapper;
    private final PhotoGalleryService photoGalleryService;

    /**
     * Конструктор с инъекцией зависимостей.
     */
    @Autowired
    public ProjectMapper(TeamMemberMapper teamMemberMapper, PhotoGalleryService photoGalleryService) {
        this.teamMemberMapper = teamMemberMapper;
        this.photoGalleryService = photoGalleryService;
    }

    /**
     * Преобразует Project Entity в ProjectDTO.
     * Базовый метод - использует те же поля что и в админке.
     */
    public ProjectDTO toDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDTO dto = new ProjectDTO();

        // Копируем ВСЕ поля из Project (как в админке)
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setSlug(project.getSlug());
        dto.setShortDescription(project.getShortDescription());
        dto.setFullDescription(project.getFullDescription());

        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setEventDate(project.getEventDate());
        dto.setLocation(project.getLocation());

        // Для календаря
        if (project.getEventDate() != null) {
            dto.setEventYear(project.getEventDate().getYear());
            dto.setEventMonth(project.getEventDate().getMonthValue());
            dto.setEventDay(project.getEventDate().getDayOfMonth());
        }

        dto.setFeaturedImagePath(project.getFeaturedImagePath());
        dto.setKeyPhotoIds(project.getKeyPhotoIds()); // ID фото как в админке
        dto.setVideoUrl(project.getVideoUrl());
        dto.setVideoPlatform(project.getVideoPlatform());
        dto.setVideoEmbedUrl(project.getVideoEmbedUrl());

        dto.setCategory(project.getCategory());
        dto.setStatus(project.getStatus().name());

        // Секции (как в админке)
        dto.setShowDescription(project.isShowDescription());
        dto.setShowPhotos(project.isShowPhotos());
        dto.setShowVideos(project.isShowVideos());
        dto.setShowTeam(project.isShowTeam());
        dto.setShowParticipation(project.isShowParticipation());
        dto.setShowPartners(project.isShowPartners());
        dto.setShowRelated(project.isShowRelated());

        dto.setMetaTitle(project.getMetaTitle());
        dto.setMetaDescription(project.getMetaDescription());
        dto.setMetaKeywords(project.getMetaKeywords());
        dto.setOgImagePath(project.getOgImagePath());

        // Вычисляемые поля (используем методы из Project)
        dto.setCurrentlyActive(project.isCurrentlyActive());
        dto.setAnnual(project.isAnnual());
        dto.setArchived(project.isArchived());
        dto.setHasVideo(project.hasVideo());
        dto.setHasFeaturedImage(project.getFeaturedImagePath() != null && !project.getFeaturedImagePath().isEmpty());
        dto.setHasKeyPhotos(project.hasKeyPhotos());
        dto.setDisplayDate(project.getDisplayDate());

        // URL для публичной части
        dto.setDetailUrl("/projects/" + project.getSlug());

        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        // Команда (преобразуем TeamMember в TeamMemberDTO через существующий маппер)
        if (project.getTeamMembers() != null) {
            List<TeamMemberDTO> teamMemberDTOs = project.getTeamMembers().stream()
                    .filter(TeamMember::isActive)
                    .map(teamMemberMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setTeamMembers(teamMemberDTOs);
        }

        // Партнеры (используем сущность Partner как в админке)
        if (project.getPartners() != null) {
            Set<Partner> partners = new HashSet<>(project.getPartners());
            dto.setPartners(partners);
        }

        // PhotoDTO для ключевых фото будет загружено отдельно через PhotoGalleryService
        // (как в админке в методах getAvailablePhotos и т.д.)

        return dto;
    }

    /**
     * Преобразует список проектов.
     */
    public List<ProjectDTO> toDTOList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        return projects.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Создает DTO для карточки (минимум данных).
     */
    public ProjectDTO toCardDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDTO dto = new ProjectDTO();

        // Только то, что нужно для карточки в списке
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setSlug(project.getSlug());
        dto.setShortDescription(project.getShortDescription());
        dto.setCategory(project.getCategory());
        dto.setFeaturedImagePath(project.getFeaturedImagePath());
        dto.setEventDate(project.getEventDate());
        dto.setLocation(project.getLocation());
        dto.setStatus(project.getStatus().name());
        dto.setCurrentlyActive(project.isCurrentlyActive());
        dto.setDetailUrl("/projects/" + project.getSlug());

        dto.setKeyPhotoIds(project.getKeyPhotoIds());  // ← ЭТО
        dto.setHasKeyPhotos(project.hasKeyPhotos());   // ← И ЭТО

        if (project.getEventDate() != null) {
            dto.setEventYear(project.getEventDate().getYear());
            dto.setEventMonth(project.getEventDate().getMonthValue());
            dto.setEventDay(project.getEventDate().getDayOfMonth());
        }

        return dto;
    }

    /**
     * Список DTO для карточек.
     */
    public List<ProjectDTO> toCardDTOList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        return projects.stream()
                .map(this::toCardDTO)
                .collect(Collectors.toList());
    }

    // ================== НОВЫЕ МЕТОДЫ ДЛЯ HomeController ==================

    /**
     * Создает полный DTO со всеми данными (команда, партнеры).
     * Используется для детальной страницы.
     */
    public ProjectDTO toFullDTO(Project project) {
        return toDetailDTO(project); // ИЗМЕНЯЕМ: теперь делегируем toDetailDTO
    }

    /**
     * Создает DTO для карусели (случайные проекты).
     */
    public ProjectDTO toCarouselDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDTO dto = new ProjectDTO();

        // Поля для карусели
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setSlug(project.getSlug());
        dto.setShortDescription(project.getShortDescription());
        dto.setFeaturedImagePath(project.getFeaturedImagePath());
        dto.setEventDate(project.getEventDate());
        dto.setLocation(project.getLocation());
        dto.setDetailUrl("/projects/" + project.getSlug());

        dto.setKeyPhotoIds(project.getKeyPhotoIds());
        dto.setHasKeyPhotos(project.hasKeyPhotos());

        // Загружаем фото для карусели
        List<PhotoGalleryDTO> keyPhotos = loadKeyPhotosForProject(project);
        dto.setKeyPhotos(keyPhotos);

        // Ограничиваем описание для карусели
        if (dto.getShortDescription() != null && dto.getShortDescription().length() > 150) {
            dto.setShortDescription(dto.getShortDescription().substring(0, 147) + "...");
        }

        return dto;
    }

    /**
     * Создает список DTO для карусели.
     */
    public List<ProjectDTO> toCarouselDTOList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        return projects.stream()
                .map(this::toCarouselDTO)
                .collect(Collectors.toList());
    }

    // ================== МЕТОД ДЛЯ ЗАГРУЗКИ ФОТО ==================

    /**
     * Загружает PhotoGalleryDTO по ID фотографии.
     * Использует PhotoGalleryService безопасным образом.
     */
    private PhotoGalleryDTO loadPhotoGalleryDTO(Long photoId) {
        try {
            // Используем существующий метод сервиса
            PhotoGalleryDTO photoDTO = photoGalleryService.getPhotoDTOById(photoId);

            // Убедимся, что thumbnailPath всегда есть
            if (photoDTO != null) {
                if (photoDTO.getThumbnailPath() == null || photoDTO.getThumbnailPath().isEmpty()) {
                    photoDTO.setThumbnailPath(photoDTO.getWebPath());
                }
            }

            return photoDTO;
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке фото ID=" + photoId + ": " + e.getMessage());

            // Возвращаем DTO-заглушку с корректными полями
            return createFallbackPhotoDTO(photoId);
        }
    }

    /**
     * Создает DTO-заглушку для фото.
     * Отдельный метод для повторного использования.
     */
    private PhotoGalleryDTO createFallbackPhotoDTO(Long photoId) {
        PhotoGalleryDTO fallback = new PhotoGalleryDTO();
        fallback.setPhotoId(photoId);
        fallback.setFileName("photo-" + photoId + ".jpg");
        fallback.setWebPath("/images/placeholder.jpg");
        fallback.setThumbnailPath("/images/placeholder.jpg");
        fallback.setTitle("Фото " + photoId);
        return fallback;
    }

    /**
     * Загружает ключевые фото для проекта.
     */
    public List<PhotoGalleryDTO> loadKeyPhotosForProject(Project project) {
        List<PhotoGalleryDTO> keyPhotos = new ArrayList<>();

        if (project != null && project.getKeyPhotoIds() != null) {
            for (Long photoId : project.getKeyPhotoIds()) {
                PhotoGalleryDTO photoDTO = loadPhotoGalleryDTO(photoId);
                if (photoDTO != null) {
                    keyPhotos.add(photoDTO);
                }
            }
        }

        return keyPhotos;
    }

    // Ищем в ProjectMapper.java после метода toCarouselDTOList() (примерно строка 155)

    // ================== МЕТОД ДЛЯ ДЕТАЛЬНОЙ СТРАНИЦЫ ==================

    /**
     * Создает DTO для детальной страницы проекта.
     * Включает все данные: ключевые фото, команду, партнеры.
     * Отдельный метод, чтобы не ломать существующую логику toDTO().
     */
    public ProjectDTO toDetailDTO(Project project) {
        if (project == null) {
            return null;
        }

        // Используем базовый toDTO() для основных полей
        ProjectDTO dto = toDTO(project);

        // ДОПОЛНИТЕЛЬНО загружаем ключевые фото только для детальной страницы
        List<PhotoGalleryDTO> keyPhotos = loadKeyPhotosForProject(project);
        dto.setKeyPhotos(keyPhotos);

        // Убедимся, что вычисляемые поля правильно установлены
        dto.setHasKeyPhotos(keyPhotos != null && !keyPhotos.isEmpty());

        return dto;
    }

    // ================== МЕТОДЫ ДЛЯ ПУБЛИЧНОЙ ЧАСТИ (без авторизации) ==================

    /**
     * Загружает ключевые фото для проекта с ПУБЛИЧНЫМИ путями.
     * Используется в публичной части сайта.
     */
    public List<PhotoGalleryDTO> loadPublicKeyPhotosForProject(Project project) {
        List<PhotoGalleryDTO> keyPhotos = loadKeyPhotosForProject(project);

        if (keyPhotos != null) {
            for (PhotoGalleryDTO photo : keyPhotos) {
                // Преобразуем путь контроллера в публичный путь
                String publicPath = convertToPublicPath(photo.getWebPath());
                photo.setPublicWebPath(publicPath);
            }
        }

        return keyPhotos;
    }

    /**
     * Создает DTO для карточки проекта с ПУБЛИЧНЫМИ путями.
     */
    public ProjectDTO toPublicCardDTO(Project project) {
        ProjectDTO dto = toCardDTO(project);

        // Загружаем ключевые фото с публичными путями
        List<PhotoGalleryDTO> keyPhotos = loadPublicKeyPhotosForProject(project);
        dto.setKeyPhotos(keyPhotos);

        return dto;
    }

    /**
     * Список DTO для карточек с ПУБЛИЧНЫМИ путями.
     */
    public List<ProjectDTO> toPublicCardDTOList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        return projects.stream()
                .map(this::toPublicCardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Создает DTO для карусели с ПУБЛИЧНЫМИ путями.
     */
    public ProjectDTO toPublicCarouselDTO(Project project) {
        ProjectDTO dto = toCarouselDTO(project);

        // Обновляем пути к фото на публичные
        if (dto.getKeyPhotos() != null) {
            for (PhotoGalleryDTO photo : dto.getKeyPhotos()) {
                String publicPath = convertToPublicPath(photo.getWebPath());
                photo.setPublicWebPath(publicPath);
            }
        }

        return dto;
    }

    /**
     * Список DTO для карусели с ПУБЛИЧНЫМИ путями.
     */
    public List<ProjectDTO> toPublicCarouselDTOList(List<Project> projects) {
        if (projects == null) {
            return new ArrayList<>();
        }

        return projects.stream()
                .map(this::toPublicCarouselDTO)
                .collect(Collectors.toList());
    }

    /**
     * Создает ПОЛНЫЙ DTO для детальной страницы с ПУБЛИЧНЫМИ путями.
     */
    public ProjectDTO toPublicDetailDTO(Project project) {
        ProjectDTO dto = toDetailDTO(project);

        // Обновляем пути к фото на публичные
        if (dto.getKeyPhotos() != null) {
            for (PhotoGalleryDTO photo : dto.getKeyPhotos()) {
                String publicPath = convertToPublicPath(photo.getWebPath());
                photo.setPublicWebPath(publicPath);
            }
        }

        return dto;
    }

    /**
     * Преобразует путь контроллера в публичный статический путь.
     * Безопасно работает с любыми форматами путей.
     */
    private String convertToPublicPath(String webPath) {
        if (webPath == null || webPath.isEmpty()) {
            return "/images/placeholder.jpg";
        }

        // Если путь уже публичный (/uploads/), оставляем как есть
        if (webPath.startsWith("/uploads/")) {
            return webPath;
        }

        // Преобразуем путь контроллера (/admin/photo-gallery/image/filename)
        // в публичный путь (/uploads/filename)
        if (webPath.startsWith("/admin/photo-gallery/image/")) {
            String filename = webPath.substring("/admin/photo-gallery/image/".length());

            // Удаляем параметры запроса если есть
            if (filename.contains("?")) {
                filename = filename.substring(0, filename.indexOf("?"));
            }

            return "/uploads/" + filename;
        }

        // Если это относительный путь без /uploads/, добавляем префикс
        if (!webPath.startsWith("/") && !webPath.startsWith("http")) {
            return "/uploads/" + webPath;
        }

        // Для других путей возвращаем как есть
        return webPath;
    }
}