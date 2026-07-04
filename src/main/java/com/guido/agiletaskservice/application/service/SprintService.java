package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.SprintDtos;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.IssueEntity;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.entity.SprintEntity;
import com.guido.agiletaskservice.domain.enums.IssueType;
import com.guido.agiletaskservice.domain.enums.SprintCompletionDestination;
import com.guido.agiletaskservice.domain.enums.SprintStatus;
import com.guido.agiletaskservice.domain.enums.StatusCategory;
import com.guido.agiletaskservice.domain.repository.IssueRepository;
import com.guido.agiletaskservice.domain.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final BoardService boardService;

    @Transactional
    public SprintEntity create(SprintDtos.CreateSprintRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        ProjectEntity project = projectService.get(request.projectId());

        SprintEntity sprint = new SprintEntity();
        sprint.setProject(project);
        sprint.setName(request.name().trim());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        SprintEntity savedSprint = sprintRepository.save(sprint);
        log.info("Created sprint: sprintId={}, projectId={}", savedSprint.getId(), project.getId());
        return savedSprint;
    }

    @Transactional(readOnly = true)
    public SprintEntity get(UUID id) {
        return sprintRepository.findById(id)
                .filter(sprint -> !sprint.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint was not found."));
    }

    @Transactional(readOnly = true)
    public List<SprintEntity> listByProject(UUID projectId) {
        projectService.get(projectId);
        return sprintRepository.findByProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(projectId);
    }

    @Transactional
    public SprintEntity update(UUID id, SprintDtos.UpdateSprintRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        SprintEntity sprint = get(id);
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessRuleException("Completed sprints cannot be updated.");
        }
        sprint.setName(request.name().trim());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        SprintEntity savedSprint = sprintRepository.save(sprint);
        log.info("Updated sprint: sprintId={}", id);
        return savedSprint;
    }

    @Transactional
    public SprintEntity start(UUID id) {
        SprintEntity sprint = get(id);
        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BusinessRuleException("Only planned sprints can be started.");
        }
        sprintRepository.findFirstByProjectIdAndStatusAndDeletedAtIsNull(sprint.getProject().getId(), SprintStatus.ACTIVE)
                .ifPresent(activeSprint -> {
                    throw new BusinessRuleException("Project already has an active sprint.");
        });
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartedAt(Instant.now());
        int baseline = issueRepository.findBySprintIdAndDeletedAtIsNullOrderByPositionAsc(sprint.getId()).stream()
                .filter(issue -> issue.getType() != IssueType.EPIC)
                .map(IssueEntity::getStoryPoints)
                .mapToInt(points -> points == null ? 0 : points)
                .sum();
        sprint.setCommitmentBaselinePoints(baseline);
        issueRepository.findBySprintIdAndDeletedAtIsNullOrderByPositionAsc(sprint.getId())
                .forEach(issue -> {
                    issue.setSprintScopeAdded(false);
                    issueRepository.save(issue);
                });
        SprintEntity savedSprint = sprintRepository.save(sprint);
        log.info("Started sprint: sprintId={}, projectId={}, commitmentBaselinePoints={}", id, sprint.getProject().getId(), baseline);
        return savedSprint;
    }

    @Transactional
    public SprintEntity complete(UUID id, SprintDtos.CompleteSprintRequest request) {
        SprintEntity sprint = get(id);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BusinessRuleException("Only active sprints can be completed.");
        }
        SprintCompletionDestination destination = request == null ? SprintCompletionDestination.BACKLOG : request.destination();
        SprintEntity targetSprint = resolveCompletionTarget(sprint, destination, request == null ? null : request.targetSprintId());

        issueRepository.findBySprintIdAndDeletedAtIsNullOrderByPositionAsc(sprint.getId())
                .forEach(issue -> {
                    boolean trulyDone = boardService.resolveStatusCategory(issue.getBoard().getId(), issue.getStatus()) == StatusCategory.DONE
                            && issue.getResolution() != null;
                    if (!trulyDone) {
                        issue.setSprint(targetSprint);
                        issue.setSprintScopeAdded(false);
                        issueRepository.save(issue);
                    }
                });

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedAt(Instant.now());
        SprintEntity savedSprint = sprintRepository.save(sprint);
        log.info("Completed sprint: sprintId={}, projectId={}, unfinishedDestination={}, targetSprintId={}",
                id, sprint.getProject().getId(), destination, targetSprint == null ? null : targetSprint.getId());
        return savedSprint;
    }

    @Transactional
    public SprintEntity complete(UUID id) {
        return complete(id, new SprintDtos.CompleteSprintRequest(SprintCompletionDestination.BACKLOG, null));
    }

    @Transactional
    public void archive(UUID id, UUID userId) {
        SprintEntity sprint = get(id);
        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BusinessRuleException("Active sprints must be completed before they can be archived.");
        }
        sprint.softDelete(userId);
        sprintRepository.save(sprint);
        log.info("Archived sprint: sprintId={}, deletedByUserId={}", id, userId);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessRuleException("Sprint end date cannot be before start date.");
        }
    }

    private SprintEntity resolveCompletionTarget(SprintEntity sprint, SprintCompletionDestination destination, UUID targetSprintId) {
        if (destination == SprintCompletionDestination.BACKLOG) {
            return null;
        }
        if (targetSprintId == null) {
            throw new BusinessRuleException("Target sprint id is required when unfinished work moves to a future sprint.");
        }
        SprintEntity targetSprint = get(targetSprintId);
        if (!targetSprint.getProject().getId().equals(sprint.getProject().getId())) {
            throw new BusinessRuleException("Target sprint must belong to the same project.");
        }
        if (targetSprint.getStatus() != SprintStatus.PLANNED) {
            throw new BusinessRuleException("Unfinished work can only move to a planned future sprint.");
        }
        return targetSprint;
    }
}
