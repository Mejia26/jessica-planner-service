package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.CalendarCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarCategoryRepository extends JpaRepository<CalendarCategoryEntity, UUID> {

    List<CalendarCategoryEntity> findByOwnerUserIdAndDeletedAtIsNullOrderByPositionAscNameAsc(UUID ownerUserId);

    Optional<CalendarCategoryEntity> findByOwnerUserIdAndKeyIgnoreCaseAndDeletedAtIsNull(UUID ownerUserId, String key);

    boolean existsByOwnerUserIdAndKeyIgnoreCaseAndDeletedAtIsNull(UUID ownerUserId, String key);
}
