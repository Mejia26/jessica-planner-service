package com.guido.agiletaskservice.domain.entity;

import com.guido.agiletaskservice.domain.enums.ClassOfService;
import com.guido.agiletaskservice.domain.enums.IssuePriority;
import com.guido.agiletaskservice.domain.enums.IssueResolution;
import com.guido.agiletaskservice.domain.enums.IssueType;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "issues", indexes = {
        @Index(name = "idx_issues_project_id", columnList = "project_id"),
        @Index(name = "idx_issues_board_id", columnList = "board_id"),
        @Index(name = "idx_issues_sprint_id", columnList = "sprint_id"),
        @Index(name = "idx_issues_status", columnList = "status"),
        @Index(name = "idx_issues_assignee", columnList = "assignee_user_id"),
        @Index(name = "idx_issues_reporter", columnList = "reporter_user_id")
})
public class IssueEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private SprintEntity sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_issue_id")
    private IssueEntity parentIssue;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 8000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssuePriority priority;

    @Column(nullable = false, length = 60)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IssueResolution resolution;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_of_service", nullable = false, length = 30)
    private ClassOfService classOfService = ClassOfService.STANDARD;

    @Column(name = "category_key", length = 80)
    private String categoryKey;

    @Column(name = "component_key", length = 80)
    private String componentKey;

    @Column(name = "assignee_user_id")
    private UUID assigneeUserId;

    @Column(name = "reporter_user_id", nullable = false)
    private UUID reporterUserId;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "position_index", nullable = false)
    private Integer position = 0;

    @Column(name = "sprint_scope_added", nullable = false)
    private Boolean sprintScopeAdded = false;

    @ElementCollection
    @CollectionTable(name = "issue_labels", joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "label_key", nullable = false, length = 80)
    private Set<String> labels = new LinkedHashSet<>();
}
