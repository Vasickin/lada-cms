
package com.community.cms.domain.service.media;

import com.community.cms.domain.model.media.PublicationCategory;
import com.community.cms.domain.repository.media.PublicationCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с категориями публикации.
 * Service for working with publication categories.
 *
 * Обеспечивает бизнес-логику для управления категориями публикации.
 * Provides business logic for managing publication categories.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see PublicationCategory
 * @see PublicationCategoryRepository
 */
@Service
public class PublicationCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(PublicationCategoryService.class);

    private final PublicationCategoryRepository publicationCategoryRepository;

    /**
     * Конструктор с внедрением зависимостей.
     * Constructor with dependency injection.
     *
     * @param publicationCategoryRepository репозиторий категорий / category repository
     */
    @Autowired
    public PublicationCategoryService(PublicationCategoryRepository publicationCategoryRepository) {
        this.publicationCategoryRepository = publicationCategoryRepository;
    }

    /**
     * Инициализирует стандартные категории публикации.
     * Initializes default publication categories.
     *
     * Вызывается при запуске приложения для создания необходимых категорий.
     * Called on application startup to create required categories.
     */
    @Transactional
    public void initializeDefaultCategories() {
        logger.info("🔄 Начало инициализации категорий публикации / Starting publication categories initialization...");

        List<PublicationCategory> defaultCategories = List.of(
                new PublicationCategory("Главная", "Публикация на главной странице сайта / Publication on homepage"),
                new PublicationCategory("О нас", "Публикация в разделе 'О нас' / Publication in 'About us' section"),
                new PublicationCategory("Наши проекты", "Публикация в разделе проектов / Publication in projects section"),
                new PublicationCategory("Галерея", "Публикация в общей галерее / Publication in general gallery")
        );

        int createdCount = 0;
        int skippedCount = 0;

        for (PublicationCategory category : defaultCategories) {
            if (!publicationCategoryRepository.existsByNameIgnoreCase(category.getName())) {
                publicationCategoryRepository.save(category);
                createdCount++;
                logger.info("✅ Создана категория: {} / Created category: {}", category.getName(), category.getName());
            } else {
                skippedCount++;
                logger.debug("⏩ Категория уже существует: {} / Category already exists: {}", category.getName(), category.getName());
            }
        }

        logger.info("✅ Инициализация категорий завершена: создано {}, пропущено {} / Categories initialization completed: created {}, skipped {}",
                createdCount, skippedCount, createdCount, skippedCount);
    }

    /**
     * Получает все категории, отсортированные по названию.
     * Gets all categories, sorted by name.
     *
     * @return список категорий / list of categories
     */
    public List<PublicationCategory> getAllCategories() {
        return publicationCategoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Находит категорию по названию (без учета регистра).
     * Finds category by name (case insensitive).
     *
     * @param name название категории / category name
     * @return категория или null если не найдена / category or null if not found
     */
    public PublicationCategory findByName(String name) {
        return publicationCategoryRepository.findByNameIgnoreCase(name)
                .orElse(null);
    }

    /**
     * Получает количество категорий.
     * Gets categories count.
     *
     * @return количество категорий / categories count
     */
    public long getCount() {
        return publicationCategoryRepository.count();
    }

    /**
     * Проверяет существует ли категория с указанным названием.
     * Checks if category exists with specified name.
     *
     * @param name название категории / category name
     * @return true если категория существует / true if category exists
     */
    public boolean existsByName(String name) {
        return publicationCategoryRepository.existsByNameIgnoreCase(name);
    }

    /**
     * Сохраняет или обновляет категорию.
     * Saves or updates category.
     *
     * @param category категория для сохранения / category to save
     * @return сохраненная категория / saved category
     */
    @Transactional
    public PublicationCategory saveCategory(PublicationCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null / Category cannot be null");
        }

        if (category.getId() == null) {
            // Новая категория
            category.setCreatedAt(LocalDateTime.now());
            logger.info("Создана новая категория: {} / Created new category: {}", category.getName(), category.getName());
        } else {
            // Обновление существующей категории
            category.setUpdatedAt(LocalDateTime.now());
            logger.info("Обновлена категория: {} (ID: {}) / Updated category: {} (ID: {})",
                    category.getName(), category.getId(), category.getName(), category.getId());
        }

        return publicationCategoryRepository.save(category);
    }

    /**
     * Удаляет категорию по ID.
     * Deletes category by ID.
     *
     * @param id ID категории / category ID
     * @return true если категория была удалена / true if category was deleted
     */
    @Transactional
    public boolean deleteCategory(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID категории не может быть null / Category ID cannot be null");
        }

        if (publicationCategoryRepository.existsById(id)) {
            publicationCategoryRepository.deleteById(id);
            logger.info("Удалена категория с ID: {} / Deleted category with ID: {}", id, id);
            return true;
        }

        logger.warn("Попытка удалить несуществующую категорию с ID: {} / Attempt to delete non-existing category with ID: {}", id, id);
        return false;
    }

    /**
     * Получает категорию по ID.
     * Gets category by ID.
     *
     * @param id ID категории / category ID
     * @return категория или null если не найдена / category or null if not found
     */
    public PublicationCategory getCategoryById(Long id) {
        return publicationCategoryRepository.findById(id).orElse(null);
    }

    /**
     * Поиск категорий по части названия.
     * Searches categories by name part.
     *
     * @param searchText текст для поиска / search text
     * @return список найденных категорий / list of found categories
     */
    public List<PublicationCategory> searchCategories(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllCategories();
        }
        return publicationCategoryRepository.findByNameContainingIgnoreCase(searchText.trim());
    }
}