package com.community.cms.web.mvc.controller.admin.content;

import com.community.cms.domain.model.content.About;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.content.About.ArticleStatus;
import com.community.cms.domain.service.content.AboutService;
import com.community.cms.domain.service.content.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Контроллер для админ-панели управления статьями проектов.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/admin/projects/{projectId}/articles")
public class AboutAdminController {

    private final AboutService articleService;
    private final ProjectService projectService;

    @Autowired
    public AboutAdminController(AboutService articleService,
                                ProjectService projectService) {
        this.articleService = articleService;
        this.projectService = projectService;
    }

    // ================== СПИСОК СТАТЕЙ ПРОЕКТА ==================

    /**
     * Отображает список статей проекта.
     */
    @GetMapping
    public String listArticles(@PathVariable Long projectId,
                               Model model,
                               @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String search) {

        Project project = getProjectOrThrow(projectId);

        Page<About> articlesPage = getArticlesPage(project, pageable, status, search);

        model.addAttribute("project", project);
        model.addAttribute("articlesPage", articlesPage);
        model.addAttribute("statuses", ArticleStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("search", search);

        return "admin/projects/articles/list";
    }

    // ================== СОЗДАНИЕ СТАТЬИ ==================

    /**
     * Отображает форму создания новой статьи.
     */
    @GetMapping("/create")
    public String showCreateForm(@PathVariable Long projectId, Model model) {
        Project project = getProjectOrThrow(projectId);

        About article = new About();
        article.setProject(project);
        article.setStatus(ArticleStatus.DRAFT);

        model.addAttribute("project", project);
        model.addAttribute("article", article);
        model.addAttribute("statuses", ArticleStatus.values());

        return "admin/projects/articles/create";
    }

    /**
     * Обрабатывает создание новой статьи.
     */
    @PostMapping("/create")
    public String createArticle(@PathVariable Long projectId,
                                @Valid @ModelAttribute("article") About article,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Project project = getProjectOrThrow(projectId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("statuses", ArticleStatus.values());
            return "admin/projects/articles/create";
        }

        // Проверка уникальности slug
        if (article.getSlug() != null && articleService.existsBySlug(article.getSlug())) {
            bindingResult.rejectValue("slug", "error.article", "Статья с таким URL уже существует");
            model.addAttribute("project", project);
            model.addAttribute("statuses", ArticleStatus.values());
            return "admin/projects/articles/create";
        }

        try {
            article.setProject(project);
            articleService.save(article);
            redirectAttributes.addFlashAttribute("successMessage", "Статья успешно создана");
            return "redirect:/admin/projects/" + projectId + "/articles";
        } catch (Exception e) {
            bindingResult.reject("error.article", "Ошибка при создании статьи: " + e.getMessage());
            model.addAttribute("project", project);
            model.addAttribute("statuses", ArticleStatus.values());
            return "admin/projects/articles/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ СТАТЬИ ==================

    /**
     * Отображает форму редактирования статьи.
     */
    @GetMapping("/edit/{articleId}")
    public String showEditForm(@PathVariable Long projectId,
                               @PathVariable Long articleId,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Project project = getProjectOrThrow(projectId);

        return articleService.findById(articleId)
                .filter(article -> article.getProject().getId().equals(projectId))
                .map(article -> {
                    model.addAttribute("project", project);
                    model.addAttribute("article", article);
                    model.addAttribute("statuses", ArticleStatus.values());
                    return "admin/projects/articles/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Статья не найдена");
                    return "redirect:/admin/projects/" + projectId + "/articles";
                });
    }

    /**
     * Обрабатывает обновление статьи.
     */
    @PostMapping("/edit/{articleId}")
    public String updateArticle(@PathVariable Long projectId,
                                @PathVariable Long articleId,
                                @Valid @ModelAttribute("article") About article,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Project project = getProjectOrThrow(projectId);

        // Проверка что статья существует и принадлежит проекту
        About existingArticle = articleService.findById(articleId)
                .filter(a -> a.getProject().getId().equals(projectId))
                .orElseThrow(() -> new IllegalArgumentException("Статья не найдена"));

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("statuses", ArticleStatus.values());
            return "admin/projects/articles/edit";
        }

        // Проверка уникальности slug (исключая текущую статью)
        if (article.getSlug() != null) {
            articleService.findPublishedBySlug(article.getSlug())
                    .filter(a -> !a.getId().equals(articleId))
                    .ifPresent(a -> {
                        bindingResult.rejectValue("slug", "error.article", "Статья с таким URL уже существует");
                    });
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("statuses", ArticleStatus.values());
            return "admin/projects/articles/edit";
        }

        try {
            article.setId(articleId);
            article.setProject(project);
            articleService.update(article);
            redirectAttributes.addFlashAttribute("successMessage", "Статья успешно обновлена");
            return "redirect:/admin/projects/" + projectId + "/articles";
        } catch (Exception e) {
            bindingResult.reject("error.article", "Ошибка при обновлении статьи: " + e.getMessage());
            model.addAttribute("project", project);
            model.addAttribute("statuses", ArticleStatus.values());
            return "admin/projects/articles/edit";
        }
    }

    // ================== ПРОСМОТР СТАТЬИ ==================

    /**
     * Отображает детальную информацию о статье.
     */
    @GetMapping("/view/{articleId}")
    public String viewArticle(@PathVariable Long projectId,
                              @PathVariable Long articleId,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Project project = getProjectOrThrow(projectId);

        return articleService.findById(articleId)
                .filter(article -> article.getProject().getId().equals(projectId))
                .map(article -> {
                    model.addAttribute("project", project);
                    model.addAttribute("article", article);
                    return "admin/projects/articles/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Статья не найдена");
                    return "redirect:/admin/projects/" + projectId + "/articles";
                });
    }

