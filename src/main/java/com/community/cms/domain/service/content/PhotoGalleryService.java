package com.community.cms.domain.service.content;

import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.domain.model.media.PublicationCategory;
import com.community.cms.domain.repository.media.MediaFileRepository;
import com.community.cms.domain.repository.content.PhotoGalleryRepository;
import com.community.cms.domain.repository.media.PublicationCategoryRepository;
import com.community.cms.infrastructure.storage.FileStorageService;
import com.community.cms.web.mvc.dto.content.PhotoGalleryDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Сервис для работы с фото-галереей.
 * Управляет элементами галереи, изображениями и категориями публикации.
 *
 * Service for working with photo gallery.
 * Manages gallery items, images and publication categories.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Service
@Transactional
public class PhotoGalleryService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGalleryService.class);

    // Максимальное количество изображений на элемент
    private static final int MAX_IMAGES_PER_ITEM = 15;
    // Максимальный размер изображения (5MB)
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final PhotoGalleryRepository photoGalleryItemRepository;
    private final MediaFileRepository mediaFileRepository;
    private final PublicationCategoryRepository publicationCategoryRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PhotoGalleryService(PhotoGalleryRepository photoGalleryItemRepository,
                               MediaFileRepository mediaFileRepository,
                               PublicationCategoryRepository publicationCategoryRepository,
                               FileStorageService fileStorageService) {
        this.photoGalleryItemRepository = photoGalleryItemRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.publicationCategoryRepository = publicationCategoryRepository;
        this.fileStorageService = fileStorageService;
    }

    // ========== CRUD МЕТОДЫ ==========

    /**
     * Создает новый элемент фото-галереи.
     * Creates new photo gallery item.
     *
     * @param item элемент для создания / item to create
     * @return созданный элемент / created item
     */
    public PhotoGallery createPhotoGalleryItem(PhotoGallery item) {
        logger.info("Создание элемента фото-галереи: {}", item.getTitle());

        // Валидация бизнес-правил
        validatePhotoGalleryItem(item);

        // Сохраняем элемент
        PhotoGallery savedItem = photoGalleryItemRepository.save(item);

        logger.info("Элемент создан успешно. ID: {}", savedItem.getId());
        return savedItem;
    }

    /**
     * Создает элемент фото-галереи с загрузкой изображений.
     * Creates photo gallery item with image upload.
     *
     * @param item элемент для создания / item to create
     * @param images массив изображений / array of images
     * @return созданный элемент / created item
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public PhotoGallery createPhotoGalleryItemWithImages(PhotoGallery item, MultipartFile[] images)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Создание элемента с {} изображениями", images != null ? images.length : 0);

        // Валидация количества изображений
        if (images != null && images.length > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Максимальное количество изображений: %d", MAX_IMAGES_PER_ITEM)
            );
        }

        // Сначала сохраняем элемент
        PhotoGallery savedItem = createPhotoGalleryItem(item);

        // Затем добавляем изображения если есть
        if (images != null && images.length > 0) {
            addImagesToPhotoGalleryItem(savedItem.getId(), images);
            // Перезагружаем элемент с изображениями
            savedItem = getPhotoGalleryItemById(savedItem.getId());
        }

        return savedItem;
    }

    /**
     * Обновляет существующий элемент фото-галереи.
     * Updates existing photo gallery item.
     *
     * @param id ID элемента / item ID
     * @param item данные для обновления / data to update
     * @return обновленный элемент / updated item
     */
    public PhotoGallery updatePhotoGalleryItem(Long id, PhotoGallery item) {
        logger.info("Обновление элемента фото-галереи. ID: {}", id);

        // Получаем существующий элемент из базы данных
        PhotoGallery existingItem = getPhotoGalleryItemById(id);

        // Обновляем только изменяемые поля, НЕ ТРОГАЯ изображения

        // 1. Текстовые поля
        existingItem.setTitle(item.getTitle());
        existingItem.setDescription(item.getDescription());
        existingItem.setYear(item.getYear());

        // 2. Статус публикации
        existingItem.setPublished(item.getPublished());

        // 3. Категории (если переданы в item)
        if (item.getCategories() != null && !item.getCategories().isEmpty()) {
            // Очищаем старые категории и добавляем новые
            existingItem.getCategories().clear();
            existingItem.getCategories().addAll(item.getCategories());
        }

        // 4. Обновляем дату изменения
        existingItem.setUpdatedAt(LocalDateTime.now());

        // Валидация бизнес-правил (на обновленном объекте)
        validatePhotoGalleryItem(existingItem);

        // Сохраняем обновленный элемент (со старыми изображениями)
        PhotoGallery updatedItem = photoGalleryItemRepository.save(existingItem);

        logger.info("Элемент обновлен успешно. ID: {}", updatedItem.getId());
        return updatedItem;
    }



    // Добавляем в PhotoGalleryService.java:

    /**
     * Обновляет элемент фото-галереи с сохранением указанных изображений.
     * Updates photo gallery item while keeping specified images.
     *
     * @param id ID элемента / item ID
     * @param item данные для обновления / data to update
     * @param keepImageIds список ID изображений для сохранения / list of image IDs to keep
     * @return обновленный элемент / updated item
     */
    public PhotoGallery updatePhotoGalleryItem(Long id, PhotoGallery item, List<Long> keepImageIds) {
        logger.info("Обновление элемента с сохранением изображений. ID: {}, сохраняемые: {}",
                id, keepImageIds);

        PhotoGallery existingItem = getPhotoGalleryItemById(id);

        // Обновляем поля
        existingItem.setTitle(item.getTitle());
        existingItem.setDescription(item.getDescription());
        existingItem.setYear(item.getYear());
        existingItem.setPublished(item.getPublished());
        existingItem.setCategories(item.getCategories());
        existingItem.setUpdatedAt(LocalDateTime.now());

        // Сохраняем только указанные изображения
        if (keepImageIds != null && !keepImageIds.isEmpty()) {
            // Удаляем изображения, которых нет в списке keepImageIds
            Iterator<MediaFile> iterator = existingItem.getImages().iterator();
            while (iterator.hasNext()) {
                MediaFile image = iterator.next();
                if (!keepImageIds.contains(image.getId())) {
                    try {
                        // Удаляем файл из файловой системы
                        fileStorageService.deleteFile(image.getFilePath());
                        iterator.remove();
                        logger.info("Удалено изображение: {}", image.getId());
                    } catch (Exception e) {
                        logger.error("Ошибка при удалении файла {}: {}",
                                image.getFilePath(), e.getMessage());
                    }
                }
            }
        }

        return photoGalleryItemRepository.save(existingItem);
    }

    /**
     * Обновляет элемент фото-галереи с загрузкой новых изображений и сохранением старых.
     * Updates photo gallery item with new images while keeping old ones.
     *
     * @param id ID элемента / item ID
     * @param item данные для обновления / data to update
     * @param newImages новые изображения / new images
     * @param keepImageIds список ID старых изображений для сохранения / list of old image IDs to keep
     * @return обновленный элемент / updated item
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public PhotoGallery updatePhotoGalleryItemWithImages(Long id, PhotoGallery item,
                                                         MultipartFile[] newImages, List<Long> keepImageIds)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Обновление элемента с новыми изображениями. ID: {}, новые файлы: {}, сохраняемые: {}",
                id, newImages != null ? newImages.length : 0, keepImageIds);

        // Фильтруем null и пустые файлы
        List<MultipartFile> validNewImages = new ArrayList<>();
        if (newImages != null) {
            for (MultipartFile image : newImages) {
                if (image != null && !image.isEmpty() && image.getSize() > 0) {
                    validNewImages.add(image);
                }
            }
        }

        // Получаем текущий элемент
        PhotoGallery existingItem = getPhotoGalleryItemById(id);

        // Проверяем общее количество изображений
        int currentImageCount = keepImageIds != null ? keepImageIds.size() : existingItem.getImagesCount();
        if (currentImageCount + validNewImages.size() > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышено максимальное количество изображений. Сохраняемые: %d, новые: %d, максимум: %d",
                            currentImageCount, validNewImages.size(), MAX_IMAGES_PER_ITEM)
            );
        }

        // Сначала удаляем изображения, которые не в списке keepImageIds
        if (keepImageIds != null && !keepImageIds.isEmpty()) {
            Iterator<MediaFile> iterator = existingItem.getImages().iterator();
            while (iterator.hasNext()) {
                MediaFile image = iterator.next();
                if (!keepImageIds.contains(image.getId())) {
                    try {
                        fileStorageService.deleteFile(image.getFilePath());
                        iterator.remove();
                        logger.info("Удалено изображение: {}", image.getId());
                    } catch (Exception e) {
                        logger.error("Ошибка при удалении файла {}: {}",
                                image.getFilePath(), e.getMessage());
                    }
                }
            }
        }

        // Обновляем остальные поля
        existingItem.setTitle(item.getTitle());
        existingItem.setDescription(item.getDescription());
        existingItem.setYear(item.getYear());
        existingItem.setPublished(item.getPublished());
        existingItem.setCategories(item.getCategories());
        existingItem.setUpdatedAt(LocalDateTime.now());

        // Добавляем новые изображения
        if (!validNewImages.isEmpty()) {
            addImagesToPhotoGalleryItem(existingItem.getId(),
                    validNewImages.toArray(new MultipartFile[0]));
        }

        // Сохраняем и возвращаем обновленный элемент
        PhotoGallery updatedItem = photoGalleryItemRepository.save(existingItem);
        logger.info("Элемент успешно обновлен. ID: {}, всего изображений: {}",
                id, updatedItem.getImagesCount());

        return updatedItem;
    }




    /**
     * Обновляет элемент фото-галереи с загрузкой новых изображений.
     * Updates photo gallery item with new image upload.
     *
     * @param id ID элемента / item ID
     * @param item данные для обновления / data to update
     * @param newImages новые изображения / new images
     * @return обновленный элемент / updated item
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public PhotoGallery updatePhotoGalleryItemWithImages(Long id, PhotoGallery item, MultipartFile[] newImages)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Обновление элемента с новыми изображениями. ID: {}, новые файлы: {}",
                id, newImages != null ? newImages.length : 0);

        // ПРОВЕРКА: Если нет новых изображений - просто обновляем элемент
        if (newImages == null || newImages.length == 0) {
            logger.info("Нет новых изображений, выполняем обычное обновление элемента ID: {}", id);
            return updatePhotoGalleryItem(id, item);
        }

        // Фильтруем null и пустые файлы
        List<MultipartFile> validNewImages = new ArrayList<>();
        for (MultipartFile image : newImages) {
            if (image != null && !image.isEmpty() && image.getSize() > 0) {
                validNewImages.add(image);
            }
        }

        // Если после фильтрации не осталось валидных изображений
        if (validNewImages.isEmpty()) {
            logger.info("После фильтрации не осталось валидных изображений. Обычное обновление элемента ID: {}", id);
            return updatePhotoGalleryItem(id, item);
        }

        // Получаем текущий элемент для проверки лимита
        PhotoGallery existingItem = getPhotoGalleryItemById(id);

        // Проверяем общее количество изображений с учётом валидных новых
        int currentImageCount = existingItem.getImagesCount();
        if (currentImageCount + validNewImages.size() > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышено максимальное количество изображений. Текущее: %d, новые: %d, максимум: %d",
                            currentImageCount, validNewImages.size(), MAX_IMAGES_PER_ITEM)
            );
        }

        // Обновляем элемент
        PhotoGallery updatedItem = updatePhotoGalleryItem(id, item);

        // Добавляем валидные новые изображения
        addImagesToPhotoGalleryItem(id, validNewImages.toArray(new MultipartFile[0]));

        // Перезагружаем элемент с изображениями
        return getPhotoGalleryItemById(id);
    }

    /**
     * Получает элемент фото-галереи по ID.
     * Gets photo gallery item by ID.
     *
     * @param id ID элемента / item ID
     * @return элемент или null если не найден / item or null if not found
     */
    @Transactional(readOnly = true)
    public PhotoGallery getPhotoGalleryItemById(Long id) {
        return photoGalleryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Элемент фото-галереи не найден. ID: " + id));
    }

    /**
     * Получает все элементы фото-галереи.
     * Gets all photo gallery items.
     *
     * @return список всех элементов / list of all items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getAllPhotoGalleryItems() {
        return photoGalleryItemRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Удаляет элемент фото-галереи.
     * Deletes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @throws IOException если произошла ошибка при удалении файлов / if file deletion error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при удалении файлов / if file deletion error occurs
     */
    public void deletePhotoGalleryItem(Long id) throws IOException, FileStorageService.FileStorageException {
        logger.info("Удаление элемента фото-галереи. ID: {}", id);

        PhotoGallery item = getPhotoGalleryItemById(id);

        // Удаляем файлы изображений
        deleteAllItemImages(item);

        // Удаляем элемент из базы данных
        photoGalleryItemRepository.deleteById(id);

        logger.info("Элемент удален успешно. ID: {}", id);
    }

    // ========== МЕТОДЫ РАБОТЫ С ИЗОБРАЖЕНИЯМИ ==========

    /**
     * Добавляет изображения к элементу фото-галереи.
     * Adds images to photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param images массив изображений / array of images
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public void addImagesToPhotoGalleryItem(Long itemId, MultipartFile[] images)
            throws IOException, FileStorageService.FileStorageException {

        // ДОБАВИТЬ: Проверка на null или пустой массив
        if (images == null || images.length == 0) {
            logger.warn("Попытка добавить пустой массив изображений для элемента ID: {}", itemId);
            return; // Ничего не делаем
        }

        logger.info("Добавление изображений к элементу. ID: {}, количество: {}", itemId, images.length);

        PhotoGallery item = getPhotoGalleryItemById(itemId);

        // ДОБАВИТЬ: Фильтрация null файлов
        List<MultipartFile> validImages = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                validImages.add(image);
            }
        }

        if (validImages.isEmpty()) {
            logger.warn("Нет валидных изображений для добавления. Элемент ID: {}", itemId);
            return;
        }

        // Проверяем лимит изображений с учётом отфильтрованных
        if (item.getImagesCount() + validImages.size() > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышен лимит изображений. Текущее: %d, добавляемые: %d, максимум: %d",
                            item.getImagesCount(), validImages.size(), MAX_IMAGES_PER_ITEM)
            );
        }

        // Загружаем только валидные файлы
        List<String> storedFilePaths = fileStorageService.storeFiles(
                validImages.toArray(new MultipartFile[0])
        );

        List<MediaFile> mediaFiles = new ArrayList<>();

        for (int i = 0; i < validImages.size(); i++) {
            MultipartFile image = validImages.get(i);
            String storedFilePath = storedFilePaths.get(i);

            // Валидация типа файла
            if (!isValidImageFile(image)) {
                throw new IllegalArgumentException("Файл не является изображением: " + image.getOriginalFilename());
            }

            // Валидация размера файла
            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException(
                        String.format("Размер файла превышает лимит (%d MB): %s",
                                MAX_IMAGE_SIZE / (1024 * 1024), image.getOriginalFilename())
                );
            }

            // Создаем MediaFile
            MediaFile mediaFile = new MediaFile(
                    image.getOriginalFilename(),
                    storedFilePath,
                    image.getContentType(),
                    image.getSize()
            );

            // Устанавливаем порядок сортировки
            mediaFile.setSortOrder(item.getImagesCount() + i);

            // Если это первое изображение элемента, устанавливаем как основное
            if (item.getImagesCount() == 0 && i == 0) {
                mediaFile.setIsPrimary(true);
            }

            mediaFiles.add(mediaFile);
        }

        // Добавляем изображения к элементу
        item.addImages(mediaFiles);

        // Сохраняем элемент
        photoGalleryItemRepository.save(item);

        logger.info("Изображения успешно добавлены к элементу. ID: {}", itemId);
    }

    /**
     * Удаляет изображение из элемента фото-галереи.
     * Removes image from photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param imageId ID изображения / image ID
     * @return true если изображение было удалено / true if image was removed
     * @throws IOException если произошла ошибка при удалении файла / if file deletion error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при удалении файла / if file deletion error occurs
     */
    public boolean removeImageFromPhotoGalleryItem(Long itemId, Long imageId)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Удаление изображения. Элемент ID: {}, изображение ID: {}", itemId, imageId);

        PhotoGallery item = getPhotoGalleryItemById(itemId);
        MediaFile image = item.getImageById(imageId);

        if (image == null) {
            logger.warn("Изображение не найдено. Элемент ID: {}, изображение ID: {}", itemId, imageId);
            return false;
        }

        // Удаляем файл из файловой системы
        fileStorageService.deleteFile(image.getFilePath());

        // Удаляем изображение из элемента
        boolean removed = item.removeImageById(imageId);

        if (removed) {
            // Сохраняем изменения
            photoGalleryItemRepository.save(item);
            logger.info("Изображение удалено успешно. Элемент ID: {}, изображение ID: {}", itemId, imageId);
        }

        return removed;
    }

    /**
     * Устанавливает основное изображение элемента.
     * Sets primary image for item.
     *
     * @param itemId ID элемента / item ID
     * @param imageId ID изображения / image ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean setPrimaryImage(Long itemId, Long imageId) {
        logger.info("Установка основного изображения. Элемент ID: {}, изображение ID: {}", itemId, imageId);

        PhotoGallery item = getPhotoGalleryItemById(itemId);
        boolean success = item.setPrimaryImageById(imageId);

        if (success) {
            // Сохраняем изменения
            photoGalleryItemRepository.save(item);
            logger.info("Основное изображение установлено успешно. Элемент ID: {}, изображение ID: {}", itemId, imageId);
        } else {
            logger.warn("Не удалось установить основное изображение. Элемент ID: {}, изображение ID: {}", itemId, imageId);
        }

        return success;
    }

    /**
     * Удаляет все изображения элемента.
     * Deletes all item images.
     *
     * @param item элемент / item
     * @throws IOException если произошла ошибка при удалении файлов / if file deletion error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при удалении файлов / if file storage error occurs
     */
    private void deleteAllItemImages(PhotoGallery item)
            throws IOException, FileStorageService.FileStorageException {
        if (item.getImagesCount() == 0) {
            return;
        }

        for (MediaFile image : item.getImages()) {
            try {
                fileStorageService.deleteFile(image.getFilePath());
                logger.debug("Файл удален: {}", image.getFilePath());
            } catch (Exception e) {
                logger.error("Ошибка при удалении файла: {}", image.getFilePath(), e);
                // Продолжаем удаление других файлов
                // Continue deleting other files
            }
        }
    }

    // ========== МЕТОДЫ РАБОТЫ С КАТЕГОРИЯМИ ==========

    /**
     * Добавляет категорию к элементу фото-галереи.
     * Adds category to photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param categoryId ID категории / category ID
     * @return обновленный элемент / updated item
     */
    public PhotoGallery addCategoryToPhotoGalleryItem(Long itemId, Long categoryId) {
        PhotoGallery item = getPhotoGalleryItemById(itemId);
        PublicationCategory category = publicationCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена. ID: " + categoryId));

        item.addCategory(category);
        return photoGalleryItemRepository.save(item);
    }

    /**
     * Удаляет категорию из элемента фото-галереи.
     * Removes category from photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param categoryId ID категории / category ID
     * @return обновленный элемент / updated item
     */
    public PhotoGallery removeCategoryFromPhotoGalleryItem(Long itemId, Long categoryId) {
        PhotoGallery item = getPhotoGalleryItemById(itemId);
        item.removeCategoryById(categoryId);
        return photoGalleryItemRepository.save(item);
    }

    // ========== ПОИСК И ФИЛЬТРАЦИЯ ==========

    /**
     * Получает опубликованные элементы фото-галереи.
     * Gets published photo gallery items.
     *
     * @return список опубликованных элементов / list of published items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getPublishedPhotoGalleryItems() {
        return photoGalleryItemRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    /**
     * Получает черновики элементов фото-галереи.
     * Gets draft photo gallery items.
     *
     * @return список черновиков / list of draft items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getDraftPhotoGalleryItems() {
        return photoGalleryItemRepository.findByPublishedFalseOrderByCreatedAtDesc();
    }

    /**
     * Получает опубликованные элементы по году.
     * Gets published items by year.
     *
     * @param year год / year
     * @return список элементов за указанный год / list of items for specified year
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getPublishedPhotoGalleryItemsByYear(Integer year) {
        return photoGalleryItemRepository.findByYearAndPublishedTrueOrderByCreatedAtDesc(year);
    }

    /**
     * Получает опубликованные элементы по категории.
     * Gets published items by category.
     *
     * @param categoryName название категории / category name
     * @return список элементов указанной категории / list of items for specified category
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getPublishedPhotoGalleryItemsByCategory(String categoryName) {
        return photoGalleryItemRepository.findByCategoryNameAndPublishedTrue(categoryName);
    }

    /**
     * Получает элементы опубликованные на главной странице.
     * Gets items published on homepage.
     *
     * @return список элементов главной страницы / list of homepage items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getHomepagePhotoGalleryItems() {
        return getPublishedPhotoGalleryItemsByCategory("Главная");
    }

    /**
     * Получает элементы опубликованные в галерее.
     * Gets items published in gallery.
     *
     * @return список элементов галереи / list of gallery items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getGalleryPhotoGalleryItems() {
        return getPublishedPhotoGalleryItemsByCategory("Галерея");
    }

    /**
     * Поиск элементов по названию.
     * Searches items by title.
     *
     * @param query поисковый запрос / search query
     * @return список найденных элементов / list of found items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> searchPhotoGalleryItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPhotoGalleryItems();
        }
        return photoGalleryItemRepository.findByTitleContainingIgnoreCase(query.trim());
    }

    /**
     * Получает последние добавленные элементы.
     * Gets recently added items.
     *
     * @param limit ограничение количества / items limit
     * @return список последних элементов / list of recent items
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getRecentPhotoGalleryItems(int limit) {
        return photoGalleryItemRepository.findRecentItems(limit);
    }

    // ========== УПРАВЛЕНИЕ ПУБЛИКАЦИЕЙ ==========

    /**
     * Публикует элемент фото-галереи.
     * Publishes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean publishPhotoGalleryItem(Long id) {
        PhotoGallery item = getPhotoGalleryItemById(id);
        item.setPublished(true);
        photoGalleryItemRepository.save(item);

        logger.info("Элемент опубликован. ID: {}", id);
        return true;
    }

    /**
     * Снимает с публикации элемент фото-галереи.
     * Unpublishes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean unpublishPhotoGalleryItem(Long id) {
        PhotoGallery item = getPhotoGalleryItemById(id);
        item.setPublished(false);
        photoGalleryItemRepository.save(item);

        logger.info("Элемент снят с публикации. ID: {}", id);
        return true;
    }

    // ========== СТАТИСТИКА ==========

    /**
     * Получает статистику фото-галереи.
     * Gets photo gallery statistics.
     *
     * @return статистика галереи / gallery statistics
     */
    @Transactional(readOnly = true)
    public PhotoGalleryStatistics getPhotoGalleryStatistics() {
        long totalItems = photoGalleryItemRepository.count();
        long publishedCount = photoGalleryItemRepository.countByPublished(true);
        long draftCount = photoGalleryItemRepository.countByPublished(false);

        long totalImages = photoGalleryItemRepository.countTotalImages();
        long itemsWithoutImages = photoGalleryItemRepository.findItemsWithoutImages().size();
        long itemsWithoutCategories = photoGalleryItemRepository.findItemsWithoutCategories().size();

        return new PhotoGalleryStatistics(
                totalItems, publishedCount, draftCount,
                totalImages, itemsWithoutImages, itemsWithoutCategories
        );
    }

    /**
     * Получает список уникальных годов.
     * Gets list of distinct years.
     *
     * @return список уникальных годов / list of distinct years
     */
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return photoGalleryItemRepository.findDistinctYears();
    }

    /**
     * Получает элементы по диапазону годов.
     * Gets items by year range.
     *
     * @param startYear начальный год / start year
     * @param endYear конечный год / end year
     * @return список элементов в диапазоне / list of items in range
     */
    @Transactional(readOnly = true)
    public List<PhotoGallery> getPhotoGalleryItemsByYearRange(Integer startYear, Integer endYear) {
        return photoGalleryItemRepository.findByYearRangeAndPublishedTrue(startYear, endYear);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Валидирует элемент фото-галереи.
     * Validates photo gallery item.
     *
     * @param item элемент для валидации / item to validate
     * @throws IllegalArgumentException если элемент не валиден / if item is not valid
     */
    private void validatePhotoGalleryItem(PhotoGallery item) {
        if (item == null) {
            throw new IllegalArgumentException("Элемент не может быть null");
        }

        // Проверяем что все изображения являются именно изображениями
        for (MediaFile image : item.getImages()) {
            if (image != null && !image.isImage()) {
                throw new IllegalArgumentException("Все файлы должны быть изображениями");
            }
        }

        // Проверяем лимит изображений
        if (item.getImagesCount() > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышено максимальное количество изображений: %d", MAX_IMAGES_PER_ITEM)
            );
        }
    }

    /**
     * Проверяет является ли файл валидным изображением.
     * Checks if file is valid image.
     *
     * @param file файл для проверки / file to check
     * @return true если файл является изображением / true if file is image
     */
    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public MediaFileRepository getMediaFileRepository() {
        return mediaFileRepository;
    }

    // ========== КЛАСС СТАТИСТИКИ ==========

        /**
         * Класс статистики фото-галереи.
         * Photo gallery statistics class.
         */
        public record PhotoGalleryStatistics(long totalItems, long publishedCount, long draftCount, long totalImages,
                                             long itemsWithoutImages, long itemsWithoutCategories) {

        @Override
            public String toString() {
                return String.format(
                        "PhotoGalleryStatistics{totalItems=%d, published=%d, draft=%d, totalImages=%d, withoutImages=%d, withoutCategories=%d}",
                        totalItems, publishedCount, draftCount, totalImages, itemsWithoutImages, itemsWithoutCategories
                );
            }
        }

    // ========== МЕТОД ДЛЯ ПОЛУЧЕНИЯ ФОТО ДЛЯ ПРОЕКТОВ ==========

    /**
     * Получает PhotoGalleryDTO по ID медиафайла.
     * Используется для отображения ключевых фото в проектах.
     */
    @Transactional(readOnly = true)
    public PhotoGalleryDTO getPhotoDTOById(Long mediaFileId) {
        try {
            // 1. Ищем медиафайл в базе
            MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                    .orElseThrow(() -> new EntityNotFoundException("Медиафайл не найден. ID: " + mediaFileId));

            // 2. Создаем DTO используя существующий конструктор
            PhotoGalleryDTO dto = new PhotoGalleryDTO(
                    mediaFile.getId(),           // photoId
                    mediaFile.getFileName(),     // fileName
                    mediaFile.getWebPath(),      // webPath
                    mediaFile.getFilenameFromPath(), // thumbnailPath
                    mediaFile.getFileName(),     // title
                    null,                        // galleryId - не знаем
                    null,                        // galleryTitle - не знаем
                    null,                        // galleryYear - не знаем
                    false                        // isPrimary
            );

            logger.info("Загружен медиафайл ID {}: {}", mediaFileId, dto.getWebPath());
            return dto;

        } catch (Exception e) {
            logger.error("Ошибка при получении медиафайла по ID {}: {}", mediaFileId, e.getMessage());

            // 3. Возвращаем заглушку при ошибке
            return createFallbackPhotoDTO(mediaFileId);
        }
    }

    /**
     * Создает заглушку для фото если оно не найдено.
     */
    private PhotoGalleryDTO createFallbackPhotoDTO(Long photoId) {
        return new PhotoGalleryDTO(
                photoId,
                "photo-" + photoId + ".jpg",
                "/images/placeholder.jpg",
                "/images/placeholder.jpg",
                "Фото " + photoId,
                null, null, null, false
        );
    }

    /**
     * Преобразует путь контроллера в публичный путь.
     */
    private String convertToPublicPath(String webPath) {
        if (webPath == null || webPath.isEmpty()) {
            return "/images/placeholder.jpg";
        }

        if (webPath.startsWith("/uploads/")) {
            return webPath; // Уже публичный
        }

        if (webPath.startsWith("/admin/photo-gallery/image/")) {
            String filename = webPath.substring("/admin/photo-gallery/image/".length());
            return "/uploads/" + filename;
        }

        return webPath;
    }
}