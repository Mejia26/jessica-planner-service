package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.ProjectDtos;
import com.guido.agiletaskservice.common.exception.ConflictException;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectEntity create(ProjectDtos.CreateProjectRequest request, UUID userId) {
        String key = request.key().trim().toUpperCase();
        if (projectRepository.existsByKeyIgnoreCase(key)) {
            throw new ConflictException("Project key already exists.");
        }

        ProjectEntity project = new ProjectEntity();
        project.setKey(key);
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setCreatedByUserId(userId);
        ProjectEntity savedProject = projectRepository.save(project);
        log.info("Created project: projectId={}, key={}, userId={}", savedProject.getId(), savedProject.getKey(), userId);
        return savedProject;
    }

    @Transactional(readOnly = true)
    public ProjectEntity get(UUID id) {
        return projectRepository.findById(id)
                .filter(project -> !project.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }

    @Transactional(readOnly = true)
    public Page<ProjectEntity> search(String text, Pageable pageable) {
        Specification<ProjectEntity> specification = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (StringUtils.hasText(text)) {
            specification = specification.and((root, query, cb) -> {
            String value = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("key")), value),
                    cb.like(cb.lower(root.get("name")), value),
                    cb.like(cb.lower(root.get("description")), value)
            );
            });
        }
        return projectRepository.findAll(specification, pageable);
    }

    @Transactional
    public ProjectEntity update(UUID id, ProjectDtos.UpdateProjectRequest request) {
        ProjectEntity project = get(id);
        project.setName(request.name().trim());
        project.setDescription(request.description());
        ProjectEntity savedProject = projectRepository.save(project);
        log.info("Updated project: projectId={}", id);
        return savedProject;
    }

    @Transactional
    public void archive(UUID id, UUID userId) {
        ProjectEntity project = get(id);
        project.softDelete(userId);
        projectRepository.save(project);
        log.info("Archived project: projectId={}, deletedByUserId={}", id, userId);
    }
}
