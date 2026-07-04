package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.WorkWeekDtos;
import com.guido.agiletaskservice.application.service.WorkWeekService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import com.guido.agiletaskservice.common.web.UserContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/work-week")
@Tag(name = "Work Week", description = "Weekly meeting planning APIs for team coordination charts.")
public class WorkWeekController {

    private final WorkWeekService workWeekService;

    @GetMapping
    @Operation(
            summary = "Get work-week chart data",
            description = "Returns a Monday-to-Sunday structure grouped by date. The UI can render this directly as a weekly chart, timeline, or calendar."
    )
    public WorkWeekDtos.WorkWeekResponse getWeek(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Any date inside the desired week. Defaults to today.", example = "2026-07-06")
            LocalDate anchorDate
    ) {
        return workWeekService.getWeek(anchorDate);
    }

    @PostMapping("/meetings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create work-week meeting",
            description = "Creates a meeting item for a specific date. Participants are free-form names and can include people outside the system.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = """
                    {
                      "meetingDate": "2026-07-06",
                      "purpose": "Daily stand-up",
                      "description": "Review blockers and deployment readiness.",
                      "participants": [
                        {"displayName": "Harriet White", "roleLabel": "Lead"},
                        {"displayName": "External Camera Crew", "roleLabel": "Vendor", "externalReference": "studio-crew"}
                      ]
                    }
                    """)))
    )
    @ApiResponse(responseCode = "201", description = "Meeting created.")
    @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public WorkWeekDtos.MeetingResponse create(
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId,
            @Valid @RequestBody WorkWeekDtos.CreateMeetingRequest request
    ) {
        return workWeekService.toResponse(workWeekService.create(request, userId));
    }

    @GetMapping("/meetings/{id}")
    @Operation(summary = "Get work-week meeting", description = "Returns a meeting item and its participants.")
    public WorkWeekDtos.MeetingResponse get(@PathVariable UUID id) {
        return workWeekService.toResponse(workWeekService.get(id));
    }

    @PatchMapping("/meetings/{id}")
    @Operation(summary = "Update work-week meeting", description = "Updates meeting date, purpose, description, and participant list.")
    public WorkWeekDtos.MeetingResponse update(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @Valid @RequestBody WorkWeekDtos.UpdateMeetingRequest request
    ) {
        return workWeekService.toResponse(workWeekService.update(id, request, userId));
    }

    @DeleteMapping("/meetings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive work-week meeting", description = "Soft-deletes a work-week meeting and its participants.")
    public void archive(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        workWeekService.archive(id, userId);
    }
}
