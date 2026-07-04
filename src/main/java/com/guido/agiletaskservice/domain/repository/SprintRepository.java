package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.SprintEntity;
import com.guido.agiletaskservice.domain.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<SprintEntity, UUID> {

    List<SprintEntity> findByProjectIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID projectId);

    Optional<SprintEntity> findFirstByProjectIdAndStatusAndDeletedAtIsNull(UUID projectId, SprintStatus status);
}
