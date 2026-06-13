package com.example.demo.controller;

import com.example.demo.dto.ProjectRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectController(ProjectService projectService,
                             ProjectRepository projectRepository,
                             UserRepository userRepository) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequest request,
                                                 HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
        if(userId == null){
            throw new RuntimeException("No autenticado");
        }
        return ResponseEntity.ok(projectService.createProject(request, userId));
    }
    @GetMapping
    public List<Project> getProjects(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if(userId == null){
            throw new RuntimeException("No autenticado");
        }
        return projectService.getProjectsByUser(userId);
    }


    @GetMapping("/debug")
    public String debug() {
        List<Project> projects = projectService.getAllProjects();
        return "Projects encontrados: " + projects.size();
    }

    @GetMapping("/{id}/users")
    public List<UserResponse> getProjectUsers(
            @PathVariable Long id
    ) {

        Project project = projectRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Proyecto no encontrado")
                );

        return project.getUsers()
                .stream()
                .map(UserResponse::new)
                .toList();
    }

    @Transactional
    @PostMapping("/{projectId}/users/{userId}")
    public void addUserToProject(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest
    ) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Proyecto no encontrado")
                );

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        // verificar permiso
        Long currentUserId =
                (Long) httpRequest.getAttribute("userId");

        if (currentUserId == null) {
            throw new RuntimeException("No autenticado");
        }

        verifyProjectOwner(project, currentUserId);

        // evitar duplicados

        boolean alreadyExists = project.getUsers()
                .stream()
                .anyMatch(u -> u.getId().equals(userId));

        if (!alreadyExists) {

            project.getUsers().add(user);

            projectRepository.save(project);
        }
    }

    @Transactional
    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest
    ) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Proyecto no encontrado")
                );

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        Long currentUserId =
                (Long) httpRequest.getAttribute("userId");

        if (currentUserId == null) {
            throw new RuntimeException("No autenticado");
        }

        verifyProjectOwner(project, currentUserId);

        project.getUsers().removeIf(
                u -> u.getId().equals(userId)
        );

        projectRepository.save(project);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectRequest request,
            HttpServletRequest httpRequest
    ) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        if (userId == null) {

            throw new RuntimeException(
                    "No autenticado"
            );
        }

        Project project = projectRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Proyecto no encontrado"
                        )
                );

        // Verificar acceso

        verifyProjectOwner(project, userId);

        project.setName(
                request.getName()
        );

        project.setDescription(
                request.getDescription()
        );

        Project updatedProject =
                projectRepository.save(project);

        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        if (userId == null) {

            throw new RuntimeException(
                    "No autenticado"
            );
        }

        Project project = projectRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Proyecto no encontrado"
                        )
                );

        // Verificar acceso

        verifyProjectOwner(project, userId);

        projectRepository.delete(project);

        return ResponseEntity.noContent().build();
    }

    private void verifyProjectOwner(Project project, Long userId) {
        if (project.getCreatedBy() == null ||
                !project.getCreatedBy().getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el creador puede modificar este proyecto"
            );
        }
    }

}
