package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.ProjectOptionDtos;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.application.service.ProjectOptionService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import com.guido.agiletaskservice.common.web.UserContextResolver;
import com.guido.agiletaskservice.domain.enums.ProjectOptionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/options")
@Tag(name = "Project Options", description = "User-configurable option catalogs for labels, components, and categories.")
public class ProjectOptionController {

    private final ProjectOptionService projectOptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create project option",
            description = "Creates a configurable value that the UI can use for issue categorization, filtering, and selectors."
    )
    @ApiResponse(responseCode = "201", description = "Project option created.")
    @ApiResponse(responseCode = "400", description = "Invalid option or duplicated key.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ProjectOptionDtos.ProjectOptionResponse create(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectOptionDtos.CreateProjectOptionRequest request
    ) {
        return ApiMapper.toProjectOptionResponse(projectOptionService.create(projectId, request));
    }

    @GetMapping
    @Operation(
            summary = "List project options",
            description = "Returns active configurable options for a project. Filter by type to populate a specific UI selector."
    )
    public List<ProjectOptionDtos.ProjectOptionResponse> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false)
            @Parameter(description = "Optional option type filter.", example = "ISSUE_CATEGORY")
            ProjectOptionType type
    ) {
        return projectOptionService.list(projectId, type).stream()
                .map(ApiMapper::toProjectOptionResponse)
                .toList();
    }

    @PatchMapping("/{optionId}")
    @Operation(summary = "Update project option", description = "Updates a configurable project option without affecting unrelated options.")
    public ProjectOptionDtos.ProjectOptionResponse update(
            @PathVariable UUID projectId,
            @PathVariable UUID optionId,
            @Valid @RequestBody ProjectOptionDtos.UpdateProjectOptionRequest request
    ) {
        return ApiMapper.toProjectOptionResponse(projectOptionService.update(projectId, optionId, request));
    }

    @DeleteMapping("/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Archive project option",
            description = "Soft-deletes a configurable option. Existing issues keep their stored key for audit/history."
    )
    public void archive(
            @PathVariable UUID projectId,
            @PathVariable UUID optionId,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        projectOptionService.archive(projectId, optionId, userId);
    }
}
