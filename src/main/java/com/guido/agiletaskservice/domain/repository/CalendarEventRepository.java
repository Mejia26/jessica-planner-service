package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, UUID>, JpaSpecificationExecutor<CalendarEventEntity> {

    List<CalendarEventEntity> findByOwnerUserIdAndDeletedAtIsNullOrderByStartAtAsc(UUID ownerUserId);
}
