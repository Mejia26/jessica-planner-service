package com.guido.agiletaskservice.api.dto;

import com.guido.agiletaskservice.domain.enums.BoardType;
import com.guido.agiletaskservice.domain.enums.StatusCategory;
import com.guido.agiletaskservice.domain.enums.WorkflowMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BoardDtos {

    private BoardDtos() {
    }

    @Schema(description = "Payload used to create a Scrum or Kanban board.")
    public record CreateBoardRequest(
            @NotNull
            @Schema(description = "Project that owns the board.")
            UUID projectId,

            @NotBlank
            @Size(max = 160)
            @Schema(description = "Board name.", example = "Platform Scrum Board")
            String name,

            @NotNull
            @Schema(description = "Agile methodology supported by the board.", example = "SCRUM")
            BoardType type,

            @Schema(description = "Workflow policy. OPEN allows any transition. STRICT only allows moving to the same, previous, or next column.", example = "OPEN")
            WorkflowMode workflowMode,

            @NotEmpty
            @Valid
            @Schema(description = "Initial workflow columns for the board.")
            List<CreateBoardColumnRequest> columns
    ) {
    }

    @Schema(description = "Payload used to create a board column.")
    public record CreateBoardColumnRequest(
            @NotBlank
            @Size(max = 120)
            @Schema(description = "Column display name.", example = "In Progress")
            String name,

            @NotBlank
            @Size(max = 60)
            @Schema(description = "Stable status key stored in issues.", example = "IN_PROGRESS")
            String statusKey,

            @Schema(description = "Systemic status category used by Scrum/Kanban calculations. If omitted, the backend infers a category from the status key.", example = "IN_PROGRESS")
            StatusCategory statusCategory,

            @NotNull
            @Min(0)
            @Schema(description = "Column order from left to right.", example = "1")
            Integer position,

            @Min(1)
            @Max(999)
            @Schema(description = "Optional work-in-progress limit for Kanban policies.", example = "5")
            Integer wipLimit
    ) {
    }

    @Schema(description = "Payload used to update mutable board fields.")
    public record UpdateBoardRequest(
            @NotBlank
            @Size(max = 160)
            @Schema(description = "Board display name.", example = "Platform Delivery Board")
            String name
    ) {
    }

    @Schema(description = "Payload used to update a board column without recreating the board.")
    public record UpdateBoardColumnRequest(
            @NotBlank
            @Size(max = 120)
            @Schema(description = "Column display name.", example = "Testing")
            String name,

            @NotBlank
            @Size(max = 60)
            @Schema(description = "Stable status key stored in issues. If changed, existing issues are migrated to the new key.", example = "TESTING")
            String statusKey,

            @Schema(description = "Systemic status category used for Done/velocity/WIP semantics. If omitted, the backend infers a category from the status key.", example = "IN_PROGRESS")
            StatusCategory statusCategory,

            @NotNull
            @Min(0)
            @Schema(description = "Column order from left to right.", example = "2")
            Integer position,

            @Min(1)
            @Max(999)
            @Schema(description = "Optional work-in-progress limit.", example = "4")
            Integer wipLimit
    ) {
    }

    @Schema(description = "Column order item used to reorder a board workflow.")
    public record ReorderBoardColumnItem(
            @NotNull
            @Schema(description = "Board column id.")
            UUID id,

            @NotNull
            @Min(0)
            @Schema(description = "New zero-based position.", example = "1")
            Integer position
    ) {
    }

    @Schema(description = "Payload used to reorder board columns.")
    public record ReorderBoardColumnsRequest(
            @NotEmpty
            @Valid
            List<ReorderBoardColumnItem> columns
    ) {
    }

    @Schema(description = "Payload used to archive a board column. Issues can be moved to another status before the column is archived.")
    public record ArchiveBoardColumnRequest(
            @Schema(description = "Replacement status key. Required when the column still has issues.", example = "DONE")
            String replacementStatusKey
    ) {
    }

    @Schema(description = "Board details including workflow columns.")
    public record BoardResponse(
            UUID id,
            UUID projectId,
            String name,
            BoardType type,
            WorkflowMode workflowMode,
            List<BoardColumnResponse> columns,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }

    @Schema(description = "Board workflow column.")
    public record BoardColumnResponse(
            UUID id,
            String name,
            String statusKey,
            StatusCategory statusCategory,
            Integer position,
            Integer wipLimit,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }
}
