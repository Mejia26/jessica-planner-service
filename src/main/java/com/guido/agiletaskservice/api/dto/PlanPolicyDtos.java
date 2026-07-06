package com.guido.agiletaskservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class PlanPolicyDtos {

    private PlanPolicyDtos() {
    }

    @Schema(description = "Action selection used to build a SaaS plan YAML file.")
    public record PlanActionSelectionRequest(
            @NotNull
            @Schema(description = "Action id from the actions table.", example = "08fae65f-ec0e-43f6-8068-b60a0c8f2508")
            UUID id,

            @NotNull
            @Schema(description = "When true, the action is included in the generated plan. Disabled actions are omitted.", example = "true")
            Boolean enabled
    ) {
    }

    @Schema(description = "Plan definition used to generate the YAML consumed by the SaaS plan loader.")
    public record PlanDefinitionRequest(
            @Schema(description = "Plan id. When omitted, the backend generates a new UUID.", example = "770e8400-e29b-41d4-a716-446655440201")
            UUID planId,

            @NotBlank
            @Size(max = 160)
            @Schema(description = "Human-readable SaaS plan name.", example = "Starter Plan")
            String name,

            @NotEmpty
            @Valid
            @Schema(description = "Actions available for this plan. Only enabled actions are written to the YAML file.")
            List<PlanActionSelectionRequest> actions
    ) {
    }

    @Schema(description = "Request used to generate a plans YAML file.")
    public record GeneratePlansYamlRequest(
            @NotEmpty
            @Valid
            @Schema(description = "Plans to include in the generated YAML file.")
            List<PlanDefinitionRequest> plans
    ) {
    }
}
