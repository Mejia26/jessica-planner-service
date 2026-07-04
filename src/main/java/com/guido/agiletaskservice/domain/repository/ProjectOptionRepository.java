package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.ProjectOptionEntity;
import com.guido.agiletaskservice.domain.enums.ProjectOptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectOptionRepository extends JpaRepository<ProjectOptionEntity, UUID> {

    List<ProjectOptionEntity> findByProjectIdAndTypeAndDeletedAtIsNullOrderByPositionAscNameAsc(UUID projectId, ProjectOptionType type);

    List<ProjectOptionEntity> findByProjectIdAndDeletedAtIsNullOrderByTypeAscPositionAscNameAsc(UUID projectId);

    Optional<ProjectOptionEntity> findByProjectIdAndTypeAndKeyIgnoreCaseAndDeletedAtIsNull(UUID projectId, ProjectOptionType type, String key);

    boolean existsByProjectIdAndTypeAndKeyIgnoreCaseAndDeletedAtIsNull(UUID projectId, ProjectOptionType type, String key);
}
