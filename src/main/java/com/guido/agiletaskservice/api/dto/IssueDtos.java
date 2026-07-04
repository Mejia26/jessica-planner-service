package com.guido.agiletaskservice.api.dto;

import com.guido.agiletaskservice.domain.enums.ClassOfService;
import com.guido.agiletaskservice.domain.enums.IssuePriority;
import com.guido.agiletaskservice.domain.enums.IssueResolution;
import com.guido.agiletaskservice.domain.enums.IssueType;
import com.guido.agiletaskservice.domain.enums.StatusCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public final class IssueDtos {

    private IssueDtos() {
    }

    @Schema(description = "Payload used to create an issue, story, bug, epic, task, or subtask.")
    public record CreateIssueRequest(
            @NotNull UUID projectId,
            @NotNull UUID boardId,
            UUID sprintId,
            UUID parentIssueId,

            @NotBlank
            @Size(max = 220)
            @Schema(description = "Issue title.", example = "Create board column API")
            String title,

            @Size(max = 8000)
            @Schema(description = "Issue description in plain text or markdown.", example = "The frontend needs an endpoint to configure board columns.")
            String description,

            @NotNull
            @Schema(description = "Issue type.", example = "STORY")
            IssueType type,

            @NotNull
            @Schema(description = "Issue priority.", example = "HIGH")
            IssuePriority priority,

            @NotBlank
            @Size(max = 60)
            @Schema(description = "Workflow status key. It should match a board column status key.", example = "TODO")
            String status,

            @Schema(description = "Resolution explains why a work item is truly done. Required when the destination status category is DONE.", example = "FIXED")
            IssueResolution resolution,

            @Schema(description = "Kanban class of service. EXPEDITE items are excluded from WIP limit calculations.", example = "STANDARD")
            ClassOfService classOfService,

            @Size(max = 80)
            @Schema(description = "Optional project-defined category key.", example = "BACKEND")
            String categoryKey,

            @Size(max = 80)
            @Schema(description = "Optional project-defined component key.", example = "AUTH")
            String componentKey,

            @Schema(description = "Project-defined label keys used for filtering and triage.", example = "[\"REGRESSION\", \"QA\"]")
            Set<@Size(max = 80) String> labels,

            @Schema(description = "Assigned user id from the user microservice.")
            UUID assigneeUserId,

            @Min(0)
            @Max(1000)
            @Schema(description = "Story points for Scrum planning.", example = "5")
            Integer storyPoints,

            @Schema(description = "Optional due date.", example = "2026-07-10")
            LocalDate dueDate,

            @Min(0)
            @Schema(description = "Position inside the board column or sprint backlog.", example = "0")
            Integer position
    ) {
    }

    @Schema(description = "Payload used to update an issue.")
    public record UpdateIssueRequest(
            @NotBlank
            @Size(max = 220)
            String title,

            @Size(max = 8000)
            String description,

            @NotNull
            IssueType type,

            @NotNull
            IssuePriority priority,

            @NotBlank
            @Size(max = 60)
            String status,

            @Schema(description = "Parent issue id. Epics cannot have a parent. Subtasks must have a parent and inherit the parent's sprint.")
            UUID parentIssueId,

            @Schema(description = "Resolution explains why a work item is truly done. Required when the destination status category is DONE.")
            IssueResolution resolution,

            @Schema(description = "Kanban class of service. EXPEDITE items are excluded from WIP limit calculations.")
            ClassOfService classOfService,

            @Size(max = 80)
            String categoryKey,

            @Size(max = 80)
            String componentKey,

            Set<@Size(max = 80) String> labels,

            UUID assigneeUserId,

            @Min(0)
            @Max(1000)
            Integer storyPoints,

            LocalDate dueDate
    ) {
    }

    @Schema(description = "Payload used to move an issue between board columns or sprint backlog positions.")
    public record MoveIssueRequest(
            @NotNull UUID boardId,

            @NotBlank
            @Size(max = 60)
            @Schema(description = "Destination board column status key.", example = "DONE")
            String status,

            @Schema(description = "Destination sprint. Use null to move the issue to the backlog.")
            UUID sprintId,

            @Schema(description = "Resolution to apply during the move. Required when moving into a DONE category status.", example = "FIXED")
            IssueResolution resolution,

            @Schema(description = "Set true to allow a Kanban WIP limit violation. The service logs the override for audit visibility.", example = "false")
            Boolean overrideWipLimit,

            @NotNull
            @Min(0)
            @Schema(description = "Destination position.", example = "2")
            Integer position
    ) {
    }

    @Schema(description = "Payload used to assign or unassign an issue.")
    public record AssignIssueRequest(
            @Schema(description = "Assigned user id from the user microservice. Send null to unassign the issue.")
            UUID assigneeUserId
    ) {
    }

    @Schema(description = "Issue returned by the API.")
    public record IssueResponse(
            UUID id,
            UUID projectId,
            UUID boardId,
            UUID sprintId,
            UUID parentIssueId,
            String title,
            String description,
            IssueType type,
            IssuePriority priority,
            String status,
            StatusCategory statusCategory,
            IssueResolution resolution,
            ClassOfService classOfService,
            String categoryKey,
            String componentKey,
            Set<String> labels,
            UUID assigneeUserId,
            UUID reporterUserId,
            Integer storyPoints,
            LocalDate dueDate,
            Integer position,
            Boolean sprintScopeAdded,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }

    @Schema(description = "Payload used to create an issue comment.")
    public record CreateCommentRequest(
            @NotBlank
            @Size(max = 4000)
            @Schema(description = "Comment body.", example = "This is ready for QA.")
            String body
    ) {
    }

    @Schema(description = "Issue comment returned by the API.")
    public record CommentResponse(
            UUID id,
            UUID issueId,
            UUID authorUserId,
            String body,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    @Schema(description = "Issue attachment metadata returned by the API.")
    public record AttachmentResponse(
            UUID id,
            UUID issueId,
            UUID uploadedByUserId,
            String fileName,
            String contentType,
            Long sizeBytes,
            String storageKey,
            String publicUrl,
            Instant createdAt
    ) {
    }
}
