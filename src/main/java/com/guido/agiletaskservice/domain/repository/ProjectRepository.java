package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID>, JpaSpecificationExecutor<ProjectEntity> {

    boolean existsByKeyIgnoreCase(String key);

    Optional<ProjectEntity> findByKeyIgnoreCase(String key);
}
