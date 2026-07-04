package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.BoardColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardColumnRepository extends JpaRepository<BoardColumnEntity, UUID> {

    List<BoardColumnEntity> findByBoardIdAndDeletedAtIsNullOrderByPositionAsc(UUID boardId);

    boolean existsByBoardIdAndStatusKeyIgnoreCaseAndDeletedAtIsNull(UUID boardId, String statusKey);

    Optional<BoardColumnEntity> findByBoardIdAndStatusKeyIgnoreCaseAndDeletedAtIsNull(UUID boardId, String statusKey);

    long countByBoardIdAndDeletedAtIsNull(UUID boardId);
}