    // ================== УДАЛЕНИЕ СТАТЬИ ==================

    /**
     * Удаляет статью.
     */
    @PostMapping("/delete/{articleId}")
    public String deleteArticle(@PathVariable Long projectId,
                                @PathVariable Long articleId,
                                RedirectAttributes redirectAttributes) {

        try {
            // Проверяем что статья принадлежит проекту
            boolean articleBelongsToProject = articleService.findById(articleId)
                    .filter(article -> article.getProject().getId().equals(projectId))
                    .isPresent();

            if (!articleBelongsToProject) {
                redirectAttributes.addFlashAttribute("errorMessage", "Статья не найдена или не принадлежит проекту");
                return "redirect:/admin/projects/" + projectId + "/articles";
            }

            articleService.deleteById(articleId);
            redirectAttributes.addFlashAttribute("successMessage", "Статья успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении статьи: " + e.getMessage());
        }

        return "redirect:/admin/projects/" + projectId + "/articles";
    }

    // ================== УПРАВЛЕНИЕ СТАТУСОМ СТАТЬИ ==================

    /**
     * Публикует статью.
     */
    @PostMapping("/publish/{articleId}")
    public String publishArticle(@PathVariable Long projectId,
                                 @PathVariable Long articleId,
                                 RedirectAttributes redirectAttributes) {

        return processArticleStatusChange(projectId, articleId, redirectAttributes, "publish");
    }

    /**
     * Переводит статью в черновик.
     */
    @PostMapping("/unpublish/{articleId}")
    public String unpublishArticle(@PathVariable Long projectId,
                                   @PathVariable Long articleId,
                                   RedirectAttributes redirectAttributes) {

        return processArticleStatusChange(projectId, articleId, redirectAttributes, "unpublish");
    }

    /**
     * Архивирует статью.
     */
    @PostMapping("/archive/{articleId}")
    public String archiveArticle(@PathVariable Long projectId,
                                 @PathVariable Long articleId,
                                 RedirectAttributes redirectAttributes) {

        return processArticleStatusChange(projectId, articleId, redirectAttributes, "archive");
    }

    // ================== МАССОВЫЕ ОПЕРАЦИИ ==================

    /**
     * Обрабатывает массовые операции со статьями.
     */
    @PostMapping("/batch")
    public String batchOperation(@PathVariable Long projectId,
                                 @RequestParam String action,
                                 @RequestParam("ids") List<Long> ids,
                                 RedirectAttributes redirectAttributes) {

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не выбрано ни одной статьи");
            return "redirect:/admin/projects/" + projectId + "/articles";
        }

        int successCount = 0;
        int errorCount = 0;

        for (Long articleId : ids) {
            try {
                // Проверяем что статья принадлежит проекту
                boolean articleBelongsToProject = articleService.findById(articleId)
                        .filter(article -> article.getProject().getId().equals(projectId))
                        .isPresent();

                if (!articleBelongsToProject) {
                    errorCount++;
                    continue;
                }

                switch (action) {
                    case "publish":
                        articleService.publishById(articleId);
                        successCount++;
                        break;
                    case "delete":
                        articleService.deleteById(articleId);
                        successCount++;
                        break;
                    default:
                        errorCount++;
                }
            } catch (Exception e) {
                errorCount++;
            }
        }

        if (successCount > 0) {
            String message = switch (action) {
                case "publish" -> "Опубликовано статей: " + successCount;
                case "delete" -> "Удалено статей: " + successCount;
                default -> "Выполнено операций: " + successCount;
            };
            redirectAttributes.addFlashAttribute("successMessage", message);
        }

        if (errorCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибок: " + errorCount);
        }

        return "redirect:/admin/projects/" + projectId + "/articles";
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    private Project getProjectOrThrow(Long projectId) {
        return projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Проект с ID " + projectId + " не найден"));
    }

    private Page<About> getArticlesPage(Project project, Pageable pageable, String status, String search) {
        if (search != null && !search.trim().isEmpty()) {
            List<About> articles = articleService.search(search).stream()
                    .filter(article -> article.getProject().getId().equals(project.getId()))
                    .toList();

            // Преобразуем List в CustomPage
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), articles.size());
            return new org.springframework.data.domain.PageImpl<>(
                    articles.subList(start, end),
                    pageable,
                    articles.size()
            );
        } else if (status != null && !status.trim().isEmpty()) {
            try {
                ArticleStatus articleStatus = ArticleStatus.valueOf(status.toUpperCase());
                return articleService.findByStatus(articleStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Если статус невалиден, возвращаем все статьи проекта
                return articleService.findPublishedByProject(project, pageable);
            }
        } else {
            return articleService.findPublishedByProject(project, pageable);
        }
    }

    private String processArticleStatusChange(Long projectId, Long articleId,
                                              RedirectAttributes redirectAttributes, String action) {
        return articleService.findById(articleId)
                .filter(article -> article.getProject().getId().equals(projectId))
                .map(article -> {
                    try {
                        switch (action) {
                            case "publish":
                                articleService.publish(article);
                                redirectAttributes.addFlashAttribute("successMessage", "Статья опубликована");
                                break;
                            case "unpublish":
                                articleService.unpublish(article);
                                redirectAttributes.addFlashAttribute("successMessage", "Статья переведена в черновик");
                                break;
                            case "archive":
                                articleService.archive(article);
                                redirectAttributes.addFlashAttribute("successMessage", "Статья архивирована");
                                break;
                            default:
                                redirectAttributes.addFlashAttribute("errorMessage", "Неизвестное действие");
                        }
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
                    }
                    return "redirect:/admin/projects/" + projectId + "/articles";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Статья не найдена");
                    return "redirect:/admin/projects/" + projectId + "/articles";
                });
    }
}
