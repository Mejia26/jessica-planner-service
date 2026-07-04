package com.guido.agiletaskservice.domain.entity;

import com.guido.agiletaskservice.domain.enums.StatusCategory;
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
@Table(name = "board_columns", indexes = {
        @Index(name = "idx_board_columns_board_id", columnList = "board_id"),
        @Index(name = "idx_board_columns_status_key", columnList = "status_key")
})
public class BoardColumnEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "status_key", nullable = false, length = 60)
    private String statusKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_category", nullable = false, length = 20)
    private StatusCategory statusCategory = StatusCategory.TO_DO;

    @Column(name = "position_index", nullable = false)
    private Integer position;

    @Column(name = "wip_limit")
    private Integer wipLimit;
}
