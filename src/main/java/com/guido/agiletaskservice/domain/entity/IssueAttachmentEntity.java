package com.guido.agiletaskservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "issue_attachments", indexes = {
        @Index(name = "idx_issue_attachments_issue_id", columnList = "issue_id")
})
public class IssueAttachmentEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "storage_key", nullable = false, length = 700)
    private String storageKey;

    @Column(name = "public_url", length = 1000)
    private String publicUrl;
}
