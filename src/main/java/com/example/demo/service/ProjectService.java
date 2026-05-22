package com.example.demo.service;

import com.example.demo.dto.ProjectRequest;
import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public Project createProject(ProjectRequest request, Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(()-> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(user);
        project.setCreatedAt(LocalDateTime.now());

        project.getUsers().add(user);

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByUser(Long userId){
        return projectRepository.findByUsers_Id(userId);
    }

    public void addUserToProject(
            Long projectId,
            Long userId
    ) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow();

        User user = userRepository
                .findById(userId)
                .orElseThrow();

        // evitar duplicados

        if (!project.getUsers().contains(user)) {

            project.getUsers().add(user);

            projectRepository.save(project);
        }
    }
}
