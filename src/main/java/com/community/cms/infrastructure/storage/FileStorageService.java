package com.community.cms.infrastructure.storage;

import com.community.cms.domain.enums.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления файловым хранилищем
 * Service for file storage management
 * Обеспечивает загрузку, удаление и валидацию медиафайлов
 * Provides upload, deletion and validation of media files
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.allowed-image-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedImageTypes;

    @Value("${file.allowed-video-types:video/mp4,video/webm,video/ogg}")
    private String allowedVideoTypes;

    @Value("${file.max-image-size:10485760}") // 10MB
    private long maxImageSize;

    @Value("${file.max-video-size:52428800}") // 50MB
    private long maxVideoSize;

    @Value("${file.max-files-per-item:20}")
    private int maxFilesPerItem;

    /**
     * Сохраняет один файл
     * Stores single file
     *
     * @param file файл для сохранения / file to store
     * @return имя сохраненного файла / saved file name
     * @throws IOException ошибка ввода-вывода / IO error
     * @throws FileStorageException ошибка валидации / validation error
     */
    public String storeFile(MultipartFile file) throws IOException, FileStorageException {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = generateUniqueFileName(fileExtension);

        // ↓↓↓ ТОЛЬКО В UPLOADS ↓↓↓
        Path uploadPath = Paths.get(uploadDir).resolve(fileName).normalize();
        Files.createDirectories(uploadPath.getParent());
        Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * Сохраняет несколько файлов
     * Stores multiple files
     *
     * @param files массив файлов / array of files
     * @return список имен сохраненных файлов / list of saved file names
     * @throws IOException ошибка ввода-вывода / IO error
     * @throws FileStorageException ошибка валидации / validation error
     */
    public List<String> storeFiles(MultipartFile[] files) throws IOException, FileStorageException {
        List<String> storedFileNames = new ArrayList<>();

        if (files == null || files.length == 0) {
            return storedFileNames;
        }

        // Проверяем лимит файлов / Check file limit
        if (files.length > maxFilesPerItem) {
            throw new FileStorageException(
                    "Превышено максимальное количество файлов: " + maxFilesPerItem +
                            " / Maximum files exceeded: " + maxFilesPerItem
            );
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileName = storeFile(file);
                storedFileNames.add(fileName);
            }
        }

        return storedFileNames;
    }

    /**
     * Удаляет файл
     * Deletes file
     *
     * @param fileName имя файла для удаления / file name to delete
     * @throws IOException ошибка ввода-вывода / IO error
     */
    public void deleteFile(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();

        // Проверяем что файл находится в разрешенной директории
        // Check that file is in allowed directory
        Path uploadPath = Paths.get(uploadDir).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new SecurityException("Попытка доступа к файлу вне разрешенной директории / Attempt to access file outside allowed directory");
        }

        Files.deleteIfExists(filePath);
    }

    /**
     * Удаляет несколько файлов
     * Deletes multiple files
     *
     * @param fileNames список имен файлов / list of file names
     * @throws IOException ошибка ввода-вывода / IO error
     */
    public void deleteFiles(List<String> fileNames) throws IOException {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        for (String fileName : fileNames) {
            deleteFile(fileName);
        }
    }

    /**
     * Загружает файл как ресурс
     * Loads file as resource
     *
     * @param fileName имя файла / file name
     * @return путь к файлу / file path
     * @throws FileNotFoundException файл не найден / file not found
     */
    public Path loadFile(String fileName) throws FileNotFoundException {
        if (fileName == null || fileName.isEmpty()) {
            throw new FileNotFoundException("Имя файла не указано / File name not specified");
        }

        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();

        // Проверяем безопасность пути / Check path security
        Path uploadPath = Paths.get(uploadDir).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new SecurityException("Попытка доступа к файлу вне разрешенной директории / Attempt to access file outside allowed directory");
        }

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Файл не найден: " + fileName + " / File not found: " + fileName);
        }

        return filePath;
    }

    /**
     * Проверяет валидность файла
     * Validates file
     *
     * @param file файл для проверки / file to validate
     * @throws FileStorageException ошибка валидации / validation error
     */
    public void validateFile(MultipartFile file) throws FileStorageException {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Файл пуст или не существует / File is empty or doesn't exist");
        }

        // Проверяем имя файла / Check file name
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.contains("..")) {
            throw new FileStorageException("Некорректное имя файла: " + originalFileName + " / Invalid file name: " + originalFileName);
        }

        // Проверяем тип файла / Check file type
        String contentType = file.getContentType();
        MediaType mediaType = determineMediaType(contentType);

        if (mediaType == MediaType.PHOTO) {
            validateImageFile(file, contentType);
        } else if (mediaType == MediaType.VIDEO) {
            validateVideoFile(file, contentType);
        } else {
            throw new FileStorageException("Неподдерживаемый тип файла: " + contentType + " / Unsupported file type: " + contentType);
        }
    }

    /**
     * Проверяет изображение
     * Validates image
     *
     * @param file файл изображения / image file
     * @param contentType MIME тип / MIME type
     * @throws FileStorageException ошибка валидации / validation error
     */
    private void validateImageFile(MultipartFile file, String contentType) throws FileStorageException {
        if (!isAllowedImageType(contentType)) {
            throw new FileStorageException(
                    "Неподдерживаемый тип изображения: " + contentType +
                            ". Разрешенные типы: " + allowedImageTypes +
                            " / Unsupported image type: " + contentType +
                            ". Allowed types: " + allowedImageTypes
            );
        }

        if (file.getSize() > maxImageSize) {
            throw new FileStorageException(
                    "Размер изображения превышает допустимый: " + (maxImageSize / 1024 / 1024) + "MB" +
                            " / Image size exceeds maximum: " + (maxImageSize / 1024 / 1024) + "MB"
            );
        }
    }

    /**
     * Проверяет видео
     * Validates video
     *
     * @param file видео файл / video file
     * @param contentType MIME тип / MIME type
     * @throws FileStorageException ошибка валидации / validation error
     */
    private void validateVideoFile(MultipartFile file, String contentType) throws FileStorageException {
        if (!isAllowedVideoType(contentType)) {
            throw new FileStorageException(
                    "Неподдерживаемый тип видео: " + contentType +
                            ". Разрешенные типы: " + allowedVideoTypes +
                            " / Unsupported video type: " + contentType +
                            ". Allowed types: " + allowedVideoTypes
            );
        }

        if (file.getSize() > maxVideoSize) {
            throw new FileStorageException(
                    "Размер видео превышает допустимый: " + (maxVideoSize / 1024 / 1024) + "MB" +
                            " / Video size exceeds maximum: " + (maxVideoSize / 1024 / 1024) + "MB"
            );
        }
    }

    /**
     * Определяет тип медиа по MIME типу
     * Determines media type by MIME type
     *
     * @param contentType MIME тип / MIME type
     * @return тип медиа / media type
     */
    public MediaType determineMediaType(String contentType) {
        if (contentType == null) {
            return MediaType.PHOTO; // По умолчанию / Default
        }

        if (contentType.startsWith("image/")) {
            return MediaType.PHOTO;
        } else if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        } else {
            // Пытаемся определить по расширению / Try to determine by extension
            return MediaType.PHOTO;
        }
    }

    /**
     * Проверяет разрешен ли тип изображения
     * Checks if image type is allowed
     *
     * @param contentType MIME тип / MIME type
     * @return true если разрешен / true if allowed
     */
    private boolean isAllowedImageType(String contentType) {
        if (contentType == null) return false;

        String[] allowedTypes = allowedImageTypes.split(",");
        for (String allowedType : allowedTypes) {
            if (allowedType.trim().equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет разрешен ли тип видео
     * Checks if video type is allowed
     *
     * @param contentType MIME тип / MIME type
     * @return true если разрешен / true if allowed
     */
    private boolean isAllowedVideoType(String contentType) {
        if (contentType == null) return false;

        String[] allowedTypes = allowedVideoTypes.split(",");
        for (String allowedType : allowedTypes) {
            if (allowedType.trim().equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Генерирует уникальное имя файла
     * Generates unique file name
     *
     * @param fileExtension расширение файла / file extension
     * @return уникальное имя файла / unique file name
     */
    private String generateUniqueFileName(String fileExtension) {
        return UUID.randomUUID().toString() + fileExtension;
    }

    /**
     * Извлекает расширение файла
     * Extracts file extension
     *
     * @param fileName имя файла / file name
     * @return расширение файла / file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".dat";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    /**
     * Получает MIME тип файла
     * Gets file MIME type
     *
     * @param file файл / file
     * @return MIME тип / MIME type
     */
    public String getFileContentType(MultipartFile file) {
        return file.getContentType();
    }

    /**
     * Получает размер файла
     * Gets file size
     *
     * @param file файл / file
     * @return размер в байтах / size in bytes
     */
    public long getFileSize(MultipartFile file) {
        return file.getSize();
    }

    /**
     * Проверяет существует ли файл
     * Checks if file exists
     *
     * @param fileName имя файла / file name
     * @return true если существует / true if exists
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получает общий размер всех файлов в хранилище
     * Gets total size of all files in storage
     *
     * @return общий размер в байтах / total size in bytes
     * @throws IOException ошибка ввода-вывода / IO error
     */
    public long getTotalStorageSize() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            return 0;
        }

        return Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
    }

    /**
     * Исключение для ошибок файлового хранилища
     * Exception for file storage errors
     */
    public static class FileStorageException extends Exception {
        public FileStorageException(String message) {
            super(message);
        }

        public FileStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение для ненайденных файлов
     * Exception for file not found
     */
    public static class FileNotFoundException extends Exception {
        public FileNotFoundException(String message) {
            super(message);
        }

        public FileNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}