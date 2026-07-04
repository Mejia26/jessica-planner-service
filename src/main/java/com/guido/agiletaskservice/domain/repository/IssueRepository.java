package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.IssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<IssueEntity, UUID>, JpaSpecificationExecutor<IssueEntity> {

    List<IssueEntity> findBySprintIdOrderByPositionAsc(UUID sprintId);

    List<IssueEntity> findBySprintIdAndDeletedAtIsNullOrderByPositionAsc(UUID sprintId);

    List<IssueEntity> findByBoardIdAndStatusIgnoreCaseAndDeletedAtIsNullOrderByPositionAsc(UUID boardId, String status);

    long countByBoardIdAndStatusIgnoreCaseAndDeletedAtIsNull(UUID boardId, String status);

    long countByBoardIdAndStatusIgnoreCaseAndClassOfServiceNotAndDeletedAtIsNull(UUID boardId, String status, com.guido.agiletaskservice.domain.enums.ClassOfService classOfService);
}
