package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.WorkWeekMeetingParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkWeekMeetingParticipantRepository extends JpaRepository<WorkWeekMeetingParticipantEntity, UUID> {

    List<WorkWeekMeetingParticipantEntity> findByMeetingIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID meetingId);
}
