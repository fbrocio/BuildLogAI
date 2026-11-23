package com.example.demo.service;

import com.example.demo.dto.ProjectRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(ProjectRequest request, Long userId) {
        User user = userRepository.findById(requireUserId(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(user);
        project.getUsers().add(user);
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByUser(Long userId) {
        return projectRepository.findByUsers_Id(requireUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<User> getProjectUsers(Long projectId, Long userId) {
        Project project = findProject(projectId);
        requireMember(project, requireUserId(userId));
        return List.copyOf(project.getUsers());
    }

    @Transactional
    public void addUserToProject(Long projectId, Long userId, Long currentUserId) {
        Project project = findProject(projectId);
        requireOwner(project, requireUserId(currentUserId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (project.getUsers().stream().noneMatch(member -> member.getId().equals(user.getId()))) {
            project.getUsers().add(user);
        }
    }

    @Transactional
    public void removeUserFromProject(Long projectId, Long userId, Long currentUserId) {
        Project project = findProject(projectId);
        requireOwner(project, requireUserId(currentUserId));
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        project.getUsers().removeIf(member -> member.getId().equals(userId));
    }

    @Transactional
    public Project updateProject(Long projectId, ProjectRequest request, Long userId) {
        Project project = findProject(projectId);
        requireOwner(project, requireUserId(userId));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return project;
    }

    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = findProject(projectId);
        requireOwner(project, requireUserId(userId));
        projectRepository.delete(project);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void requireMember(Project project, Long userId) {
        if (project.getUsers().stream().noneMatch(member -> member.getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este proyecto");
        }
    }

    private void requireOwner(Project project, Long userId) {
        if (project.getCreatedBy() == null || !project.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el creador puede modificar este proyecto");
        }
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return userId;
    }
}
