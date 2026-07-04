package com.guido.agiletaskservice.api.dto;

import com.guido.agiletaskservice.domain.enums.SprintCompletionDestination;
import com.guido.agiletaskservice.domain.enums.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class SprintDtos {

    private SprintDtos() {
    }

    @Schema(description = "Payload used to create a sprint backlog container.")
    public record CreateSprintRequest(
            @NotNull
            @Schema(description = "Project that owns the sprint.")
            UUID projectId,

            @NotBlank
            @Size(max = 160)
            @Schema(description = "Sprint name.", example = "Sprint 2026.07.1")
            String name,

            @Size(max = 2000)
            @Schema(description = "Sprint goal.", example = "Ship the first issue management workflow.")
            String goal,

            @Schema(description = "Planned start date.", example = "2026-07-01")
            LocalDate startDate,

            @Schema(description = "Planned end date.", example = "2026-07-14")
            LocalDate endDate
    ) {
    }

    @Schema(description = "Payload used to update sprint planning fields.")
    public record UpdateSprintRequest(
            @NotBlank
            @Size(max = 160)
            String name,

            @Size(max = 2000)
            String goal,

            LocalDate startDate,

            LocalDate endDate
    ) {
    }

    @Schema(description = "Payload used to complete a sprint and decide where unfinished work should go.")
    public record CompleteSprintRequest(
            @NotNull
            @Schema(description = "Where unfinished non-Done issues should be moved when the sprint closes.", example = "BACKLOG")
            SprintCompletionDestination destination,

            @Schema(description = "Required when destination is FUTURE_SPRINT. The future sprint must belong to the same project and be PLANNED.")
            UUID targetSprintId
    ) {
    }

    @Schema(description = "Sprint returned by the API.")
    public record SprintResponse(
            UUID id,
            UUID projectId,
            String name,
            String goal,
            SprintStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant startedAt,
            Instant completedAt,
            Integer commitmentBaselinePoints,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }
}
