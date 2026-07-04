package com.guido.agiletaskservice.domain.entity;

import com.guido.agiletaskservice.domain.enums.ProjectOptionType;
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
@Table(name = "project_options", indexes = {
        @Index(name = "idx_project_options_project_id", columnList = "project_id"),
        @Index(name = "idx_project_options_type", columnList = "type"),
        @Index(name = "idx_project_options_key", columnList = "option_key")
})
public class ProjectOptionEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectOptionType type;

    @Column(name = "option_key", nullable = false, length = 80)
    private String key;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 32)
    private String color;

    @Column(name = "position_index", nullable = false)
    private Integer position = 0;
}
