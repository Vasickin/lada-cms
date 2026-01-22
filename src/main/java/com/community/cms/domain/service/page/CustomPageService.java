package com.community.cms.domain.service.page;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.repository.page.CustomPageRepository;
import com.community.cms.web.mvc.dto.PageStatistics;
import com.community.cms.domain.enums.PageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для работы со страницами.
 * Содержит бизнес-логику приложения и служит прослойкой между контроллерами и репозиториями.
 *
 * <p>Расширен методами для работы с типами страниц и основными разделами сайта.
 *
 * @author Vasickin
 * @version 1.1
 * @since 2025
 * @see CustomPage
 * @see PageType
 * @see CustomPageRepository
 */
@Service
public class CustomPageService {

    private final CustomPageRepository pageRepository;

    @Autowired
    public CustomPageService(CustomPageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    // СУЩЕСТВУЮЩИЕ МЕТОДЫ

    public CustomPage savePage(CustomPage page) {
        if (page.getId() == null && pageRepository.existsBySlug(page.getSlug())) {
            throw new IllegalArgumentException("Страница с slug '" + page.getSlug() + "' уже существует");
        }
        return pageRepository.save(page);
    }

    public Optional<CustomPage> findPageById(Long id) {
        return pageRepository.findById(id);
    }

    public Optional<CustomPage> findPageBySlug(String slug) {
        return pageRepository.findBySlug(slug);
    }

    public List<CustomPage> findAllPublishedPages() {
        return pageRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<CustomPage> findAllPages() {
        return pageRepository.findAll();
    }

    public void deletePage(Long id) {
        if (!pageRepository.existsById(id)) {
            throw new IllegalArgumentException("Страница с ID " + id + " не найдена");
        }
        pageRepository.deleteById(id);
    }

    public CustomPage publishPage(Long id) {
        CustomPage page = pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Страница с ID " + id + " не найдена"));
        page.setPublished(true);
        return pageRepository.save(page);
    }

    public CustomPage unpublishPage(Long id) {
        CustomPage page = pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Страница с ID " + id + " не найдена"));
        page.setPublished(false);
        return pageRepository.save(page);
    }

    public List<CustomPage> searchPagesByTitle(String title) {
        return pageRepository.findByTitleContainingIgnoreCase(title);
    }

    public boolean pageExistsBySlug(String slug) {
        return pageRepository.existsBySlug(slug);
    }

    public PageStatistics getPageStatistics() {
        long totalPages = pageRepository.count();
        long publishedCount = pageRepository.countByPublished(true);
        long draftCount = pageRepository.countByPublished(false);
        return new PageStatistics(totalPages, publishedCount, draftCount);
    }

    public List<CustomPage> findRecentPages(int limit) {
        return pageRepository.findRecentPages(limit);
    }

    public List<CustomPage> findRecentPages() {
        return findRecentPages(5);
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ТИПАМИ СТРАНИЦ

    /**
     * Находит страницу по slug ТОЛЬКО если она опубликована.
     * Используется для публичного доступа к контенту.
     *
     * @param slug уникальный идентификатор страницы
     * @param published статус публикации
     * @return Optional содержащий страницу если найдена и соответствует статусу
     */
    public Optional<CustomPage> findPageBySlugAndPublished(String slug, boolean published) {
        return pageRepository.findBySlugAndPublished(slug, published);
    }

    /**
     * Находит ОПУБЛИКОВАННУЮ страницу по типу.
     * Используется для основных страниц сайта (about, projects, gallery и т.д.).
     *
     * @param pageType тип страницы
     * @return Optional содержащий опубликованную страницу если найдена
     */
    public Optional<CustomPage> findPublishedPageByType(PageType pageType) {
        return pageRepository.findFirstByPageTypeAndPublished(pageType, true);
    }

    /**
     * Находит все ОПУБЛИКОВАННЫЕ страницы определенного типа.
     *
     * @param pageType тип страницы
     * @return список опубликованных страниц указанного типа
     */
    public List<CustomPage> findPublishedPagesByType(PageType pageType) {
        return pageRepository.findByPageTypeAndPublished(pageType, true);
    }

    /**
     * Создает или обновляет страницу сайта по типу.
     * Гарантирует что для каждого типа страницы существует только одна запись.
     *
     * @param pageType тип страницы
     * @param title заголовок страницы
     * @param content содержимое страницы
     * @param metaDescription meta-описание для SEO
     * @return сохраненная страница
     */
    public CustomPage saveSitePage(PageType pageType, String title, String content, String metaDescription) {
        Optional<CustomPage> existingPage = pageRepository.findFirstByPageTypeAndPublished(pageType, true);

        CustomPage page;
        if (existingPage.isPresent()) {
            // Обновляем существующую страницу
            page = existingPage.get();
            page.setTitle(title);
            page.setContent(content);
            page.setMetaDescription(metaDescription);
        } else {
            // Создаем новую страницу
            page = new CustomPage(title, content, pageType.getSlug(), pageType);
            page.setMetaDescription(metaDescription);
            page.setPublished(true);
        }

        return pageRepository.save(page);
    }

    /**
     * Проверяет существует ли страница определенного типа.
     *
     * @param pageType тип страницы
     * @return true если страница существует, иначе false
     */
    public boolean pageTypeExists(PageType pageType) {
        return pageRepository.existsByPageType(pageType);
    }

    /**
     * Находит все основные страницы сайта (исключая CUSTOM тип).
     *
     * @return список основных страниц сайта
     */
    public List<CustomPage> findAllSitePages() {
        return pageRepository.findAllSitePages();
    }

    /**
     * Находит все ОПУБЛИКОВАННЫЕ основные страницы сайта.
     *
     * @return список опубликованных основных страниц
     */
    public List<CustomPage> findPublishedSitePages() {
        return pageRepository.findPublishedSitePages();
    }

    /**
     * Подсчитывает количество страниц по типу.
     *
     * @param pageType тип страницы
     * @return количество страниц указанного типа
     */
    public long countPagesByType(PageType pageType) {
        return pageRepository.countByPageType(pageType);
    }

    /**
     * Подсчитывает количество ОПУБЛИКОВАННЫХ страниц по типу.
     *
     * @param pageType тип страницы
     * @return количество опубликованных страниц указанного типа
     */
    public long countPublishedPagesByType(PageType pageType) {
        return pageRepository.countByPageTypeAndPublished(pageType, true);
    }

    /**
     * Создает основные страницы сайта если они не существуют.
     * ИСПРАВЛЕННАЯ ВЕРСИЯ: Проверяет существование по slug, а не по типу страницы.
     * Это предотвращает конфликты дублирования slug в базе данных.
     *
     * @return список созданных страниц
     */
    public List<CustomPage> initializeSitePages() {
        List<CustomPage> createdPages = new java.util.ArrayList<>();

        for (PageType pageType : PageType.getSitePages()) {
            try {
                String expectedSlug = pageType.getSlug();

                // ИСПРАВЛЕНИЕ: Проверяем существование по slug, а не по типу
                // Это предотвращает дублирование slug в базе данных
                if (!pageRepository.existsBySlug(expectedSlug)) {
                    CustomPage page = new CustomPage(
                            pageType.getDisplayName(),
                            getDefaultContentForPageType(pageType),
                            expectedSlug,  // Используем slug из PageType
                            pageType
                    );
                    page.setMetaDescription(getDefaultMetaDescriptionForPageType(pageType));
                    page.setPublished(true);

                    CustomPage savedPage = pageRepository.save(page);
                    createdPages.add(savedPage);

                    System.out.println("✅ Создана страница: " + pageType.getDisplayName() + " (slug: " + expectedSlug + ")");
                } else {
                    // Страница уже существует - проверяем и обновляем тип если нужно
                    Optional<CustomPage> existingPage = pageRepository.findBySlug(expectedSlug);
                    if (existingPage.isPresent()) {
                        CustomPage page = existingPage.get();
                        if (!pageType.equals(page.getPageType())) {
                            // Обновляем тип страницы если не совпадает
                            page.setPageType(pageType);
                            pageRepository.save(page);
                            System.out.println("🔄 Обновлен тип страницы: " + pageType.getDisplayName());
                        }
                    }
                    System.out.println("✅ Страница уже существует: " + pageType.getDisplayName() + " (slug: " + expectedSlug + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Ошибка при создании страницы " + pageType + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return createdPages;
    }

    /**
     * Возвращает контент по умолчанию для типа страницы.
     *
     * @param pageType тип страницы
     * @return контент по умолчанию
     */
    private String getDefaultContentForPageType(PageType pageType) {
        return switch (pageType) {
            case ABOUT -> "<p>Информация о нашей организации будет здесь.</p>";
            case PROJECTS -> "<p>Наши проекты и инициативы будут отображены здесь.</p>";
            case GALLERY -> "<p>Фотографии и видео наших мероприятий будут здесь.</p>";
            case PATRONS -> "<p>Информация для меценатов и партнеров будет здесь.</p>";
            case CONTACT -> "<p>Контактная информация и форма обратной связи будут здесь.</p>";
            default -> "<p>Содержимое страницы.</p>";
        };
    }

    /**
     * Возвращает meta-описание по умолчанию для типа страницы.
     *
     * @param pageType тип страницы
     * @return meta-описание по умолчанию
     */
    private String getDefaultMetaDescriptionForPageType(PageType pageType) {
        return switch (pageType) {
            case ABOUT -> "Информация о нашей организации, миссии и ценностях";
            case PROJECTS -> "Наши текущие и завершенные проекты и инициативы";
            case GALLERY -> "Фотографии и видео с наших мероприятий и проектов";
            case PATRONS -> "Информация для меценатов и партнеров организации";
            case CONTACT -> "Контактная информация и форма обратной связи";
            default -> "Страница организации";
        };
    }
}