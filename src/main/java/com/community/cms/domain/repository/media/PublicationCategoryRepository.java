package com.community.cms.domain.repository.media;

import com.community.cms.domain.model.media.PublicationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с категориями публикации в базе данных.
 * Repository for working with publication categories in the database.
 *
 * Предоставляет методы для выполнения CRUD операций и пользовательских запросов.
 * Provides methods for performing CRUD operations and custom queries.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see PublicationCategory
 */
@Repository
public interface PublicationCategoryRepository extends JpaRepository<PublicationCategory, Long> {

    /**
     * Находит категорию по названию (точное совпадение, без учета регистра).
     * Finds category by name (exact match, case insensitive).
     *
     * @param name название категории / category name
     * @return Optional с категорией если найдена / Optional with category if found
     */
    Optional<PublicationCategory> findByNameIgnoreCase(String name);

    /**
     * Проверяет существует ли категория с таким названием (без учета регистра).
     * Checks if category exists by name (case insensitive).
     *
     * @param name название категории / category name
     * @return true если категория существует / true if category exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Находит все категории, отсортированные по названию.
     * Finds all categories, sorted by name.
     *
     * @return список категорий / list of categories
     */
    List<PublicationCategory> findAllByOrderByNameAsc();

    /**
     * Находит категории по части названия (без учета регистра).
     * Finds categories by name part (case insensitive).
     *
     * @param name часть названия для поиска / name part for search
     * @return список найденных категорий / list of found categories
     */
    List<PublicationCategory> findByNameContainingIgnoreCase(String name);

    /**
     * Находит категории с описанием содержащим указанный текст (без учета регистра).
     * Finds categories with description containing specified text (case insensitive).
     *
     * @param text текст для поиска в описании / text to search in description
     * @return список найденных категорий / list of found categories
     */
    @Query("SELECT pc FROM PublicationCategory pc WHERE LOWER(pc.description) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<PublicationCategory> findByDescriptionContainingIgnoreCase(@Param("text") String text);

    /**
     * Подсчитывает количество категорий.
     * Counts categories.
     *
     * @return количество категорий / categories count
     */
    long count();

    /**
     * Находит все категории, отсортированные по дате создания (новые первыми).
     * Finds all categories, sorted by creation date (newest first).
     *
     * @return список категорий / list of categories
     */
    List<PublicationCategory> findAllByOrderByCreatedAtDesc();

    /**
     * Находит все категории, отсортированные по дате обновления (последние обновленные первыми).
     * Finds all categories, sorted by update date (last updated first).
     *
     * @return список категорий / list of categories
     */
    List<PublicationCategory> findAllByOrderByUpdatedAtDesc();
}
