package com.guido.agiletaskservice.api.dto;

import com.guido.agiletaskservice.domain.enums.CalendarEventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class CalendarDtos {

    private CalendarDtos() {
    }

    @Schema(description = "Payload used to create a personal calendar event or note.")
    public record CreateCalendarEventRequest(
            @NotBlank
            @Size(max = 220)
            @Schema(description = "Event title.", example = "QA planning meeting")
            String title,

            @Size(max = 5000)
            @Schema(description = "Personal notes for the event.", example = "Review board blockers and release risks.")
            String notes,

            @Size(max = 240)
            @Schema(description = "Optional location or meeting link.", example = "Google Meet")
            String location,

            @NotNull
            @Schema(description = "Start instant in UTC.", example = "2026-07-06T14:00:00Z")
            Instant startAt,

            @Schema(description = "Optional end instant in UTC.", example = "2026-07-06T15:00:00Z")
            Instant endAt,

            @Schema(description = "Whether this item represents a full-day note.", example = "false")
            Boolean allDay,

            @Size(max = 80)
            @Schema(description = "IANA timezone used by the user when creating the item.", example = "America/Santo_Domingo")
            String timeZone,

            @Size(max = 80)
            @Schema(description = "Optional user-defined category key. Missing categories are created automatically.", example = "MEETING")
            String categoryKey,

            @Size(max = 32)
            @Schema(description = "Optional display color for this event.", example = "#579DFF")
            String color,

            @Schema(description = "Calendar event status.", example = "PLANNED")
            CalendarEventStatus status,

            @Schema(description = "Optional reminder instant in UTC.", example = "2026-07-06T13:45:00Z")
            Instant reminderAt
    ) {
    }

    @Schema(description = "Payload used to update a personal calendar event or note.")
    public record UpdateCalendarEventRequest(
            @NotBlank
            @Size(max = 220)
            String title,

            @Size(max = 5000)
            String notes,

            @Size(max = 240)
            String location,

            @NotNull
            Instant startAt,

            Instant endAt,

            Boolean allDay,

            @Size(max = 80)
            String timeZone,

            @Size(max = 80)
            String categoryKey,

            @Size(max = 32)
            String color,

            CalendarEventStatus status,

            Instant reminderAt
    ) {
    }

    @Schema(description = "Personal calendar event returned by the API.")
    public record CalendarEventResponse(
            UUID id,
            UUID ownerUserId,
            String title,
            String notes,
            String location,
            Instant startAt,
            Instant endAt,
            boolean allDay,
            String timeZone,
            String categoryKey,
            String color,
            CalendarEventStatus status,
            Instant reminderAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }

    @Schema(description = "Payload used to create a personal calendar category.")
    public record CreateCalendarCategoryRequest(
            @NotBlank
            @Size(max = 80)
            @Schema(description = "Stable key used by the UI for filtering.", example = "MEETING")
            String key,

            @NotBlank
            @Size(max = 120)
            @Schema(description = "Display name.", example = "Meeting")
            String name,

            @Size(max = 500)
            String description,

            @Size(max = 32)
            String color,

            @Min(0)
            Integer position
    ) {
    }

    @Schema(description = "Payload used to update a personal calendar category.")
    public record UpdateCalendarCategoryRequest(
            @NotBlank
            @Size(max = 80)
            String key,

            @NotBlank
            @Size(max = 120)
            String name,

            @Size(max = 500)
            String description,

            @Size(max = 32)
            String color,

            @Min(0)
            Integer position
    ) {
    }

    @Schema(description = "Personal calendar category returned by the API.")
    public record CalendarCategoryResponse(
            UUID id,
            UUID ownerUserId,
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
