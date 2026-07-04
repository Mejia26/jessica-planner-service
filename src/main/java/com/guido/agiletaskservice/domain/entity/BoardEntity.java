package com.guido.agiletaskservice.domain.entity;

import com.guido.agiletaskservice.domain.enums.BoardType;
import com.guido.agiletaskservice.domain.enums.WorkflowMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "boards", indexes = {
        @Index(name = "idx_boards_project_id", columnList = "project_id"),
        @Index(name = "idx_boards_type", columnList = "type")
})
public class BoardEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_mode", nullable = false, length = 20)
    private WorkflowMode workflowMode = WorkflowMode.OPEN;
}
