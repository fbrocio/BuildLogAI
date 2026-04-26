package com.example.demo.controller;

import com.example.demo.dto.ProjectRequest;
import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.service.ProjectService;
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
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequest request){
        return ResponseEntity.ok(projectService.createProject(request));
    }
    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/debug")
    public String debug() {
        List<Project> projects = projectService.getAllProjects();
        return "Projects encontrados: " + projects.size();
    }

}
