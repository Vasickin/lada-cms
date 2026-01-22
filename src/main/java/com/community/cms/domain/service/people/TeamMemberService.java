package com.community.cms.domain.service.people;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.repository.people.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления членами команды организации "ЛАДА".
 */
@Service
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    public TeamMember save(TeamMember teamMember) {
        validateTeamMember(teamMember);
        return teamMemberRepository.save(teamMember);
    }

    public TeamMember update(TeamMember teamMember) {
        validateTeamMember(teamMember);
        return teamMemberRepository.save(teamMember);
    }

    @Transactional(readOnly = true)
    public Optional<TeamMember> findById(Long id) {
        return teamMemberRepository.findById(id);
    }

    public void deleteById(Long id) {
        teamMemberRepository.deleteById(id);
    }

    // ================== ПОИСК ПО АКТИВНОСТИ ==================

    @Transactional(readOnly = true)
    public List<TeamMember> findAllActive() {
        return teamMemberRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findAllActiveOrderBySortOrder() {
        return teamMemberRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findAllActiveOrderByName() {
        return teamMemberRepository.findByActiveTrueOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findAllInactive() {
        return teamMemberRepository.findByActiveFalse();
    }

    // ================== ПОИСК ПО ИМЕНИ И ДОЛЖНОСТИ ==================

    @Transactional(readOnly = true)
    public List<TeamMember> findByNameContaining(String fullName) {
        return teamMemberRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findActiveByNameContaining(String fullName) {
        return teamMemberRepository.findByFullNameContainingIgnoreCaseAndActiveTrue(fullName);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findByPositionContaining(String position) {
        return teamMemberRepository.findByPositionContainingIgnoreCase(position);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findActiveByPositionContaining(String position) {
        return teamMemberRepository.findByPositionContainingIgnoreCaseAndActiveTrue(position);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> searchByNameOrPosition(String searchTerm) {
        return teamMemberRepository.searchByNameOrPosition(searchTerm);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> searchActiveByNameOrPosition(String searchTerm) {
        return teamMemberRepository.searchActiveByNameOrPosition(searchTerm);
    }

    // ================== РАБОТА С ПРОЕКТАМИ ==================

    @Transactional(readOnly = true)
    public List<TeamMember> findByProject(Project project) {
        return teamMemberRepository.findByProject(project);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findByProjectOrderBySortOrder(Project project) {
        return teamMemberRepository.findByProjectOrderBySortOrder(project);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findByProjectId(Long projectId) {
        return teamMemberRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findNotInProject(Project project) {
        return teamMemberRepository.findNotInProject(project);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findWithoutProjects() {
        return teamMemberRepository.findWithoutProjects();
    }

    // ================== ПАГИНАЦИЯ ==================

    @Transactional(readOnly = true)
    public Page<TeamMember> findAllActive(Pageable pageable) {
        return teamMemberRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> findAll(Pageable pageable) {
        return teamMemberRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> findAllInactive(Pageable pageable) {
        return teamMemberRepository.findByActiveFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> findWithAvatar(Pageable pageable) {
        return teamMemberRepository.findByAvatarPathIsNotNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> findWithoutAvatar(Pageable pageable) {
        return teamMemberRepository.findByAvatarPathIsNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<TeamMember> searchByNameOrPosition(String searchTerm, Pageable pageable) {
        return teamMemberRepository.searchByNameOrPosition(searchTerm, pageable);
    }

    // ================== СОРТИРОВКА ==================

    @Transactional(readOnly = true)
    public List<TeamMember> findAllOrderByName() {
        return teamMemberRepository.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findAllOrderBySortOrder() {
        return teamMemberRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findAllOrderByCreatedDateDesc() {
        return teamMemberRepository.findAllByOrderByCreatedAtDesc();
    }

    // ================== СТАТИСТИКА И СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    @Transactional(readOnly = true)
    public long countActive() {
        return teamMemberRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long countInactive() {
        return teamMemberRepository.countByActiveFalse();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findWithAvatar() {
        return teamMemberRepository.findByAvatarPathIsNotNull();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findWithoutAvatar() {
        return teamMemberRepository.findByAvatarPathIsNull();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findWithBio() {
        return teamMemberRepository.findWithBio();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findWithoutBio() {
        return teamMemberRepository.findWithoutBio();
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findRecentTeamMembers(int limit) {
        return teamMemberRepository.findRecentTeamMembers(limit);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findKeyTeamMembers(int limit) {
        return teamMemberRepository.findKeyTeamMembers(limit);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findByExactPosition(String position) {
        return teamMemberRepository.findByPosition(position);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findActiveByExactPosition(String position) {
        return teamMemberRepository.findByPositionAndActiveTrue(position);
    }

    // ================== РАБОТА С EMAIL ==================

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return teamMemberRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<TeamMember> findByEmail(String email) {
        return teamMemberRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<TeamMember> findActiveByEmail(String email) {
        return teamMemberRepository.findByEmailAndActiveTrue(email);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    private void validateTeamMember(TeamMember teamMember) {
        if (teamMember == null) {
            throw new IllegalArgumentException("Член команды не может быть null");
        }

        if (teamMember.getFullName() == null || teamMember.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Полное имя члена команды обязательно");
        }

        if (teamMember.getPosition() == null || teamMember.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Должность члена команды обязательна");
        }

        if (teamMember.getSortOrder() == null) {
            teamMember.setSortOrder(0);
        }
    }

    public TeamMember createTeamMember(String fullName, String position,
                                       String bio, String avatarPath,
                                       String email, String phone,
                                       String socialLinks, Integer sortOrder) {
        TeamMember teamMember = new TeamMember(fullName, position);
        teamMember.setBio(bio);
        teamMember.setAvatarPath(avatarPath);
        teamMember.setEmail(email);
        teamMember.setPhone(phone);
        teamMember.setSocialLinks(socialLinks);
        teamMember.setSortOrder(sortOrder != null ? sortOrder : 0);
        teamMember.setActive(true);

        return save(teamMember);
    }

    public TeamMember activate(TeamMember teamMember) {
        teamMember.setActive(true);
        return teamMemberRepository.save(teamMember);
    }

    public TeamMember deactivate(TeamMember teamMember) {
        teamMember.setActive(false);
        return teamMemberRepository.save(teamMember);
    }

    public TeamMember activateById(Long id) {
        TeamMember teamMember = teamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Член команды с ID " + id + " не найден"));
        return activate(teamMember);
    }

    public TeamMember deactivateById(Long id) {
        TeamMember teamMember = teamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Член команды с ID " + id + " не найден"));
        return deactivate(teamMember);
    }

    public TeamMember addProjectToTeamMember(TeamMember teamMember, Project project) {
        teamMember.addProject(project);
        return teamMemberRepository.save(teamMember);
    }

    public TeamMember removeProjectFromTeamMember(TeamMember teamMember, Project project) {
        teamMember.removeProject(project);
        return teamMemberRepository.save(teamMember);
    }

    public TeamMember setRoleForProject(TeamMember teamMember, Long projectId, String role) {
        teamMember.setRoleForProject(projectId, role);
        return teamMemberRepository.save(teamMember);
    }

    public String getRoleForProject(TeamMember teamMember, Long projectId) {
        return teamMember.getRoleForProject(projectId);
    }

    public String removeRoleForProject(TeamMember teamMember, Long projectId) {
        String removedRole = teamMember.removeRoleForProject(projectId);
        teamMemberRepository.save(teamMember);
        return removedRole;
    }

    public boolean hasAvatar(TeamMember teamMember) {
        return teamMember.hasAvatar();
    }

    public boolean hasBio(TeamMember teamMember) {
        return teamMember.hasBio();
    }

    public int getProjectsCount(TeamMember teamMember) {
        return teamMember.getProjectsCount();
    }

    public String getInitials(TeamMember teamMember) {
        return teamMember.getInitials();
    }

    public String getDisplayRoleForProject(TeamMember teamMember, Long projectId) {
        return teamMember.getDisplayRoleForProject(projectId);
    }

    public boolean participatesInProject(TeamMember teamMember, Project project) {
        return teamMember.participatesInProject(project);
    }

    public boolean participatesInProject(TeamMember teamMember, Long projectId) {
        return teamMember.participatesInProject(projectId);
    }

    public List<TeamMember> updateSortOrder(List<TeamMember> teamMembers) {
        return teamMemberRepository.saveAll(teamMembers);
    }
}