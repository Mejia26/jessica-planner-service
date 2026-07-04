package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.CalendarDtos;
import com.guido.agiletaskservice.application.service.CalendarService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import com.guido.agiletaskservice.common.web.UserContextResolver;
import com.guido.agiletaskservice.domain.enums.CalendarEventStatus;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
@Tag(name = "Personal Calendar", description = "Personal calendar APIs for user-owned notes, meetings, reminders, and categories.")
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create personal calendar event", description = "Creates a calendar note or meeting owned only by the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Calendar event created.")
    @ApiResponse(responseCode = "400", description = "Invalid calendar event.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public CalendarDtos.CalendarEventResponse createEvent(
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId,
            @Valid @RequestBody CalendarDtos.CreateCalendarEventRequest request
    ) {
        return calendarService.createEvent(userId, request);
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Get personal calendar event", description = "Returns a calendar event only when it belongs to the authenticated user.")
    public CalendarDtos.CalendarEventResponse getEvent(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @PathVariable UUID eventId
    ) {
        return calendarService.getEvent(userId, eventId);
    }

    @GetMapping("/events")
    @Operation(summary = "Search personal calendar events", description = "Returns paginated user-owned calendar events filtered by date range, category, status, or text.")
    public Page<CalendarDtos.CalendarEventResponse> searchEvents(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String categoryKey,
            @RequestParam(required = false) CalendarEventStatus status,
            @RequestParam(required = false) String text,
            @PageableDefault(size = 50, sort = "startAt") Pageable pageable
    ) {
        return calendarService.searchEvents(userId, from, to, categoryKey, status, text, pageable);
    }

    @GetMapping("/dates/{date}/events")
    @Operation(summary = "List calendar events for a date", description = "Returns all user-owned calendar events that overlap the selected local date.")
    public List<CalendarDtos.CalendarEventResponse> listEventsForDate(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "UTC") String timeZone
    ) {
        return calendarService.listEventsForDate(userId, date, timeZone);
    }

    @PatchMapping("/events/{eventId}")
    @Operation(summary = "Update personal calendar event", description = "Updates an event owned by the authenticated user.")
    public CalendarDtos.CalendarEventResponse updateEvent(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody CalendarDtos.UpdateCalendarEventRequest request
    ) {
        return calendarService.updateEvent(userId, eventId, request);
    }

    @DeleteMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive personal calendar event", description = "Soft-deletes an event owned by the authenticated user.")
    public void archiveEvent(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @PathVariable UUID eventId
    ) {
        calendarService.archiveEvent(userId, eventId);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create personal calendar category", description = "Creates a user-owned category used to organize personal calendar events.")
    public CalendarDtos.CalendarCategoryResponse createCategory(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @Valid @RequestBody CalendarDtos.CreateCalendarCategoryRequest request
    ) {
        return calendarService.createCategory(userId, request);
    }

    @GetMapping("/categories")
    @Operation(summary = "List personal calendar categories", description = "Returns active user-owned categories for calendar filters and selectors.")
    public List<CalendarDtos.CalendarCategoryResponse> listCategories(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId
    ) {
        return calendarService.listCategories(userId);
    }

    @PatchMapping("/categories/{categoryId}")
    @Operation(summary = "Update personal calendar category", description = "Updates a user-owned calendar category.")
    public CalendarDtos.CalendarCategoryResponse updateCategory(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CalendarDtos.UpdateCalendarCategoryRequest request
    ) {
        return calendarService.updateCategory(userId, categoryId, request);
    }

    @DeleteMapping("/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive personal calendar category", description = "Soft-deletes a user-owned calendar category.")
    public void archiveCategory(
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @PathVariable UUID categoryId
    ) {
        calendarService.archiveCategory(userId, categoryId);
    }
}
