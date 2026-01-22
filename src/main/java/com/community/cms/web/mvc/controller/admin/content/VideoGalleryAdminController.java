package com.community.cms.web.mvc.controller.admin.content;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.VideoGallery;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.content.VideoGalleryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер административной панели для управления видео проектов.
 *
 * <p>Предоставляет интерфейс для управления видео, связанными с проектами.
 * Видео хранятся только как ссылки на внешние видеохостинги (YouTube, Vimeo, Rutube).
 * Поддерживает установку основного видео для проекта.</p>
 *
 * @author Community CMS
 * @version 1.1
 * @since 2025
 * @see VideoGallery
 * @see VideoGalleryService
 */
@Controller
@RequestMapping("/admin/project-videos")
public class VideoGalleryAdminController {

    private final VideoGalleryService videoGalleryService;
    private final ProjectService projectService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param videoGalleryService сервис для работы с видео проектов
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public VideoGalleryAdminController(VideoGalleryService videoGalleryService,
                                       ProjectService projectService) {
        this.videoGalleryService = videoGalleryService;
        this.projectService = projectService;
    }

    // ================== СПИСОК ВИДЕО ПРОЕКТА ==================

    /**
     * Отображает список видео указанного проекта.
     *
     * @param projectId идентификатор проекта
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон списка видео или редирект с ошибкой
     */
    @GetMapping("/project/{projectId}")
    public String listProjectVideos(@PathVariable Long projectId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Optional<Project> projectOpt = projectService.findById(projectId);

        if (projectOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Проект с ID " + projectId + " не найден");
            return "redirect:/admin/projects";
        }

        Project project = projectOpt.get();
        List<VideoGallery> videos = videoGalleryService.findByProjectOrderBySortOrder(project);
        Optional<VideoGallery> mainVideoOpt = videoGalleryService.findMainVideoByProject(project);

        model.addAttribute("project", project);
        model.addAttribute("videos", videos);
        model.addAttribute("mainVideo", mainVideoOpt.orElse(null));
        model.addAttribute("title", "Видео проекта: " + project.getTitle());

        return "admin/project-videos/list";
    }

    /**
     * Отображает список всех видео (без привязки к проекту).
     *
     * @param model модель для передачи данных в представление
     * @return шаблон списка всех видео
     */
    @GetMapping
    public String listAllVideos(Model model) {
        List<VideoGallery> videos = videoGalleryService.findRecentVideos(50);
        model.addAttribute("videos", videos);
        model.addAttribute("title", "Все видео проектов");
        model.addAttribute("showAll", true);
        return "admin/project-videos/list";
    }

    // ================== СОЗДАНИЕ ВИДЕО ==================

    /**
     * Отображает форму создания нового видео для проекта.
     *
     * @param projectId идентификатор проекта (опционально)
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон формы создания видео
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long projectId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        VideoGallery video = new VideoGallery();

        // Если указан projectId, привязываем видео к проекту
        if (projectId != null) {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Проект с ID " + projectId + " не найден");
                return "redirect:/admin/projects";
            }
            video.setProject(projectOpt.get());
        }

        List<Project> projects = projectService.findAllActive();
        model.addAttribute("video", video);
        model.addAttribute("projects", projects);
        model.addAttribute("title", "Создание видео");

        return "admin/project-videos/create";
    }

    /**
     * Обрабатывает создание нового видео.
     *
     * @param video создаваемое видео
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список видео проекта или форму с ошибками
     */
    @PostMapping("/create")
    public String createVideo(@Valid @ModelAttribute("video") VideoGallery video,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        // Валидация URL видео
        if (video.getVideoUrl() != null &&
                !videoGalleryService.isValidVideoUrl(video.getVideoUrl())) {
            bindingResult.rejectValue("videoUrl", "error.video",
                    "Некорректный URL видео. Поддерживаются только YouTube, Vimeo и Rutube.");
        }

        if (bindingResult.hasErrors()) {
            List<Project> projects = projectService.findAllActive();
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.video", bindingResult);
            redirectAttributes.addFlashAttribute("video", video);
            redirectAttributes.addFlashAttribute("projects", projects);
            return "redirect:/admin/project-videos/create" +
                    (video.getProject() != null ? "?projectId=" + video.getProject().getId() : "");
        }

        try {
            VideoGallery savedVideo = videoGalleryService.save(video);

            // Если видео помечено как основное, устанавливаем его
            if (video.isMain()) {
                videoGalleryService.setAsMainVideo(savedVideo);
            }

            String successMessage = "Видео '" + savedVideo.getTitle() + "' успешно создано!";
            if (savedVideo.isMain()) {
                successMessage += " Установлено как основное видео проекта.";
            }

            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/project-videos/project/" + savedVideo.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании видео: " + e.getMessage());
            return "redirect:/admin/project-videos/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ ВИДЕО ==================

    /**
     * Отображает форму редактирования видео.
     *
     * @param id идентификатор видео
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон формы редактирования или редирект с ошибкой
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<VideoGallery> videoOpt = videoGalleryService.findById(id);

        if (videoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Видео с ID " + id + " не найден");
            return "redirect:/admin/project-videos";
        }

        VideoGallery video = videoOpt.get();
        List<Project> projects = projectService.findAllActive();

        model.addAttribute("video", video);
        model.addAttribute("projects", projects);
        model.addAttribute("title", "Редактирование видео: " + video.getTitle());

        return "admin/project-videos/edit";
    }

    /**
     * Обрабатывает обновление видео.
     *
     * @param id идентификатор видео
     * @param video обновленные данные видео
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список видео проекта или форму с ошибками
     */
    @PostMapping("/edit/{id}")
    public String updateVideo(@PathVariable Long id,
                              @Valid @ModelAttribute("video") VideoGallery video,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        // Валидация URL видео
        if (video.getVideoUrl() != null &&
                !videoGalleryService.isValidVideoUrl(video.getVideoUrl())) {
            bindingResult.rejectValue("videoUrl", "error.video",
                    "Некорректный URL видео. Поддерживаются только YouTube, Vimeo и Rutube.");
        }

        if (bindingResult.hasErrors()) {
            video.setId(id); // Сохраняем ID для формы
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.video", bindingResult);
            redirectAttributes.addFlashAttribute("video", video);
            return "redirect:/admin/project-videos/edit/" + id;
        }

        try {
            // Проверяем существование видео
            Optional<VideoGallery> existingVideoOpt = videoGalleryService.findById(id);
            if (existingVideoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Видео с ID " + id + " не найден");
                return "redirect:/admin/project-videos";
            }

            VideoGallery existingVideo = existingVideoOpt.get();
            video.setId(id);

            // Сохраняем проект, если он не установлен в форме
            if (video.getProject() == null) {
                video.setProject(existingVideo.getProject());
            }

            VideoGallery updatedVideo = videoGalleryService.update(video);

            // Управление основным видео
            if (video.isMain() && !existingVideo.isMain()) {
                videoGalleryService.setAsMainVideo(updatedVideo);
            } else if (!video.isMain() && existingVideo.isMain()) {
                videoGalleryService.removeMainVideo(updatedVideo);
            }

            String successMessage = "Видео '" + updatedVideo.getTitle() + "' успешно обновлено!";
            if (updatedVideo.isMain()) {
                successMessage += " Является основным видео проекта.";
            }

            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/project-videos/project/" + updatedVideo.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении видео: " + e.getMessage());
            return "redirect:/admin/project-videos/edit/" + id;
        }
    }

