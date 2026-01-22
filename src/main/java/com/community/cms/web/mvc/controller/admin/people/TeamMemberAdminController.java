package com.community.cms.web.mvc.controller.admin.people;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.people.TeamMemberService;
import com.community.cms.infrastructure.storage.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер административной панели для управления членами команды.
 */
@Controller
@RequestMapping("/admin/team-members")
public class TeamMemberAdminController {

    private final TeamMemberService teamMemberService;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService;

    @Autowired
    public TeamMemberAdminController(TeamMemberService teamMemberService,
                                     ProjectService projectService,
                                     FileStorageService fileStorageService) {
        this.teamMemberService = teamMemberService;
        this.projectService = projectService;
        this.fileStorageService = fileStorageService;
    }

    // ================== СПИСОК ЧЛЕНОВ КОМАНДЫ С ФИЛЬТРАЦИЕЙ ==================

    @GetMapping
    public String listTeamMembers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "hasAvatar", required = false) String hasAvatar,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMember> teamMembersPage;

        if (search != null && !search.trim().isEmpty()) {
            teamMembersPage = teamMemberService.searchByNameOrPosition(search, pageable);
        } else if ("ACTIVE".equals(status)) {
            teamMembersPage = teamMemberService.findAllActive(pageable);
        } else if ("INACTIVE".equals(status)) {
            teamMembersPage = teamMemberService.findAllInactive(pageable);
        } else if ("true".equals(hasAvatar)) {
            teamMembersPage = teamMemberService.findWithAvatar(pageable);
        } else if ("false".equals(hasAvatar)) {
            teamMembersPage = teamMemberService.findWithoutAvatar(pageable);
        } else {
            teamMembersPage = teamMemberService.findAll(pageable);
        }

        model.addAttribute("teamMembersPage", teamMembersPage);
        model.addAttribute("selectedSearch", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedHasAvatar", hasAvatar);
        model.addAttribute("title", "Управление командой");

        return "admin/team-members/list";
    }

    @GetMapping("/inactive")
    public String listInactiveTeamMembers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMember> teamMembersPage = teamMemberService.findAllInactive(pageable);

        model.addAttribute("teamMembersPage", teamMembersPage);
        model.addAttribute("selectedStatus", "INACTIVE");
        model.addAttribute("title", "Неактивные члены команды");
        model.addAttribute("showInactive", true);

        return "admin/team-members/list";
    }

    // ================== СОЗДАНИЕ ЧЛЕНА КОМАНДЫ ==================

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("teamMember", new TeamMember());
        model.addAttribute("title", "Создание члена команды");
        model.addAttribute("allProjects", projectService.findAllActive());
        return "admin/team-members/create";
    }

    @PostMapping("/create")
    public String createTeamMember(@Valid @ModelAttribute("teamMember") TeamMember teamMember,
                                   @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/team-members/create";
        }

        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    String fileName = fileStorageService.storeFile(avatarFile);
                    teamMember.setAvatarPath(fileName);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Ошибка при загрузке аватарки: " + e.getMessage());
                    return "redirect:/admin/team-members/create";
                }
            }

            TeamMember savedMember = teamMemberService.save(teamMember);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + savedMember.getFullName() + "' успешно создан!");
            return "redirect:/admin/team-members";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании члена команды: " + e.getMessage());
            return "redirect:/admin/team-members/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ ЧЛЕНА КОМАНДЫ ==================

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Член команды с ID " + id + " не найден");
            return "redirect:/admin/team-members";
        }

        TeamMember teamMember = teamMemberOpt.get();
        model.addAttribute("teamMember", teamMember);
        model.addAttribute("title", "Редактирование: " + teamMember.getFullName());
        model.addAttribute("allProjects", projectService.findAllActive());
        model.addAttribute("memberProjects", teamMember.getProjects());

        return "admin/team-members/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTeamMember(@PathVariable Long id,
                                   @Valid @ModelAttribute("teamMember") TeamMember teamMember,
                                   @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                   @RequestParam(value = "clearAvatar", defaultValue = "false") boolean clearAvatar,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            teamMember.setId(id);
            return "admin/team-members/edit";
        }

        try {
            Optional<TeamMember> existingMemberOpt = teamMemberService.findById(id);
            if (existingMemberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды с ID " + id + " не найден");
                return "redirect:/admin/team-members";
            }

            TeamMember existingMember = existingMemberOpt.get();

            if (clearAvatar) {
                if (existingMember.getAvatarPath() != null && !existingMember.getAvatarPath().isEmpty()) {
                    try {
                        fileStorageService.deleteFile(existingMember.getAvatarPath());
                    } catch (Exception e) {
                        System.err.println("Ошибка при удалении аватарки: " + e.getMessage());
                    }
                }
                teamMember.setAvatarPath(null);
            }
            else if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    if (existingMember.getAvatarPath() != null && !existingMember.getAvatarPath().isEmpty()) {
                        try {
                            fileStorageService.deleteFile(existingMember.getAvatarPath());
                        } catch (Exception e) {
                            System.err.println("Ошибка при удалении старой аватарки: " + e.getMessage());
                        }
                    }

                    String fileName = fileStorageService.storeFile(avatarFile);
                    teamMember.setAvatarPath(fileName);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Ошибка при загрузке аватарки: " + e.getMessage());
                    return "redirect:/admin/team-members/edit/" + id;
                }
            }
            else {
                teamMember.setAvatarPath(existingMember.getAvatarPath());
            }

            teamMember.setProjects(existingMember.getProjects());
            teamMember.setId(id);

            TeamMember updatedMember = teamMemberService.update(teamMember);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + updatedMember.getFullName() + "' успешно обновлен!");
            return "redirect:/admin/team-members";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении члена команды: " + e.getMessage());
            return "redirect:/admin/team-members/edit/" + id;
        }
    }

    // ================== УПРАВЛЕНИЕ АКТИВНОСТЬЮ ==================

    @PostMapping("/activate/{id}")
    public String activateTeamMember(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            TeamMember activatedMember = teamMemberService.activateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + activatedMember.getFullName() + "' активирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при активации члена команды: " + e.getMessage());
        }
        return "redirect:/admin/team-members";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateTeamMember(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            TeamMember deactivatedMember = teamMemberService.deactivateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + deactivatedMember.getFullName() + "' деактивирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при деактивации члена команды: " + e.getMessage());
        }
        return "redirect:/admin/team-members";
    }

    // ================== УДАЛЕНИЕ ЧЛЕНА КОМАНДЫ ==================

    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Член команды с ID " + id + " не найден");
            return "redirect:/admin/team-members";
        }

        TeamMember teamMember = teamMemberOpt.get();
        model.addAttribute("teamMember", teamMember);
        model.addAttribute("title", "Удаление члена команды: " + teamMember.getFullName());

        int projectCount = teamMember.getProjectsCount();
        model.addAttribute("projectCount", projectCount);

        return "admin/team-members/delete";
    }

    @PostMapping("/delete/{id}")
    public String deleteTeamMember(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);
            if (teamMemberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды с ID " + id + " не найден");
                return "redirect:/admin/team-members";
            }

            TeamMember teamMember = teamMemberOpt.get();
            String memberName = teamMember.getFullName();

            teamMember.getProjects().clear();
            teamMemberService.update(teamMember);

            teamMemberService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + memberName + "' успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении члена команды: " + e.getMessage());
        }
        return "redirect:/admin/team-members";
    }

    // ================== УПРАВЛЕНИЕ ПРОЕКТАМИ И РОЛЯМИ ==================

    @GetMapping("/projects/{id}")
    public String manageProjects(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Член команды с ID " + id + " не найден");
            return "redirect:/admin/team-members";
        }

        TeamMember teamMember = teamMemberOpt.get();
        List<Project> allProjects = projectService.findAllActive();
        List<Project> memberProjects = List.copyOf(teamMember.getProjects());
        List<Project> availableProjects = allProjects.stream()
                .filter(project -> !memberProjects.contains(project))
                .toList();

        model.addAttribute("teamMember", teamMember);
        model.addAttribute("title", "Управление проектами: " + teamMember.getFullName());
        model.addAttribute("memberProjects", memberProjects);
        model.addAttribute("availableProjects", availableProjects);

        return "admin/team-members/projects";
    }

    @PostMapping("/projects/{memberId}/add")
    public String addProjectToMember(@PathVariable Long memberId,
                                     @RequestParam Long projectId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(memberId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (teamMemberOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды или проект не найден");
                return "redirect:/admin/team-members/projects/" + memberId;
            }

            TeamMember teamMember = teamMemberOpt.get();
            Project project = projectOpt.get();

            teamMemberService.addProjectToTeamMember(teamMember, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' добавлен к члену команды '" +
                            teamMember.getFullName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении проекта: " + e.getMessage());
        }

        return "redirect:/admin/team-members/projects/" + memberId;
    }

    @PostMapping("/projects/{memberId}/remove")
    public String removeProjectFromMember(@PathVariable Long memberId,
                                          @RequestParam Long projectId,
                                          RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(memberId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (teamMemberOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды или проект не найден");
                return "redirect:/admin/team-members/projects/" + memberId;
            }

            TeamMember teamMember = teamMemberOpt.get();
            Project project = projectOpt.get();

            teamMemberService.removeProjectFromTeamMember(teamMember, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' удален у члена команды '" +
                            teamMember.getFullName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении проекта: " + e.getMessage());
        }

        return "redirect:/admin/team-members/projects/" + memberId;
    }

    @PostMapping("/projects/{memberId}/role")
    public String setRoleForProject(@PathVariable Long memberId,
                                    @RequestParam Long projectId,
                                    @RequestParam String role,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(memberId);

            if (teamMemberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды не найден");
                return "redirect:/admin/team-members";
            }

            TeamMember teamMember = teamMemberOpt.get();
            teamMemberService.setRoleForProject(teamMember, projectId, role);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Роль для проекта установлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при установке роли: " + e.getMessage());
        }

        return "redirect:/admin/team-members/projects/" + memberId;
    }

    // ================== ПОИСК И ФИЛЬТРАЦИЯ ==================

    @GetMapping("/search")
    public String searchTeamMembers(@RequestParam String searchTerm, Model model) {
        List<TeamMember> searchResults = teamMemberService.searchByNameOrPosition(searchTerm);
        model.addAttribute("teamMembers", searchResults);
        model.addAttribute("title", "Результаты поиска: " + searchTerm);
        model.addAttribute("searchTerm", searchTerm);
        return "admin/team-members/list";
    }
}