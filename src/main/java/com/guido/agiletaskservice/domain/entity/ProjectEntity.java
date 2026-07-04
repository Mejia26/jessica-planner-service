package com.guido.agiletaskservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_projects_key", columnList = "project_key", unique = true),
        @Index(name = "idx_projects_created_by", columnList = "created_by_user_id")
})
public class ProjectEntity extends BaseEntity {

    @Column(name = "project_key", nullable = false, length = 20, unique = true)
    private String key;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;
}
