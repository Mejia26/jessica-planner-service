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
@Table(name = "calendar_categories", indexes = {
        @Index(name = "idx_calendar_categories_owner", columnList = "owner_user_id"),
        @Index(name = "idx_calendar_categories_key", columnList = "category_key"),
        @Index(name = "idx_calendar_categories_deleted_at", columnList = "deleted_at")
})
public class CalendarCategoryEntity extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "category_key", nullable = false, length = 80)
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
