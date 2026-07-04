package com.guido.agiletaskservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "work_week_meeting_participants", indexes = {
        @Index(name = "idx_work_week_participants_meeting", columnList = "meeting_id")
})
public class WorkWeekMeetingParticipantEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private WorkWeekMeetingEntity meeting;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(name = "role_label", length = 120)
    private String roleLabel;

    @Column(name = "external_reference", length = 160)
    private String externalReference;
}
