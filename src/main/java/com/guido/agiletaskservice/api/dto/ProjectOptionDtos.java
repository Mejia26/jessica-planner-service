package com.guido.agiletaskservice.api.dto;

import com.guido.agiletaskservice.domain.enums.ProjectOptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ProjectOptionDtos {

    private ProjectOptionDtos() {
    }

    @Schema(description = "Payload used to create a user-configurable project option such as a label, category, or component.")
    public record CreateProjectOptionRequest(
            @NotNull
            @Schema(description = "Configurable option family.", example = "ISSUE_CATEGORY")
            ProjectOptionType type,

            @NotBlank
            @Size(max = 80)
            @Pattern(regexp = "^[A-Z0-9][A-Z0-9_\\-]{0,79}$")
            @Schema(description = "Stable key stored on issues. Use uppercase keys for clean filtering.", example = "BACKEND")
            String key,

            @NotBlank
            @Size(max = 120)
            @Schema(description = "Human-readable option name.", example = "Backend")
            String name,

            @Size(max = 500)
            @Schema(description = "Optional explanation shown by the UI.", example = "Work owned by backend developers.")
            String description,

            @Size(max = 32)
            @Schema(description = "Optional UI color token or hex value.", example = "#2E78D2")
            String color,

            @Min(0)
            @Schema(description = "Sort position in UI selectors.", example = "0")
            Integer position
    ) {
    }

    @Schema(description = "Payload used to update a user-configurable project option.")
    public record UpdateProjectOptionRequest(
            @NotBlank
            @Size(max = 80)
            @Pattern(regexp = "^[A-Z0-9][A-Z0-9_\\-]{0,79}$")
            @Schema(description = "Stable key stored on issues.", example = "API")
            String key,

            @NotBlank
            @Size(max = 120)
            @Schema(description = "Human-readable option name.", example = "API")
            String name,

            @Size(max = 500)
            @Schema(description = "Optional explanation shown by the UI.")
            String description,

            @Size(max = 32)
            @Schema(description = "Optional UI color token or hex value.", example = "#7AA116")
            String color,

            @NotNull
            @Min(0)
            @Schema(description = "Sort position in UI selectors.", example = "1")
            Integer position
    ) {
    }

    @Schema(description = "User-configurable project option returned by the API.")
    public record ProjectOptionResponse(
            UUID id,
            UUID projectId,
            ProjectOptionType type,
            String key,
            String name,
            String description,
            String color,
            Integer position,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }
}
