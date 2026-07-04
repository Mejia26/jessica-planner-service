package com.guido.agiletaskservice.domain.entity;

import com.guido.agiletaskservice.domain.enums.CalendarEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "calendar_events", indexes = {
        @Index(name = "idx_calendar_events_owner", columnList = "owner_user_id"),
        @Index(name = "idx_calendar_events_start_at", columnList = "start_at"),
        @Index(name = "idx_calendar_events_category_key", columnList = "category_key"),
        @Index(name = "idx_calendar_events_status", columnList = "status"),
        @Index(name = "idx_calendar_events_deleted_at", columnList = "deleted_at")
})
public class CalendarEventEntity extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 5000)
    private String notes;

    @Column(length = 240)
    private String location;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "all_day", nullable = false)
    private boolean allDay;

    @Column(name = "time_zone", nullable = false, length = 80)
    private String timeZone = "UTC";

    @Column(name = "category_key", length = 80)
    private String categoryKey;

    @Column(length = 32)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalendarEventStatus status = CalendarEventStatus.PLANNED;

    @Column(name = "reminder_at")
    private Instant reminderAt;
}
