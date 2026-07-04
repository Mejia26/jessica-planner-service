package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.IssueDtos;
import com.guido.agiletaskservice.application.port.ResourceStorageService;
import com.guido.agiletaskservice.application.query.IssueSearchCriteria;
import com.guido.agiletaskservice.application.query.IssueSpecifications;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.BoardEntity;
import com.guido.agiletaskservice.domain.entity.BoardColumnEntity;
import com.guido.agiletaskservice.domain.entity.IssueAttachmentEntity;
import com.guido.agiletaskservice.domain.entity.IssueCommentEntity;
import com.guido.agiletaskservice.domain.entity.IssueEntity;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.entity.SprintEntity;
import com.guido.agiletaskservice.domain.enums.BoardType;
import com.guido.agiletaskservice.domain.enums.ClassOfService;
import com.guido.agiletaskservice.domain.enums.IssueResolution;
import com.guido.agiletaskservice.domain.enums.IssueType;
import com.guido.agiletaskservice.domain.enums.SprintStatus;
import com.guido.agiletaskservice.domain.enums.StatusCategory;
import com.guido.agiletaskservice.domain.enums.WorkflowMode;
import com.guido.agiletaskservice.domain.repository.IssueAttachmentRepository;
import com.guido.agiletaskservice.domain.repository.IssueCommentRepository;
import com.guido.agiletaskservice.domain.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final IssueAttachmentRepository issueAttachmentRepository;
    private final ProjectService projectService;
    private final BoardService boardService;
    private final SprintService sprintService;
    private final ProjectOptionService projectOptionService;
    private final ResourceStorageService resourceStorageService;

    @Transactional
    public IssueEntity create(IssueDtos.CreateIssueRequest request, UUID reporterUserId) {
        ProjectEntity project = projectService.get(request.projectId());
        BoardEntity board = boardService.get(request.boardId());
        validateBoardBelongsToProject(board, project.getId());
        BoardColumnEntity targetColumn = boardService.getColumnByStatus(board.getId(), request.status());

        SprintEntity sprint = request.sprintId() == null ? null : sprintService.get(request.sprintId());
        validateSprintBelongsToProject(sprint, project.getId());
        IssueEntity parentIssue = request.parentIssueId() == null ? null : get(request.parentIssueId());
        validateParentIssue(parentIssue, project.getId());
        validateIssueHierarchy(request.type(), parentIssue);
        sprint = resolveSprintForHierarchy(request.type(), sprint, parentIssue);
        validateEpicActiveSprint(request.type(), sprint);
        validateDoneResolution(targetColumn.getStatusCategory(), request.resolution());
        Set<String> labels = projectOptionService.normalizeKeys(request.labels());
        String categoryKey = projectOptionService.normalizeKey(request.categoryKey());
        String componentKey = projectOptionService.normalizeKey(request.componentKey());
        projectOptionService.ensureIssueOptionKeys(project.getId(), categoryKey, componentKey, labels);
        ClassOfService classOfService = request.classOfService() == null ? ClassOfService.STANDARD : request.classOfService();
        enforceKanbanWip(board, targetColumn, classOfService, false, null);

        IssueEntity issue = new IssueEntity();
        issue.setProject(project);
        issue.setBoard(board);
        issue.setSprint(sprint);
        issue.setParentIssue(parentIssue);
        issue.setTitle(request.title().trim());
        issue.setDescription(request.description());
        issue.setType(request.type());
        issue.setPriority(request.priority());
        issue.setStatus(normalizeStatus(request.status()));
        issue.setResolution(request.resolution());
        issue.setClassOfService(classOfService);
        issue.setCategoryKey(categoryKey);
        issue.setComponentKey(componentKey);
        issue.getLabels().addAll(labels);
        issue.setAssigneeUserId(request.assigneeUserId());
        issue.setReporterUserId(reporterUserId);
        issue.setStoryPoints(request.storyPoints());
        issue.setDueDate(request.dueDate());
        issue.setPosition(request.position() == null ? 0 : request.position());
        issue.setSprintScopeAdded(sprint != null && sprint.getStatus() == SprintStatus.ACTIVE);

        IssueEntity savedIssue = issueRepository.save(issue);
        log.info("Created issue: issueId={}, projectId={}, boardId={}, reporterUserId={}", savedIssue.getId(), project.getId(), board.getId(), reporterUserId);
        return savedIssue;
    }

    @Transactional
    public IssueDtos.IssueResponse createResponse(IssueDtos.CreateIssueRequest request, UUID reporterUserId) {
        return toResponse(create(request, reporterUserId));
    }

    @Transactional(readOnly = true)
    public IssueEntity get(UUID id) {
        return issueRepository.findById(id)
                .filter(issue -> !issue.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Issue was not found."));
    }

    @Transactional(readOnly = true)
    public IssueDtos.IssueResponse getResponse(UUID id) {
        return toResponse(get(id));
    }

    @Transactional(readOnly = true)
    public Page<IssueEntity> search(IssueSearchCriteria criteria, Pageable pageable) {
        return issueRepository.findAll(IssueSpecifications.byCriteria(criteria), pageable);
    }

    @Transactional(readOnly = true)
    public Page<IssueDtos.IssueResponse> searchResponses(IssueSearchCriteria criteria, Pageable pageable) {
        return search(criteria, pageable).map(this::toResponse);
    }

    @Transactional
    public IssueEntity update(UUID id, IssueDtos.UpdateIssueRequest request) {
        IssueEntity issue = get(id);
        BoardColumnEntity targetColumn = boardService.getColumnByStatus(issue.getBoard().getId(), request.status());
        IssueEntity parentIssue = request.parentIssueId() == null ? null : get(request.parentIssueId());
        validateParentIssue(parentIssue, issue.getProject().getId());
        validateIssueHierarchy(request.type(), parentIssue);
        validateEpicActiveSprint(request.type(), issue.getSprint());
        validateDoneResolution(targetColumn.getStatusCategory(), request.resolution());
        Set<String> labels = projectOptionService.normalizeKeys(request.labels());
        String categoryKey = projectOptionService.normalizeKey(request.categoryKey());
        String componentKey = projectOptionService.normalizeKey(request.componentKey());
        projectOptionService.ensureIssueOptionKeys(issue.getProject().getId(), categoryKey, componentKey, labels);
        SprintEntity sprint = resolveSprintForHierarchy(request.type(), issue.getSprint(), parentIssue);
        ClassOfService classOfService = request.classOfService() == null ? ClassOfService.STANDARD : request.classOfService();
        issue.setTitle(request.title().trim());
        issue.setDescription(request.description());
        issue.setType(request.type());
        issue.setParentIssue(parentIssue);
        issue.setSprint(sprint);
        issue.setPriority(request.priority());
        issue.setStatus(normalizeStatus(request.status()));
        issue.setResolution(request.resolution());
        issue.setClassOfService(classOfService);
        issue.setCategoryKey(categoryKey);
        issue.setComponentKey(componentKey);
        issue.getLabels().clear();
        issue.getLabels().addAll(labels);
        issue.setAssigneeUserId(request.assigneeUserId());
        issue.setStoryPoints(request.storyPoints());
        issue.setDueDate(request.dueDate());
        IssueEntity savedIssue = issueRepository.save(issue);
        log.info("Updated issue: issueId={}", id);
        return savedIssue;
    }

    @Transactional
    public IssueDtos.IssueResponse updateResponse(UUID id, IssueDtos.UpdateIssueRequest request) {
        return toResponse(update(id, request));
    }

    @Transactional
    public IssueEntity assign(UUID id, IssueDtos.AssignIssueRequest request) {
        IssueEntity issue = get(id);
        issue.setAssigneeUserId(request.assigneeUserId());
        IssueEntity savedIssue = issueRepository.save(issue);
        log.info("Assigned issue: issueId={}, assigneeUserId={}", id, request.assigneeUserId());
        return savedIssue;
    }

    @Transactional
    public IssueDtos.IssueResponse assignResponse(UUID id, IssueDtos.AssignIssueRequest request) {
        return toResponse(assign(id, request));
    }

    @Transactional
    public void archive(UUID id, UUID userId) {
        IssueEntity issue = get(id);
        issue.softDelete(userId);
        issueRepository.save(issue);
        log.info("Archived issue: issueId={}, deletedByUserId={}", id, userId);
    }

    @Transactional
    public IssueEntity move(UUID id, IssueDtos.MoveIssueRequest request) {
        IssueEntity issue = get(id);
        BoardEntity board = boardService.get(request.boardId());
        validateBoardBelongsToProject(board, issue.getProject().getId());
        BoardColumnEntity targetColumn = boardService.getColumnByStatus(board.getId(), request.status());
        validateWorkflowTransition(board, issue.getStatus(), targetColumn);
        SprintEntity sprint = request.sprintId() == null ? null : sprintService.get(request.sprintId());
        validateSprintBelongsToProject(sprint, issue.getProject().getId());
        sprint = resolveSprintForHierarchy(issue.getType(), sprint, issue.getParentIssue());
        validateEpicActiveSprint(issue.getType(), sprint);
        IssueResolution resolution = targetColumn.getStatusCategory() == StatusCategory.DONE
                ? (request.resolution() == null ? issue.getResolution() : request.resolution())
                : null;
        validateDoneResolution(targetColumn.getStatusCategory(), resolution);
        enforceKanbanWip(board, targetColumn, issue.getClassOfService(), Boolean.TRUE.equals(request.overrideWipLimit()), issue);

        issue.setBoard(board);
        issue.setStatus(normalizeStatus(request.status()));
        issue.setResolution(resolution);
        if (sprint != null && sprint.getStatus() == SprintStatus.ACTIVE && (issue.getSprint() == null || !issue.getSprint().getId().equals(sprint.getId()))) {
            issue.setSprintScopeAdded(true);
        }
        issue.setSprint(sprint);
        issue.setPosition(request.position());
        IssueEntity savedIssue = issueRepository.save(issue);
        log.info("Moved issue: issueId={}, boardId={}, status={}, sprintId={}, position={}",
                id, board.getId(), issue.getStatus(), request.sprintId(), request.position());
        return savedIssue;
    }

    @Transactional
    public IssueDtos.IssueResponse moveResponse(UUID id, IssueDtos.MoveIssueRequest request) {
        return toResponse(move(id, request));
    }

    @Transactional
    public IssueCommentEntity addComment(UUID issueId, IssueDtos.CreateCommentRequest request, UUID authorUserId) {
        IssueEntity issue = get(issueId);
        IssueCommentEntity comment = new IssueCommentEntity();
        comment.setIssue(issue);
        comment.setAuthorUserId(authorUserId);
        comment.setBody(request.body().trim());
        IssueCommentEntity savedComment = issueCommentRepository.save(comment);
        log.info("Added issue comment: issueId={}, commentId={}, authorUserId={}", issueId, savedComment.getId(), authorUserId);
        return savedComment;
    }

    @Transactional(readOnly = true)
    public List<IssueCommentEntity> listComments(UUID issueId) {
        get(issueId);
        return issueCommentRepository.findByIssueIdAndDeletedAtIsNullOrderByCreatedAtAsc(issueId);
    }

    @Transactional
    public void archiveComment(UUID issueId, UUID commentId, UUID userId) {
        get(issueId);
        IssueCommentEntity comment = issueCommentRepository.findById(commentId)
                .filter(item -> !item.isDeleted())
                .filter(item -> item.getIssue().getId().equals(issueId))
                .orElseThrow(() -> new ResourceNotFoundException("Issue comment was not found."));
        comment.softDelete(userId);
        issueCommentRepository.save(comment);
        log.info("Archived issue comment: issueId={}, commentId={}, deletedByUserId={}", issueId, commentId, userId);
    }

    @Transactional
    public IssueAttachmentEntity addAttachment(UUID issueId, MultipartFile file, UUID uploadedByUserId) {
        IssueEntity issue = get(issueId);
        if (file.isEmpty()) {
            throw new BusinessRuleException("Uploaded file cannot be empty.");
        }

        ResourceStorageService.StoredResource storedResource = resourceStorageService.store("issues/" + issueId, file);
        IssueAttachmentEntity attachment = new IssueAttachmentEntity();
        attachment.setIssue(issue);
        attachment.setUploadedByUserId(uploadedByUserId);
        attachment.setFileName(file.getOriginalFilename() == null ? "resource" : file.getOriginalFilename());
        attachment.setContentType(storedResource.contentType() == null ? "application/octet-stream" : storedResource.contentType());
        attachment.setSizeBytes(storedResource.sizeBytes());
        attachment.setStorageKey(storedResource.storageKey());
        attachment.setPublicUrl(storedResource.publicUrl());
        IssueAttachmentEntity savedAttachment = issueAttachmentRepository.save(attachment);
        log.info("Added issue attachment: issueId={}, attachmentId={}, sizeBytes={}", issueId, savedAttachment.getId(), savedAttachment.getSizeBytes());
        return savedAttachment;
    }

    @Transactional(readOnly = true)
    public List<IssueAttachmentEntity> listAttachments(UUID issueId) {
        get(issueId);
        return issueAttachmentRepository.findByIssueIdAndDeletedAtIsNullOrderByCreatedAtDesc(issueId);
    }

    @Transactional
    public void archiveAttachment(UUID issueId, UUID attachmentId, UUID userId) {
        get(issueId);
        IssueAttachmentEntity attachment = issueAttachmentRepository.findById(attachmentId)
                .filter(item -> !item.isDeleted())
                .filter(item -> item.getIssue().getId().equals(issueId))
                .orElseThrow(() -> new ResourceNotFoundException("Issue attachment was not found."));
        attachment.softDelete(userId);
        issueAttachmentRepository.save(attachment);
        log.info("Archived issue attachment: issueId={}, attachmentId={}, deletedByUserId={}", issueId, attachmentId, userId);
    }

    private void validateBoardBelongsToProject(BoardEntity board, UUID projectId) {
        if (!board.getProject().getId().equals(projectId)) {
            throw new BusinessRuleException("Board does not belong to the selected project.");
        }
    }

    private void validateSprintBelongsToProject(SprintEntity sprint, UUID projectId) {
        if (sprint != null && !sprint.getProject().getId().equals(projectId)) {
            throw new BusinessRuleException("Sprint does not belong to the selected project.");
        }
    }

    private void validateParentIssue(IssueEntity parentIssue, UUID projectId) {
        if (parentIssue != null && !parentIssue.getProject().getId().equals(projectId)) {
            throw new BusinessRuleException("Parent issue does not belong to the selected project.");
        }
    }

    private void validateIssueHierarchy(IssueType type, IssueEntity parentIssue) {
        if (type == IssueType.EPIC && parentIssue != null) {
            throw new BusinessRuleException("Epics cannot have a parent issue.");
        }
        if (type == IssueType.SUBTASK && parentIssue == null) {
            throw new BusinessRuleException("Subtasks must have a parent issue.");
        }
        if (parentIssue != null && parentIssue.getType() == IssueType.SUBTASK) {
            throw new BusinessRuleException("Subtasks cannot be used as parent issues.");
        }
        if (parentIssue != null && type != IssueType.SUBTASK && parentIssue.getType() != IssueType.EPIC) {
            throw new BusinessRuleException("Stories, tasks, and bugs can only use an epic as parent.");
        }
        if (parentIssue != null && type == IssueType.SUBTASK && parentIssue.getType() == IssueType.EPIC) {
            throw new BusinessRuleException("Subtasks must belong to a story, task, or bug, not directly to an epic.");
        }
    }

    private SprintEntity resolveSprintForHierarchy(IssueType type, SprintEntity requestedSprint, IssueEntity parentIssue) {
        if (type == IssueType.SUBTASK) {
            return parentIssue == null ? null : parentIssue.getSprint();
        }
        return requestedSprint;
    }

    private void validateEpicActiveSprint(IssueType type, SprintEntity sprint) {
        if (type == IssueType.EPIC && sprint != null && sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BusinessRuleException("Epics cannot be added directly to an active sprint. Add their stories, tasks, or bugs instead.");
        }
    }

    private void validateDoneResolution(StatusCategory statusCategory, IssueResolution resolution) {
        if (statusCategory == StatusCategory.DONE && resolution == null) {
            throw new BusinessRuleException("A resolution is required before an issue can enter a Done status category.");
        }
        if (statusCategory != StatusCategory.DONE && resolution != null) {
            throw new BusinessRuleException("Resolution can only be set when the issue is in a Done status category.");
        }
    }

    private void validateWorkflowTransition(BoardEntity board, String currentStatus, BoardColumnEntity targetColumn) {
        if (board.getWorkflowMode() != WorkflowMode.STRICT) {
            return;
        }
        List<BoardColumnEntity> columns = boardService.getColumns(board.getId());
        int currentPosition = columns.stream()
                .filter(column -> column.getStatusKey().equalsIgnoreCase(currentStatus))
                .findFirst()
                .map(BoardColumnEntity::getPosition)
                .orElse(targetColumn.getPosition());
        int distance = Math.abs(targetColumn.getPosition() - currentPosition);
        if (distance > 1) {
            throw new BusinessRuleException("Strict workflows only allow moving an issue to the previous, current, or next column.");
        }
    }

    private void enforceKanbanWip(BoardEntity board, BoardColumnEntity targetColumn, ClassOfService classOfService, boolean overrideRequested, IssueEntity currentIssue) {
        if (board.getType() != BoardType.KANBAN || targetColumn.getWipLimit() == null || classOfService == ClassOfService.EXPEDITE) {
            return;
        }
        boolean alreadyInTarget = currentIssue != null
                && currentIssue.getBoard().getId().equals(board.getId())
                && currentIssue.getStatus().equalsIgnoreCase(targetColumn.getStatusKey());
        if (alreadyInTarget) {
            return;
        }

        long count = issueRepository.countByBoardIdAndStatusIgnoreCaseAndClassOfServiceNotAndDeletedAtIsNull(
                board.getId(),
                targetColumn.getStatusKey(),
                ClassOfService.EXPEDITE
        );
        if (count < targetColumn.getWipLimit()) {
            return;
        }
        if (overrideRequested) {
            log.warn("Kanban WIP limit override accepted: boardId={}, statusKey={}, wipLimit={}, currentCount={}, issueId={}",
                    board.getId(), targetColumn.getStatusKey(), targetColumn.getWipLimit(), count, currentIssue == null ? null : currentIssue.getId());
            return;
        }
        throw new BusinessRuleException("Kanban WIP limit would be exceeded for the destination column.");
    }

    private IssueDtos.IssueResponse toResponse(IssueEntity issue) {
        StatusCategory statusCategory = boardService.resolveStatusCategory(issue.getBoard().getId(), issue.getStatus());
        return com.guido.agiletaskservice.api.mapper.ApiMapper.toIssueResponse(issue, statusCategory);
    }

    private String normalizeStatus(String status) {
        return status.trim().toUpperCase(Locale.ROOT);
    }
}
