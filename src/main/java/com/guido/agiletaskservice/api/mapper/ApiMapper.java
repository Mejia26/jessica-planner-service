package com.guido.agiletaskservice.api.mapper;

import com.guido.agiletaskservice.api.dto.BoardDtos;
import com.guido.agiletaskservice.api.dto.CalendarDtos;
import com.guido.agiletaskservice.api.dto.IssueDtos;
import com.guido.agiletaskservice.api.dto.ProjectDtos;
import com.guido.agiletaskservice.api.dto.ProjectOptionDtos;
import com.guido.agiletaskservice.api.dto.SprintDtos;
import com.guido.agiletaskservice.domain.entity.BoardColumnEntity;
import com.guido.agiletaskservice.domain.entity.BoardEntity;
import com.guido.agiletaskservice.domain.entity.CalendarCategoryEntity;
import com.guido.agiletaskservice.domain.entity.CalendarEventEntity;
import com.guido.agiletaskservice.domain.entity.IssueAttachmentEntity;
import com.guido.agiletaskservice.domain.entity.IssueCommentEntity;
import com.guido.agiletaskservice.domain.entity.IssueEntity;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.entity.ProjectOptionEntity;
import com.guido.agiletaskservice.domain.entity.SprintEntity;
import com.guido.agiletaskservice.domain.enums.StatusCategory;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static ProjectDtos.ProjectResponse toProjectResponse(ProjectEntity project) {
        return new ProjectDtos.ProjectResponse(
                project.getId(),
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getCreatedByUserId(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getDeletedAt()
        );
    }

    public static BoardDtos.BoardResponse toBoardResponse(BoardEntity board, List<BoardColumnEntity> columns) {
        return new BoardDtos.BoardResponse(
                board.getId(),
                board.getProject().getId(),
                board.getName(),
                board.getType(),
                board.getWorkflowMode(),
                columns.stream().map(ApiMapper::toBoardColumnResponse).toList(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                board.getDeletedAt()
        );
    }

    public static BoardDtos.BoardColumnResponse toBoardColumnResponse(BoardColumnEntity column) {
        return new BoardDtos.BoardColumnResponse(
                column.getId(),
                column.getName(),
                column.getStatusKey(),
                column.getStatusCategory(),
                column.getPosition(),
                column.getWipLimit(),
                column.getCreatedAt(),
                column.getUpdatedAt(),
                column.getDeletedAt()
        );
    }

    public static ProjectOptionDtos.ProjectOptionResponse toProjectOptionResponse(ProjectOptionEntity option) {
        return new ProjectOptionDtos.ProjectOptionResponse(
                option.getId(),
                option.getProject().getId(),
                option.getType(),
                option.getKey(),
                option.getName(),
                option.getDescription(),
                option.getColor(),
                option.getPosition(),
                option.getCreatedAt(),
                option.getUpdatedAt(),
                option.getDeletedAt()
        );
    }

    public static SprintDtos.SprintResponse toSprintResponse(SprintEntity sprint) {
        return new SprintDtos.SprintResponse(
                sprint.getId(),
                sprint.getProject().getId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStartedAt(),
                sprint.getCompletedAt(),
                sprint.getCommitmentBaselinePoints(),
                sprint.getCreatedAt(),
                sprint.getUpdatedAt(),
                sprint.getDeletedAt()
        );
    }

    public static IssueDtos.IssueResponse toIssueResponse(IssueEntity issue) {
        return toIssueResponse(issue, null);
    }

    public static IssueDtos.IssueResponse toIssueResponse(IssueEntity issue, StatusCategory statusCategory) {
        return new IssueDtos.IssueResponse(
                issue.getId(),
                issue.getProject().getId(),
                issue.getBoard().getId(),
                Optional.ofNullable(issue.getSprint()).map(SprintEntity::getId).orElse(null),
                Optional.ofNullable(issue.getParentIssue()).map(IssueEntity::getId).orElse(null),
                issue.getTitle(),
                issue.getDescription(),
                issue.getType(),
                issue.getPriority(),
                issue.getStatus(),
                statusCategory,
                issue.getResolution(),
                issue.getClassOfService(),
                issue.getCategoryKey(),
                issue.getComponentKey(),
                new LinkedHashSet<>(issue.getLabels()),
                issue.getAssigneeUserId(),
                issue.getReporterUserId(),
                issue.getStoryPoints(),
                issue.getDueDate(),
                issue.getPosition(),
                issue.getSprintScopeAdded(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getDeletedAt()
        );
    }

    public static IssueDtos.CommentResponse toCommentResponse(IssueCommentEntity comment) {
        return new IssueDtos.CommentResponse(
                comment.getId(),
                comment.getIssue().getId(),
                comment.getAuthorUserId(),
                comment.getBody(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public static IssueDtos.AttachmentResponse toAttachmentResponse(IssueAttachmentEntity attachment) {
        return new IssueDtos.AttachmentResponse(
                attachment.getId(),
                attachment.getIssue().getId(),
                attachment.getUploadedByUserId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getStorageKey(),
                attachment.getPublicUrl(),
                attachment.getCreatedAt()
        );
    }

    public static CalendarDtos.CalendarEventResponse toCalendarEventResponse(CalendarEventEntity event) {
        return new CalendarDtos.CalendarEventResponse(
                event.getId(),
                event.getOwnerUserId(),
                event.getTitle(),
                event.getNotes(),
                event.getLocation(),
                event.getStartAt(),
                event.getEndAt(),
                event.isAllDay(),
                event.getTimeZone(),
                event.getCategoryKey(),
                event.getColor(),
                event.getStatus(),
                event.getReminderAt(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getDeletedAt()
        );
    }

    public static CalendarDtos.CalendarCategoryResponse toCalendarCategoryResponse(CalendarCategoryEntity category) {
        return new CalendarDtos.CalendarCategoryResponse(
                category.getId(),
                category.getOwnerUserId(),
                category.getKey(),
                category.getName(),
                category.getDescription(),
                category.getColor(),
                category.getPosition(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDeletedAt()
        );
    }
}