    // ================== УСТАНОВКА ОСНОВНОГО ВИДЕО ==================

    /**
     * Устанавливает видео как основное для проекта.
     *
     * @param id идентификатор видео
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список видео проекта
     */
    @PostMapping("/set-main/{id}")
    public String setAsMainVideo(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<VideoGallery> videoOpt = videoGalleryService.findById(id);
            if (videoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Видео с ID " + id + " не найден");
                return "redirect:/admin/project-videos";
            }

            VideoGallery video = videoOpt.get();
            VideoGallery updatedVideo = videoGalleryService.setAsMainVideo(video);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Видео '" + updatedVideo.getTitle() + "' установлено как основное для проекта!");
            return "redirect:/admin/project-videos/project/" + updatedVideo.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при установке основного видео: " + e.getMessage());
            return "redirect:/admin/project-videos";
        }
    }

    // ================== УДАЛЕНИЕ ВИДЕО ==================

    /**
     * Отображает страницу подтверждения удаления видео.
     *
     * @param id идентификатор видео
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон подтверждения удаления или редирект с ошибкой
     */
    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Optional<VideoGallery> videoOpt = videoGalleryService.findById(id);

        if (videoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Видео с ID " + id + " не найден");
            return "redirect:/admin/project-videos";
        }

        VideoGallery video = videoOpt.get();
        model.addAttribute("video", video);
        model.addAttribute("title", "Удаление видео: " + video.getTitle());

        return "admin/project-videos/delete";
    }

    /**
     * Обрабатывает удаление видео.
     *
     * @param id идентификатор видео
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список видео проекта
     */
    @PostMapping("/delete/{id}")
    public String deleteVideo(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<VideoGallery> videoOpt = videoGalleryService.findById(id);
            if (videoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Видео с ID " + id + " не найден");
                return "redirect:/admin/project-videos";
            }

            VideoGallery video = videoOpt.get();
            Long projectId = video.getProject().getId();
            String videoTitle = video.getTitle();

            // Проверяем, является ли видео основным
            boolean wasMain = video.isMain();

            videoGalleryService.deleteById(id);

            String successMessage = "Видео '" + videoTitle + "' успешно удалено!";
            if (wasMain) {
                successMessage += " Это было основное видео проекта.";
            }

            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/project-videos/project/" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении видео: " + e.getMessage());
            return "redirect:/admin/project-videos";
        }
    }

    // ================== УПРАВЛЕНИЕ ПОРЯДКОМ СОРТИРОВКИ ==================

    /**
     * Обновляет порядок сортировки видео.
     *
     * @param projectId идентификатор проекта
     * @param videoIds массив ID видео в новом порядке
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список видео проекта
     */
    @PostMapping("/update-order/{projectId}")
    public String updateVideoOrder(@PathVariable Long projectId,
                                   @RequestParam Long[] videoIds,
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Проект с ID " + projectId + " не найден");
                return "redirect:/admin/projects";
            }

            // Обновляем sortOrder для каждого видео
            for (int i = 0; i < videoIds.length; i++) {
                Optional<VideoGallery> videoOpt = videoGalleryService.findById(videoIds[i]);
                if (videoOpt.isPresent()) {
                    VideoGallery video = videoOpt.get();
                    video.setSortOrder(i);
                    videoGalleryService.update(video);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Порядок видео успешно обновлен!");
            return "redirect:/admin/project-videos/project/" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении порядка видео: " + e.getMessage());
            return "redirect:/admin/project-videos/project/" + projectId;
        }
    }

    // ================== ПРОВЕРКА URL ВИДЕО ==================

    /**
     * Проверяет валидность URL видео (AJAX endpoint).
     *
     * @param url URL для проверки
     * @return JSON ответ с результатом проверки
     */
    @GetMapping("/validate-url")
    @ResponseBody
    public String validateVideoUrl(@RequestParam String url) {
        boolean isValid = videoGalleryService.isValidVideoUrl(url);
        return "{\"valid\": " + isValid + "}";
    }
}