package com.guido.agiletaskservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    @Schema(description = "Payload used to create an agile project.")
    public record CreateProjectRequest(
            @NotBlank
            @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$")
            @Schema(description = "Short uppercase project key used by clients to identify the project.", example = "OPS")
            String key,

            @NotBlank
            @Size(max = 160)
            @Schema(description = "Human-readable project name.", example = "Operations Platform")
            String name,

            @Size(max = 2000)
            @Schema(description = "Optional project description.", example = "Internal platform work managed with Scrum and Kanban.")
            String description
    ) {
    }

    @Schema(description = "Payload used to update mutable project fields.")
    public record UpdateProjectRequest(
            @NotBlank
            @Size(max = 160)
            @Schema(description = "Human-readable project name.", example = "Operations Platform")
            String name,

            @Size(max = 2000)
            @Schema(description = "Optional project description.", example = "Updated project description.")
            String description
    ) {
    }

    @Schema(description = "Project returned by the API.")
    public record ProjectResponse(
            @Schema(description = "Project identifier.", example = "4f3ad43e-0df6-4125-8704-6d7a2d17c5a8")
            UUID id,

            @Schema(description = "Short uppercase project key.", example = "OPS")
            String key,

            @Schema(description = "Project name.", example = "Operations Platform")
            String name,

            @Schema(description = "Project description.")
            String description,

            @Schema(description = "User id that created the project.")
            UUID createdByUserId,

            @Schema(description = "Creation timestamp.")
            Instant createdAt,

            @Schema(description = "Last update timestamp.")
            Instant updatedAt,

            @Schema(description = "Soft-delete timestamp. Null means the project is active.")
            Instant deletedAt
    ) {
    }
}
