package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.FeatureEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FeatureRepository extends JpaRepository<FeatureEntity, UUID> {

    @EntityGraph(attributePaths = "actions")
    @Query("""
            select distinct feature
            from FeatureEntity feature
            where coalesce(feature.deleted, false) = false
            order by feature.featureName asc
            """)
    List<FeatureEntity> findActiveFeaturesWithActions();
    
    @Query("""
            SELECT f
            from FeatureEntity f
            WHERE f.deleted = false
            """)
    List<FeatureEntity> findActiveFeatures();
}
