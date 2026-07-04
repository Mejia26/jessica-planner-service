package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.IssueAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueAttachmentRepository extends JpaRepository<IssueAttachmentEntity, UUID> {

    List<IssueAttachmentEntity> findByIssueIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID issueId);
}
