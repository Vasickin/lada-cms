package com.community.cms.web.mvc.controller.admin.content;

import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.domain.model.media.PublicationCategory;
import com.community.cms.infrastructure.storage.FileStorageService;
import com.community.cms.domain.service.media.PublicationCategoryService;
import com.community.cms.domain.service.content.PhotoGalleryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
@RequestMapping("/admin/photo-gallery")
public class PhotoGalleryAdminController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGalleryAdminController.class);
    private static final int MAX_UPLOAD_FILES = 15;

    private final PhotoGalleryService photoGalleryService;
    private final PublicationCategoryService publicationCategoryService;
    private final FileStorageService fileStorageService;

    @Autowired
    public PhotoGalleryAdminController(PhotoGalleryService photoGalleryService,
                                       PublicationCategoryService publicationCategoryService,
                                       FileStorageService fileStorageService) {
        this.photoGalleryService = photoGalleryService;
        this.publicationCategoryService = publicationCategoryService;
        this.fileStorageService = fileStorageService;
    }

    // ========== СПИСОК ЭЛЕМЕНТОВ ==========

    @GetMapping("")
    public String listPhotoGalleryItems(@RequestParam(required = false) String search, Model model) {
        List<PhotoGallery> items = photoGalleryService.searchPhotoGalleryItems(search);
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        List<Integer> availableYears = photoGalleryService.getAvailableYears();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("statistics", photoGalleryService.getPhotoGalleryStatistics());
        model.addAttribute("isDraftView", false);
        model.addAttribute("isPublishedView", false);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("selectedYear", null);
        model.addAttribute("availableYears", availableYears);

        return "admin/photo-gallery/list";
    }

    @GetMapping("/published")
    public String showPublishedItems(Model model) {
        List<PhotoGallery> items = photoGalleryService.getPublishedPhotoGalleryItems();
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        List<Integer> availableYears = photoGalleryService.getAvailableYears();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("statistics", photoGalleryService.getPhotoGalleryStatistics());
        model.addAttribute("isPublishedView", true);
        model.addAttribute("isDraftView", false);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("selectedYear", null);
        model.addAttribute("availableYears", availableYears);

        return "admin/photo-gallery/list";
    }

    @GetMapping("/draft")
    public String showDraftItems(Model model) {
        List<PhotoGallery> items = photoGalleryService.getDraftPhotoGalleryItems();
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        List<Integer> availableYears = photoGalleryService.getAvailableYears();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("statistics", photoGalleryService.getPhotoGalleryStatistics());
        model.addAttribute("isPublishedView", false);
        model.addAttribute("isDraftView", true);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("selectedYear", null);
        model.addAttribute("availableYears", availableYears);

        return "admin/photo-gallery/list";
    }

    // ========== СОЗДАНИЕ ЭЛЕМЕНТА ==========

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        logger.info("Открытие формы создания элемента");

        PhotoGallery item = new PhotoGallery();
        // Устанавливаем текущий год по умолчанию
        item.setYear(java.time.Year.now().getValue());

        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        logger.info("Загружено категорий для формы: {}", categories.size());

        model.addAttribute("photoGalleryItem", item);
        model.addAttribute("categories", categories);
        model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
        model.addAttribute("isEdit", false);

        return "admin/photo-gallery/create";
    }

    @PostMapping("/create")
    public String createPhotoGalleryItem(
            @Valid @ModelAttribute("photoGalleryItem") PhotoGallery item,
            BindingResult bindingResult,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Создание элемента: {}, категории: {}, файлов: {}",
                item.getTitle(), categoryIds, files != null ? files.length : 0);

        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации: {}", bindingResult.getAllErrors());
            return prepareCreateOrEditModel(model, item, false, categoryIds);
        }


        if (categoryIds == null || categoryIds.isEmpty()) {
            bindingResult.rejectValue("categories", "error.photoGalleryItem",
                    "Выберите хотя бы одну категорию");
            return prepareCreateOrEditModel(model, item, false, categoryIds);
        }

        // Проверяем файлы
        if (files == null || files.length == 0) {
            bindingResult.rejectValue("images", "error.photoGalleryItem",
                    "Загрузите хотя бы одно изображение");
            return prepareCreateOrEditModel(model, item, false, categoryIds);
        }

        try {
            addSelectedCategories(item, categoryIds);
            PhotoGallery savedItem = photoGalleryService.createPhotoGalleryItemWithImages(item, files);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Элемент '%s' успешно создан", savedItem.getTitle()));

            return "redirect:/admin/photo-gallery";

        } catch (Exception e) {
            logger.error("Ошибка при создании: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
            return "redirect:/admin/photo-gallery/create";
        }
    }

    // ========== РЕДАКТИРОВАНИЕ ЭЛЕМЕНТА ==========

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            PhotoGallery item = photoGalleryService.getPhotoGalleryItemById(id);
            List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

            List<Long> selectedCategoryIds = item.getCategories().stream()
                    .map(PublicationCategory::getId)
                    .toList();

            model.addAttribute("photoGalleryItem", item);
            model.addAttribute("categories", categories);
            model.addAttribute("selectedCategoryIds", selectedCategoryIds);
            model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
            model.addAttribute("isEdit", true);
            model.addAttribute("currentImageCount", item.getImagesCount());

            return "admin/photo-gallery/edit";

        } catch (EntityNotFoundException e) {
            return "redirect:/admin/photo-gallery";
        }
    }

    @PostMapping("/edit/{id}")
    public String updatePhotoGalleryItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("photoGalleryItem") PhotoGallery item,
            BindingResult bindingResult,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "keepImageIds", required = false) List<Long> keepImageIds, // ИЗМЕНЕНИЕ: переименуем параметр
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Редактирование элемента ID: {}, категории: {}, файлов: {}, сохраняемые изображения: {}",
                id, categoryIds, files != null ? files.length : 0, keepImageIds);

        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации: {}", bindingResult.getAllErrors());
            return prepareCreateOrEditModel(model, item, true, categoryIds);
        }

        if (categoryIds == null || categoryIds.isEmpty()) {
            bindingResult.rejectValue("categories", "error.photoGalleryItem",
                    "Выберите хотя бы одну категорию");
            return prepareCreateOrEditModel(model, item, true, categoryIds);
        }

        try {
            addSelectedCategories(item, categoryIds);

            // ПРОВЕРЯЕМ: есть ли новые файлы?
            PhotoGallery updatedItem;

            if (files != null && files.length > 0) {
                // Есть новые изображения - используем метод с сохранением старых
                logger.info("Обновление элемента с новыми изображениями. ID: {}, файлов: {}, сохраняемые: {}",
                        id, files.length, keepImageIds);
                updatedItem = photoGalleryService.updatePhotoGalleryItemWithImages(
                        id, item, files, keepImageIds);
            } else {
                // Нет новых изображений - используем обычный метод обновления
                logger.info("Обновление элемента без новых изображений. ID: {}, сохраняемые: {}",
                        id, keepImageIds);
                updatedItem = photoGalleryService.updatePhotoGalleryItem(id, item, keepImageIds);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Элемент '%s' успешно обновлен", updatedItem.getTitle()));

            return "redirect:/admin/photo-gallery";

        } catch (Exception e) {
            logger.error("Ошибка при обновлении: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
            return "redirect:/admin/photo-gallery/edit/" + id;
        }
    }

    // ========== ПРЕДПРОСМОТР ЭЛЕМЕНТА ==========

    @GetMapping("/preview/{id}")
    public String previewPhotoGalleryItem(@PathVariable Long id, Model model) {
        try {
            PhotoGallery item = photoGalleryService.getPhotoGalleryItemById(id);
            model.addAttribute("item", item);
            return "admin/photo-gallery/preview";
        } catch (EntityNotFoundException e) {
            return "redirect:/admin/photo-gallery";
        }
    }

    // ========== УДАЛЕНИЕ ЭЛЕМЕНТА ==========

    @PostMapping("/delete/{id}")
    public String deletePhotoGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            photoGalleryService.deletePhotoGalleryItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Элемент успешно удален");
        } catch (Exception e) {
            logger.error("Ошибка при удалении элемента {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/photo-gallery";
    }

    // ========== ПУБЛИКАЦИЯ/СНЯТИЕ С ПУБЛИКАЦИИ ==========

    @PostMapping("/publish/{id}")
    public String publishPhotoGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            photoGalleryService.publishPhotoGalleryItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Элемент успешно опубликован");
        } catch (Exception e) {
            logger.error("Ошибка при публикации элемента {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при публикации: " + e.getMessage());
        }
        return "redirect:/admin/photo-gallery";
    }

    @PostMapping("/unpublish/{id}")
    public String unpublishPhotoGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            photoGalleryService.unpublishPhotoGalleryItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Элемент успешно снят с публикации");
        } catch (Exception e) {
            logger.error("Ошибка при снятии с публикации элемента {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при снятии с публикации: " + e.getMessage());
        }
        return "redirect:/admin/photo-gallery";
    }

    // ========== ФИЛЬТРАЦИЯ ==========

    @GetMapping("/year/{year}")
    public String filterByYear(@PathVariable Integer year, Model model) {
        List<PhotoGallery> items = photoGalleryService.getPublishedPhotoGalleryItemsByYear(year);
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        List<Integer> availableYears = photoGalleryService.getAvailableYears();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("availableYears", availableYears);
        model.addAttribute("isPublishedView", true);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedCategory", null);

        return "admin/photo-gallery/list";
    }

    @GetMapping("/category/{categoryId}")
    public String filterByCategory(@PathVariable Long categoryId, Model model) {
        PublicationCategory category = publicationCategoryService.getCategoryById(categoryId);
        if (category == null) {
            return "redirect:/admin/photo-gallery";
        }

        List<PhotoGallery> items = photoGalleryService.getPublishedPhotoGalleryItemsByCategory(category.getName());
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        List<Integer> availableYears = photoGalleryService.getAvailableYears();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("availableYears", availableYears);
        model.addAttribute("isPublishedView", true);
        model.addAttribute("selectedYear", null);
        model.addAttribute("selectedCategory", category);

        return "admin/photo-gallery/list";
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private String prepareCreateOrEditModel(Model model, PhotoGallery item, boolean isEdit, List<Long> categoryIds) {
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("photoGalleryItem", item);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryIds", categoryIds);
        model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
        model.addAttribute("isEdit", isEdit);

        return "admin/photo-gallery/create";
    }

    private void addSelectedCategories(PhotoGallery item, List<Long> categoryIds) {
        if (categoryIds != null) {
            item.getCategories().clear();
            for (Long categoryId : categoryIds) {
                PublicationCategory category = publicationCategoryService.getCategoryById(categoryId);
                if (category != null) {
                    item.addCategory(category);
                }
            }
        }
    }
    // ========== КАСТОМНЫЙ ENDPOINT ДЛЯ ФАЙЛОВ ==========

    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable String filename) {
        try {
            System.out.println("Запрос изображения: " + filename);

            String projectPath = System.getProperty("user.dir");
            String filePath = projectPath + "/uploads/" + filename;
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);

            System.out.println("Ищу файл по пути: " + path.toAbsolutePath());

            if (!java.nio.file.Files.exists(path)) {
                System.out.println("Файл не найден!");
                return ResponseEntity.notFound().build();
            }

            byte[] imageData = java.nio.file.Files.readAllBytes(path);
            org.springframework.core.io.ByteArrayResource resource =
                    new org.springframework.core.io.ByteArrayResource(imageData);

            String mimeType = java.nio.file.Files.probeContentType(path);
            if (mimeType == null) mimeType = "image/jpeg";

            System.out.println("Файл найден, тип: " + mimeType);

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(mimeType))
                    .body(resource);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ========== ТЕСТОВАЯ СТРАНИЦА ==========

    @GetMapping("/test-image-endpoint")
    @org.springframework.web.bind.annotation.ResponseBody
    public String testImageEndpoint() {
        String projectPath = System.getProperty("user.dir");
        String staticPath = projectPath + "/src/main/resources/static/";

        StringBuilder html = new StringBuilder();
        html.append("<h1>Тест изображений</h1>");
        html.append("<p>Путь: ").append(staticPath).append("</p>");

        String[] testFiles = {
                "88b4ef45-4fe4-4e48-be3e-c73eb657c1b1.jpg",
                "6f821c1d-2372-46fb-aa6c-e329723466d4.jpg"
        };

        html.append("<div style='display: flex; gap: 20px;'>");

        for (String filename : testFiles) {
            html.append("<div>");
            html.append("<h3>").append(filename).append("</h3>");
            html.append("<img src='/admin/photo-gallery/image/").append(filename)
                    .append("' style='width: 200px; height: 150px;'/>");
            html.append("<p><a href='/admin/photo-gallery/image/").append(filename)
                    .append("' target='_blank'>Открыть</a></p>");
            html.append("</div>");
        }

        html.append("</div>");
        return html.toString();
    }
}