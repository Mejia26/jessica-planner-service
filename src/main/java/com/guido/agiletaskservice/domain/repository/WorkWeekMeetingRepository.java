package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.WorkWeekMeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WorkWeekMeetingRepository extends JpaRepository<WorkWeekMeetingEntity, UUID> {

    List<WorkWeekMeetingEntity> findByMeetingDateBetweenAndDeletedAtIsNullOrderByMeetingDateAscCreatedAtAsc(LocalDate startDate, LocalDate endDate);
}
