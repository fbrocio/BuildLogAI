package com.example.demo.controller;

import com.example.demo.dto.ProjectRequest;
import com.example.demo.dto.ProjectResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProjectResponse(
                projectService.createProject(request, userId(httpRequest))));
    }

    @GetMapping
    public List<ProjectResponse> getProjects(HttpServletRequest request) {
        return projectService.getProjectsByUser(userId(request)).stream().map(ProjectResponse::new).toList();
    }

    @GetMapping("/{id}/users")
    public List<UserResponse> getProjectUsers(@PathVariable Long id, HttpServletRequest request) {
        return projectService.getProjectUsers(id, userId(request)).stream().map(UserResponse::new).toList();
    }

    @PostMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> addUserToProject(
            @PathVariable Long projectId, @PathVariable Long userId, HttpServletRequest request) {
        projectService.addUserToProject(projectId, userId, userId(request));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable Long projectId, @PathVariable Long userId, HttpServletRequest request) {
        projectService.removeUserFromProject(projectId, userId, userId(request));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id, @Valid @RequestBody ProjectRequest request, HttpServletRequest httpRequest) {
        return new ProjectResponse(projectService.updateProject(id, request, userId(httpRequest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, HttpServletRequest request) {
        projectService.deleteProject(id, userId(request));
        return ResponseEntity.noContent().build();
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
