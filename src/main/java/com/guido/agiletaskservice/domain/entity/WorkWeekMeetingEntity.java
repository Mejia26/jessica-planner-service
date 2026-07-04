package com.guido.agiletaskservice.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "work_week_meetings", indexes = {
        @Index(name = "idx_work_week_meetings_date", columnList = "meeting_date"),
        @Index(name = "idx_work_week_meetings_day", columnList = "day_of_week")
})
public class WorkWeekMeetingEntity extends BaseEntity {

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false, length = 220)
    private String purpose;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    private List<WorkWeekMeetingParticipantEntity> participants = new ArrayList<>();
}
