package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.WorkWeekDtos;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.WorkWeekMeetingEntity;
import com.guido.agiletaskservice.domain.entity.WorkWeekMeetingParticipantEntity;
import com.guido.agiletaskservice.domain.repository.WorkWeekMeetingParticipantRepository;
import com.guido.agiletaskservice.domain.repository.WorkWeekMeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkWeekService {

    private final WorkWeekMeetingRepository meetingRepository;
    private final WorkWeekMeetingParticipantRepository participantRepository;

    @Transactional
    public WorkWeekMeetingEntity create(WorkWeekDtos.CreateMeetingRequest request, UUID createdByUserId) {
        WorkWeekMeetingEntity meeting = new WorkWeekMeetingEntity();
        applyMeetingFields(meeting, request.meetingDate(), request.purpose(), request.description(), createdByUserId);
        WorkWeekMeetingEntity savedMeeting = meetingRepository.save(meeting);
        replaceParticipants(savedMeeting, request.participants(), createdByUserId);
        log.info("Created work-week meeting: meetingId={}, meetingDate={}, participantCount={}",
                savedMeeting.getId(), savedMeeting.getMeetingDate(), request.participants().size());
        return get(savedMeeting.getId());
    }

    @Transactional(readOnly = true)
    public WorkWeekMeetingEntity get(UUID id) {
        return meetingRepository.findById(id)
                .filter(meeting -> !meeting.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Work-week meeting was not found."));
    }

    @Transactional
    public WorkWeekMeetingEntity update(UUID id, WorkWeekDtos.UpdateMeetingRequest request, UUID userId) {
        WorkWeekMeetingEntity meeting = get(id);
        applyMeetingFields(meeting, request.meetingDate(), request.purpose(), request.description(), meeting.getCreatedByUserId());
        WorkWeekMeetingEntity savedMeeting = meetingRepository.save(meeting);
        replaceParticipants(savedMeeting, request.participants(), userId);
        log.info("Updated work-week meeting: meetingId={}, meetingDate={}", id, savedMeeting.getMeetingDate());
        return get(id);
    }

    @Transactional
    public void archive(UUID id, UUID userId) {
        WorkWeekMeetingEntity meeting = get(id);
        meeting.softDelete(userId);
        meetingRepository.save(meeting);
        participantRepository.findByMeetingIdAndDeletedAtIsNullOrderByCreatedAtAsc(id)
                .forEach(participant -> {
                    participant.softDelete(userId);
                    participantRepository.save(participant);
                });
        log.info("Archived work-week meeting: meetingId={}, deletedByUserId={}", id, userId);
    }

    @Transactional(readOnly = true)
    public WorkWeekDtos.WorkWeekResponse getWeek(LocalDate anchorDate) {
        LocalDate anchor = anchorDate == null ? LocalDate.now() : anchorDate;
        LocalDate weekStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkWeekMeetingEntity> meetings = meetingRepository.findByMeetingDateBetweenAndDeletedAtIsNullOrderByMeetingDateAscCreatedAtAsc(weekStart, weekEnd);
        Map<LocalDate, List<WorkWeekMeetingEntity>> byDate = meetings.stream().collect(Collectors.groupingBy(WorkWeekMeetingEntity::getMeetingDate));
        List<WorkWeekDtos.WorkWeekDayResponse> days = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            LocalDate date = weekStart.plusDays(dayIndex);
            List<WorkWeekDtos.MeetingResponse> dayMeetings = byDate.getOrDefault(date, List.of()).stream()
                    .map(this::toResponse)
                    .toList();
            days.add(new WorkWeekDtos.WorkWeekDayResponse(date, date.getDayOfWeek(), dayMeetings));
        }
        return new WorkWeekDtos.WorkWeekResponse(weekStart, weekEnd, days);
    }

    public WorkWeekDtos.MeetingResponse toResponse(WorkWeekMeetingEntity meeting) {
        List<WorkWeekDtos.MeetingParticipantResponse> participants = participantRepository
                .findByMeetingIdAndDeletedAtIsNullOrderByCreatedAtAsc(meeting.getId()).stream()
                .map(participant -> new WorkWeekDtos.MeetingParticipantResponse(
                        participant.getId(),
                        participant.getDisplayName(),
                        participant.getRoleLabel(),
                        participant.getExternalReference()
                ))
                .toList();
        return new WorkWeekDtos.MeetingResponse(
                meeting.getId(),
                meeting.getMeetingDate(),
                meeting.getDayOfWeek(),
                meeting.getPurpose(),
                meeting.getDescription(),
                meeting.getCreatedByUserId(),
                participants,
                meeting.getCreatedAt(),
                meeting.getUpdatedAt(),
                meeting.getDeletedAt()
        );
    }

    private void applyMeetingFields(WorkWeekMeetingEntity meeting, LocalDate meetingDate, String purpose, String description, UUID createdByUserId) {
        meeting.setMeetingDate(meetingDate);
        meeting.setDayOfWeek(meetingDate.getDayOfWeek());
        meeting.setPurpose(purpose.trim());
        meeting.setDescription(description);
        meeting.setCreatedByUserId(createdByUserId);
    }

    private void replaceParticipants(WorkWeekMeetingEntity meeting, List<WorkWeekDtos.MeetingParticipantRequest> participants, UUID userId) {
        participantRepository.findByMeetingIdAndDeletedAtIsNullOrderByCreatedAtAsc(meeting.getId())
                .forEach(participant -> {
                    participant.softDelete(userId);
                    participantRepository.save(participant);
                });
        participants.forEach(request -> {
            WorkWeekMeetingParticipantEntity participant = new WorkWeekMeetingParticipantEntity();
            participant.setMeeting(meeting);
            participant.setDisplayName(request.displayName().trim());
            participant.setRoleLabel(request.roleLabel());
            participant.setExternalReference(request.externalReference());
            participantRepository.save(participant);
        });
    }
}
