package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.IssueCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueCommentRepository extends JpaRepository<IssueCommentEntity, UUID> {

    List<IssueCommentEntity> findByIssueIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID issueId);
}
