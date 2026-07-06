package com.guido.agiletaskservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "actions")
public class ActionEntity {

    @Id
    @Column(name = "action_id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "action_name", nullable = false, length = 255)
    private String actionName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private FeatureEntity feature;

    @Column(name = "user_policy_id")
    private UUID userPolicyId;

    @Column(name = "abbreviation_key", length = 160)
    private String abbreviationKey;

    public boolean isActive() {
        return !Boolean.TRUE.equals(deleted);
    }
}
