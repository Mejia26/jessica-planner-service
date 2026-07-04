package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.ProjectDtos;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.application.service.ProjectService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import com.guido.agiletaskservice.common.web.UserContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project APIs used to group boards, sprints, and issues.")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create project", description = "Creates an agile project owned by the user supplied by the gateway in X-User-Id.")
    @ApiResponse(responseCode = "201", description = "Project created.")
    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Project key already exists.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ProjectDtos.ProjectResponse create(
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId,
            @Valid @RequestBody ProjectDtos.CreateProjectRequest request
    ) {
        return ApiMapper.toProjectResponse(projectService.create(request, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by id", description = "Returns a project with audit metadata.")
    @ApiResponse(responseCode = "200", description = "Project found.")
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ProjectDtos.ProjectResponse get(@PathVariable UUID id) {
        return ApiMapper.toProjectResponse(projectService.get(id));
    }

    @GetMapping
    @Operation(summary = "Search projects", description = "Returns paginated projects. Use text to search by key, name, or description.")
    public Page<ProjectDtos.ProjectResponse> search(
            @RequestParam(required = false) @Parameter(description = "Optional text filter.") String text,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return projectService.search(text, pageable).map(ApiMapper::toProjectResponse);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update project", description = "Updates mutable project fields.")
    public ProjectDtos.ProjectResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDtos.UpdateProjectRequest request
    ) {
        return ApiMapper.toProjectResponse(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive project", description = "Soft-deletes a project so it no longer appears in normal UI searches while preserving history.")
    public void archive(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        projectService.archive(id, userId);
    }
}
