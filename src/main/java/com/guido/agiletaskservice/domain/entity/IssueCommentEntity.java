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
@Table(name = "issue_comments", indexes = {
        @Index(name = "idx_issue_comments_issue_id", columnList = "issue_id")
})
public class IssueCommentEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private IssueEntity issue;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(nullable = false, length = 4000)
    private String body;
}
