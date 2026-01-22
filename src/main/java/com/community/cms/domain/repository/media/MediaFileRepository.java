package com.community.cms.domain.repository.media;

import com.community.cms.domain.model.media.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с медиафайлами в базе данных.
 * Repository for working with media files in the database.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    /**
     * Находит медиафайл по имени файла.
     * Finds media file by file name.
     *
     * @param fileName имя файла / file name
     * @return Optional с медиафайлом / Optional with media file
     */
    Optional<MediaFile> findByFileName(String fileName);

    /**
     * Находит медиафайлы по пути.
     * Finds media files by path.
     *
     * @param filePath путь к файлу / file path
     * @return список медиафайлов / list of media files
     */
    List<MediaFile> findByFilePath(String filePath);

    /**
     * Находит медиафайлы по типу.
     * Finds media files by type.
     *
     * @param fileType тип файла / file type
     * @return список медиафайлов / list of media files
     */
    List<MediaFile> findByFileType(MediaFile.FileType fileType);

    /**
     * Проверяет существует ли файл с таким именем.
     * Checks if file exists with given name.
     *
     * @param fileName имя файла / file name
     * @return true если файл существует / true if file exists
     */
    boolean existsByFileName(String fileName);

    /**
     * Находит медиафайлы по MIME типу.
     * Finds media files by MIME type.
     *
     * @param mimeType MIME тип / MIME type
     * @return список медиафайлов / list of media files
     */
    List<MediaFile> findByMimeType(String mimeType);

    /**
     * Находит основные медиафайлы.
     * Finds primary media files.
     *
     * @param isPrimary является ли основным / is primary
     * @return список медиафайлов / list of media files
     */
    List<MediaFile> findByIsPrimary(Boolean isPrimary);

    /**
     * Находит медиафайлы больше указанного размера.
     * Finds media files larger than specified size.
     *
     * @param size размер в байтах / size in bytes
     * @return список медиафайлов / list of media files
     */
    List<MediaFile> findByFileSizeGreaterThan(Long size);

    /**
     * Удаляет медиафайлы по пути.
     * Deletes media files by path.
     *
     * @param filePath путь к файлу / file path
     * @return количество удаленных файлов / number of deleted files
     */
    long deleteByFilePath(String filePath);
}
