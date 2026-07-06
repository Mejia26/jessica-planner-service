package com.guido.agiletaskservice.domain.repository;

import com.guido.agiletaskservice.domain.entity.ActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ActionRepository extends JpaRepository<ActionEntity, UUID> {

    @Query("""
            select action
            from ActionEntity action
            join fetch action.feature feature
            where action.id in :ids
              and coalesce(action.deleted, false) = false
              and coalesce(feature.deleted, false) = false
            """)
    List<ActionEntity> findActiveActionsWithFeaturesByIdIn(@Param("ids") Collection<UUID> ids);
    
    @Query("""
    		SELECT a
    		FROM ActionEntity a
    		WHERE a.deleted = false
    		""")
    List<ActionEntity> findActiveActions();
}
