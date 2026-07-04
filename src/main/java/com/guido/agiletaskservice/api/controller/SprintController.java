package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.SprintDtos;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.application.service.SprintService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sprints")
@Tag(name = "Sprints", description = "Scrum sprint planning and lifecycle APIs.")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create sprint", description = "Creates a planned sprint for a project.")
    @ApiResponse(responseCode = "201", description = "Sprint created.")
    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public SprintDtos.SprintResponse create(@Valid @RequestBody SprintDtos.CreateSprintRequest request) {
        return ApiMapper.toSprintResponse(sprintService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by id", description = "Returns sprint details and lifecycle status.")
    public SprintDtos.SprintResponse get(@PathVariable UUID id) {
        return ApiMapper.toSprintResponse(sprintService.get(id));
    }

    @GetMapping
    @Operation(summary = "List sprints by project", description = "Returns sprints ordered by creation date, newest first.")
    public List<SprintDtos.SprintResponse> listByProject(
            @RequestParam @Parameter(description = "Project id used to filter sprints.", required = true) UUID projectId
    ) {
        return sprintService.listByProject(projectId).stream()
                .map(ApiMapper::toSprintResponse)
                .toList();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update sprint", description = "Updates a planned or active sprint.")
    public SprintDtos.SprintResponse update(@PathVariable UUID id, @Valid @RequestBody SprintDtos.UpdateSprintRequest request) {
        return ApiMapper.toSprintResponse(sprintService.update(id, request));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start sprint", description = "Starts a planned sprint. A project can only have one active sprint.")
    public SprintDtos.SprintResponse start(@PathVariable UUID id) {
        return ApiMapper.toSprintResponse(sprintService.start(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Complete sprint",
            description = "Completes an active sprint. Truly done issues stay in the closed sprint only when their status category is DONE and a resolution is set. Unfinished work moves to backlog by default or to a selected planned future sprint."
    )
    public SprintDtos.SprintResponse complete(
            @PathVariable UUID id,
            @RequestBody(required = false) SprintDtos.CompleteSprintRequest request
    ) {
        return ApiMapper.toSprintResponse(sprintService.complete(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive sprint", description = "Soft-deletes a non-active sprint while preserving sprint history.")
    public void archive(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        sprintService.archive(id, userId);
    }
}
