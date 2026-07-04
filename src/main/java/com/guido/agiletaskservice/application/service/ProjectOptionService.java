package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.ProjectOptionDtos;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.entity.ProjectOptionEntity;
import com.guido.agiletaskservice.domain.enums.ProjectOptionType;
import com.guido.agiletaskservice.domain.repository.ProjectOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectOptionService {

    private final ProjectOptionRepository projectOptionRepository;
    private final ProjectService projectService;

    @Transactional
    public ProjectOptionEntity create(UUID projectId, ProjectOptionDtos.CreateProjectOptionRequest request) {
        ProjectEntity project = projectService.get(projectId);
        String normalizedKey = normalizeKey(request.key());
        ensureKeyAvailable(projectId, request.type(), normalizedKey, null);

        ProjectOptionEntity option = new ProjectOptionEntity();
        option.setProject(project);
        option.setType(request.type());
        option.setKey(normalizedKey);
        option.setName(request.name().trim());
        option.setDescription(request.description());
        option.setColor(request.color());
        option.setPosition(request.position() == null ? 0 : request.position());
        ProjectOptionEntity savedOption = projectOptionRepository.save(option);
        log.info("Created project option: projectId={}, optionId={}, type={}, key={}", projectId, savedOption.getId(), savedOption.getType(), savedOption.getKey());
        return savedOption;
    }

    @Transactional(readOnly = true)
    public ProjectOptionEntity get(UUID id) {
        return projectOptionRepository.findById(id)
                .filter(option -> !option.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Project option was not found."));
    }

    @Transactional(readOnly = true)
    public List<ProjectOptionEntity> list(UUID projectId, ProjectOptionType type) {
        projectService.get(projectId);
        if (type == null) {
            return projectOptionRepository.findByProjectIdAndDeletedAtIsNullOrderByTypeAscPositionAscNameAsc(projectId);
        }
        return projectOptionRepository.findByProjectIdAndTypeAndDeletedAtIsNullOrderByPositionAscNameAsc(projectId, type);
    }

    @Transactional
    public ProjectOptionEntity update(UUID projectId, UUID id, ProjectOptionDtos.UpdateProjectOptionRequest request) {
        ProjectOptionEntity option = get(id);
        validateOptionBelongsToProject(option, projectId);
        String normalizedKey = normalizeKey(request.key());
        ensureKeyAvailable(option.getProject().getId(), option.getType(), normalizedKey, option.getId());
        option.setKey(normalizedKey);
        option.setName(request.name().trim());
        option.setDescription(request.description());
        option.setColor(request.color());
        option.setPosition(request.position());
        ProjectOptionEntity savedOption = projectOptionRepository.save(option);
        log.info("Updated project option: optionId={}, type={}, key={}", id, savedOption.getType(), savedOption.getKey());
        return savedOption;
    }

    @Transactional
    public void archive(UUID projectId, UUID id, UUID userId) {
        ProjectOptionEntity option = get(id);
        validateOptionBelongsToProject(option, projectId);
        option.softDelete(userId);
        projectOptionRepository.save(option);
        log.info("Archived project option: optionId={}, deletedByUserId={}", id, userId);
    }

    @Transactional
    public void ensureIssueOptionKeys(UUID projectId, String categoryKey, String componentKey, Set<String> labels) {
        ensureOptionExists(projectId, ProjectOptionType.ISSUE_CATEGORY, categoryKey, false);
        ensureOptionExists(projectId, ProjectOptionType.ISSUE_COMPONENT, componentKey, false);
        if (labels != null) {
            labels.stream()
                    .filter(StringUtils::hasText)
                    .map(this::normalizeKey)
                    .forEach(labelKey -> ensureOptionExists(projectId, ProjectOptionType.ISSUE_LABEL, labelKey, true));
        }
    }

    public Set<String> normalizeKeys(Set<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeKey)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim()
                .replaceAll("[^a-zA-Z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase(Locale.ROOT);
    }

    private void ensureOptionExists(UUID projectId, ProjectOptionType type, String key, boolean createWhenMissing) {
        String normalizedKey = normalizeKey(key);
        if (!StringUtils.hasText(normalizedKey)) {
            return;
        }
        boolean exists = projectOptionRepository.existsByProjectIdAndTypeAndKeyIgnoreCaseAndDeletedAtIsNull(projectId, type, normalizedKey);
        if (exists) {
            return;
        }
        if (!createWhenMissing) {
            throw new BusinessRuleException(type + " key does not exist for the selected project.");
        }

        ProjectEntity project = projectService.get(projectId);
        ProjectOptionEntity option = new ProjectOptionEntity();
        option.setProject(project);
        option.setType(type);
        option.setKey(normalizedKey);
        option.setName(toDisplayName(normalizedKey));
        option.setPosition(0);
        projectOptionRepository.save(option);
        log.info("Auto-created project label: projectId={}, key={}", projectId, normalizedKey);
    }

    private void ensureKeyAvailable(UUID projectId, ProjectOptionType type, String key, UUID currentOptionId) {
        projectOptionRepository.findByProjectIdAndTypeAndKeyIgnoreCaseAndDeletedAtIsNull(projectId, type, key)
                .filter(option -> currentOptionId == null || !option.getId().equals(currentOptionId))
                .ifPresent(option -> {
                    throw new BusinessRuleException("A project option with this type and key already exists.");
                });
    }

    private String toDisplayName(String key) {
        String value = key.replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT);
        return StringUtils.capitalize(value);
    }

    private void validateOptionBelongsToProject(ProjectOptionEntity option, UUID projectId) {
        if (!option.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Project option was not found for the selected project.");
        }
    }
}
