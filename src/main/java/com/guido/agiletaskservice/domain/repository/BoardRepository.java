package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<BoardEntity, UUID> {

    List<BoardEntity> findByProjectIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID projectId);
}
