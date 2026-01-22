package com.community.cms.web.mvc.mapper.content;

import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.web.mvc.dto.content.PhotoGalleryDTO;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования сущностей фото-галереи в DTO.
 * Mapper for transforming photo gallery entities to DTO.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Component
public class PhotoGalleryMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Преобразует PhotoGallery в PhotoGalleryDTO для публичной галереи.
     * Converts PhotoGallery to PhotoGalleryDTO for public gallery.
     *
     * @param gallery галерея для преобразования / gallery to convert
     * @return DTO галереи / gallery DTO
     */
    public PhotoGalleryDTO toPublicGalleryDTO(PhotoGallery gallery) {
        if (gallery == null) {
            return null;
        }

        PhotoGalleryDTO dto = new PhotoGalleryDTO();
        dto.setId(gallery.getId());
        dto.setTitle(gallery.getTitle());
        dto.setYear(gallery.getYear());
        dto.setDescription(gallery.getDescription());
        dto.setPhotoCount(gallery.getImagesCount());
        dto.setPublished(gallery.getPublished());

        // Категории
        if (gallery.getCategories() != null && !gallery.getCategories().isEmpty()) {
            List<String> categoryNames = gallery.getCategoryNames();
            // Можно добавить поле для категорий если нужно
        }

        // Изображения (только базовые данные для списка)
        if (gallery.getImages() != null && !gallery.getImages().isEmpty()) {
            List<PhotoGalleryDTO> imageDTOs = gallery.getImages().stream()
                    .map(this::toSimpleImageDTO)
                    .collect(Collectors.toList());
            dto.setImages(imageDTOs);
        }

        // Даты
        if (gallery.getCreatedAt() != null) {
            // Можно добавить форматированную дату если нужно
        }

        return dto;
    }

    /**
     * Преобразует MediaFile в простой PhotoGalleryDTO для изображения.
     * Converts MediaFile to simple PhotoGalleryDTO for image.
     *
     * @param mediaFile медиафайл для преобразования / media file to convert
     * @return DTO изображения / image DTO
     */
    public PhotoGalleryDTO toSimpleImageDTO(MediaFile mediaFile) {
        if (mediaFile == null) {
            return null;
        }

        PhotoGalleryDTO dto = new PhotoGalleryDTO();
        dto.setPhotoId(mediaFile.getId());
        dto.setFileName(mediaFile.getFileName());
        dto.setWebPath(mediaFile.getWebPath());
        dto.setThumbnailPath(mediaFile.getWebPath()); // Используем тот же путь для миниатюры
        dto.setIsPrimary(mediaFile.getIsPrimary());

        return dto;
    }

    /**
     * Преобразует MediaFile в полный PhotoGalleryDTO для изображения с информацией о галерее.
     * Converts MediaFile to full PhotoGalleryDTO for image with gallery information.
     *
     * @param mediaFile медиафайл для преобразования / media file to convert
     * @param gallery галерея изображения / image gallery
     * @return полный DTO изображения / full image DTO
     */
    public PhotoGalleryDTO toFullImageDTO(MediaFile mediaFile, PhotoGallery gallery) {
        if (mediaFile == null) {
            return null;
        }

        PhotoGalleryDTO dto = toSimpleImageDTO(mediaFile);

        if (gallery != null) {
            dto.setGalleryId(gallery.getId());
            dto.setGalleryTitle(gallery.getTitle());
            dto.setGalleryYear(gallery.getYear());
        }

        return dto;
    }

    /**
     * Преобразует список галерей в список DTO для публичного отображения.
     * Converts list of galleries to list of DTOs for public display.
     *
     * @param galleries список галерей / list of galleries
     * @return список DTO / list of DTOs
     */
    public List<PhotoGalleryDTO> toPublicGalleryDTOList(List<PhotoGallery> galleries) {
        if (galleries == null) {
            return List.of();
        }

        return galleries.stream()
                .map(this::toPublicGalleryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список медиафайлов в список DTO для изображений.
     * Converts list of media files to list of DTOs for images.
     *
     * @param mediaFiles список медиафайлов / list of media files
     * @return список DTO изображений / list of image DTOs
     */
    public List<PhotoGalleryDTO> toSimpleImageDTOList(List<MediaFile> mediaFiles) {
        if (mediaFiles == null) {
            return List.of();
        }

        return mediaFiles.stream()
                .map(this::toSimpleImageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует PhotoGallery в DTO для карточки (минимальные данные).
     * Converts PhotoGallery to DTO for card (minimal data).
     *
     * @param gallery галерея для преобразования / gallery to convert
     * @return DTO для карточки / card DTO
     */
    public PhotoGalleryDTO toCardDTO(PhotoGallery gallery) {
        if (gallery == null) {
            return null;
        }

        return new PhotoGalleryDTO(
                gallery.getId(),
                gallery.getTitle(),
                gallery.getYear(),
                gallery.getImagesCount()
        );
    }

    /**
     * Преобразует список галерей в список DTO для карточек.
     * Converts list of galleries to list of DTOs for cards.
     *
     * @param galleries список галерей / list of galleries
     * @return список DTO карточек / list of card DTOs
     */
    public List<PhotoGalleryDTO> toCardDTOList(List<PhotoGallery> galleries) {
        if (galleries == null) {
            return List.of();
        }

        return galleries.stream()
                .map(this::toCardDTO)
                .collect(Collectors.toList());
    }
}
