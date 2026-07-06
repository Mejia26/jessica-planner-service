package com.guido.agiletaskservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "features")
public class FeatureEntity {

    @Id
    @Column(name = "feature_id", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "feature_name", nullable = false, length = 255)
    private String featureName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    @Column(name = "user_policy_id")
    private UUID userPolicyId;

    @Column(name = "abbreviation_key", length = 160)
    private String abbreviationKey;

    @OrderBy("actionName ASC")
    @OneToMany(mappedBy = "feature")
    private List<ActionEntity> actions = new ArrayList<>();

    public boolean isActive() {
        return !Boolean.TRUE.equals(deleted);
    }
}
