package com.guido.agiletaskservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class WorkWeekDtos {

    private WorkWeekDtos() {
    }

    @Schema(description = "Participant for a work-week meeting. This is plain user-provided text and does not need to exist in user-management.")
    public record MeetingParticipantRequest(
            @NotBlank
            @Size(max = 160)
            @Schema(description = "Person or group display name.", example = "Jessica QA Team")
            String displayName,

            @Size(max = 120)
            @Schema(description = "Optional role or responsibility label.", example = "QA")
            String roleLabel,

            @Size(max = 160)
            @Schema(description = "Optional external reference, email, vendor id, or free-form pointer.", example = "qa-vendor-01")
            String externalReference
    ) {
    }

    @Schema(description = "Payload used to create a work-week meeting item.")
    public record CreateMeetingRequest(
            @NotNull
            @Schema(description = "Specific meeting date. The backend derives dayOfWeek from this date.", example = "2026-07-06")
            LocalDate meetingDate,

            @NotBlank
            @Size(max = 220)
            @Schema(description = "Short reason for the meeting.", example = "Daily stand-up and release risk review")
            String purpose,

            @Size(max = 2000)
            @Schema(description = "Optional detailed description for the agenda.", example = "Review blockers, QA evidence, and release readiness.")
            String description,

            @NotEmpty
            @Valid
            @Schema(description = "People or groups expected to attend.")
            List<MeetingParticipantRequest> participants
    ) {
    }

    @Schema(description = "Payload used to update a work-week meeting item.")
    public record UpdateMeetingRequest(
            @NotNull LocalDate meetingDate,
            @NotBlank @Size(max = 220) String purpose,
            @Size(max = 2000) String description,
            @NotEmpty @Valid List<MeetingParticipantRequest> participants
    ) {
    }

    @Schema(description = "Participant returned by the work-week API.")
    public record MeetingParticipantResponse(
            UUID id,
            String displayName,
            String roleLabel,
            String externalReference
    ) {
    }

    @Schema(description = "Meeting item returned by the work-week API.")
    public record MeetingResponse(
            UUID id,
            LocalDate meetingDate,
            DayOfWeek dayOfWeek,
            String purpose,
            String description,
            UUID createdByUserId,
            List<MeetingParticipantResponse> participants,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
    }

    @Schema(description = "One day in the work-week board structure consumed by the UI.")
    public record WorkWeekDayResponse(
            LocalDate date,
            DayOfWeek dayOfWeek,
            List<MeetingResponse> meetings
    ) {
    }

    @Schema(description = "Grouped work-week structure. The UI can render this as a weekly chart, timeline, or calendar without regrouping records.")
    public record WorkWeekResponse(
            LocalDate weekStart,
            LocalDate weekEnd,
            List<WorkWeekDayResponse> days
    ) {
    }
}
