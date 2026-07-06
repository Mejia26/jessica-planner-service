package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.PlanPolicyDtos;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.domain.entity.ActionEntity;
import com.guido.agiletaskservice.domain.entity.FeatureEntity;
import com.guido.agiletaskservice.domain.repository.ActionRepository;
import com.guido.agiletaskservice.domain.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanPolicyYamlService {

    private static final Pattern YAML_MAPPING_KEY_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");

    private final ActionRepository actionRepository;
    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public String generatePlansYaml(PlanPolicyDtos.GeneratePlansYamlRequest request) {
        validatePlanActions(request.plans());
        Set<UUID> enabledActionIds = request.plans().stream()
                .flatMap(plan -> plan.actions().stream())
                .filter(action -> Boolean.TRUE.equals(action.enabled()))
                .map(PlanPolicyDtos.PlanActionSelectionRequest::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<UUID, ActionEntity> actionsById = actionRepository.findActiveActionsWithFeaturesByIdIn(enabledActionIds).stream()
                .collect(Collectors.toMap(ActionEntity::getId, Function.identity()));
        assertAllActionsExist(enabledActionIds, actionsById.keySet());

        StringBuilder yaml = new StringBuilder();
        yaml.append("plans:\n");
        yaml.append("  plans:\n");
        for (PlanPolicyDtos.PlanDefinitionRequest plan : request.plans()) {
            appendPlan(yaml, plan, actionsById);
        }
        log.info("Generated SaaS plans YAML: planCount={}, enabledActionCount={}", request.plans().size(), enabledActionIds.size());
        return yaml.toString();
    }

    @Transactional(readOnly = true)
    public String generatePlanPolicyMappingYaml() {
        List<FeatureEntity> features = featureRepository.findActiveFeaturesWithActions();
        StringBuilder yaml = new StringBuilder();
        int mappingCount = 0;
        List<String> invalidMappings = new ArrayList<>();

        for (FeatureEntity feature : features) {
            List<ActionEntity> activeActions = feature.getActions().stream()
                    .filter(ActionEntity::isActive)
                    .toList();
            validatePolicyMapping(feature.getFeatureName(), feature.getAbbreviationKey(), feature.getUserPolicyId(), invalidMappings);
            activeActions.forEach(action -> validatePolicyMapping(
                    feature.getFeatureName() + " - " + action.getActionName(),
                    action.getAbbreviationKey(),
                    action.getUserPolicyId(),
                    invalidMappings
            ));
            if (!invalidMappings.isEmpty()) {
                continue;
            }

            if (!yaml.isEmpty()) {
                yaml.append('\n');
            }
            yaml.append("# ").append(cleanComment(feature.getFeatureName())).append('\n');
            yaml.append(feature.getAbbreviationKey().trim()).append(": ").append(feature.getUserPolicyId()).append('\n');
            mappingCount++;
            for (ActionEntity action : activeActions) {
                yaml.append(action.getAbbreviationKey().trim()).append(": ").append(action.getUserPolicyId()).append('\n');
                mappingCount++;
            }
        }

        if (!invalidMappings.isEmpty()) {
            throw new BusinessRuleException("Cannot generate policy mapping YAML. Missing or invalid policy mapping data: "
                    + String.join(", ", invalidMappings));
        }

        log.info("Generated plan policy mapping YAML: featureCount={}, mappingCount={}", features.size(), mappingCount);
        return yaml.toString();
    }

    private void appendPlan(StringBuilder yaml, PlanPolicyDtos.PlanDefinitionRequest plan, Map<UUID, ActionEntity> actionsById) {
        UUID planId = plan.planId() == null ? UUID.randomUUID() : plan.planId();
        yaml.append("    - planId: \"").append(planId).append("\"\n");
        yaml.append("      name: \"").append(escapeDoubleQuoted(plan.name().trim())).append("\"\n");
        List<PlanPolicyDtos.PlanActionSelectionRequest> enabledActions = plan.actions().stream()
                .filter(selectedAction -> Boolean.TRUE.equals(selectedAction.enabled()))
                .toList();
        if (enabledActions.isEmpty()) {
            yaml.append("      actions: []\n");
            return;
        }
        yaml.append("      actions:\n");
        for (PlanPolicyDtos.PlanActionSelectionRequest selectedAction : enabledActions) {
            ActionEntity action = actionsById.get(selectedAction.id());
            yaml.append("        - id: ").append(action.getId()).append('\n');
            yaml.append("          description: ").append(toPlanActionDescription(action)).append('\n');
        }
    }

    private String toPlanActionDescription(ActionEntity action) {
        return cleanPlainScalar(action.getFeature().getFeatureName()) + " - " + cleanPlainScalar(action.getActionName());
    }

    private void validatePlanActions(List<PlanPolicyDtos.PlanDefinitionRequest> plans) {
        for (PlanPolicyDtos.PlanDefinitionRequest plan : plans) {
            Set<UUID> enabledActionIds = new LinkedHashSet<>();
            List<UUID> duplicates = plan.actions().stream()
                    .filter(action -> Boolean.TRUE.equals(action.enabled()))
                    .map(PlanPolicyDtos.PlanActionSelectionRequest::id)
                    .filter(actionId -> !enabledActionIds.add(actionId))
                    .toList();
            if (!duplicates.isEmpty()) {
                throw new BusinessRuleException("Plan contains duplicate enabled actions: planName=" + plan.name()
                        + ", actionIds=" + duplicates);
            }
        }
    }

    private void assertAllActionsExist(Collection<UUID> requestedIds, Set<UUID> foundIds) {
        List<UUID> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new BusinessRuleException("Some enabled actions were not found or are inactive: " + missingIds);
        }
    }

    private void validatePolicyMapping(String label, String abbreviationKey, UUID userPolicyId, List<String> invalidMappings) {
        if (isBlank(abbreviationKey) || userPolicyId == null) {
            invalidMappings.add(label);
            return;
        }
        String trimmedKey = abbreviationKey.trim();
        if (!YAML_MAPPING_KEY_PATTERN.matcher(trimmedKey).matches()) {
            invalidMappings.add(label + " has invalid abbreviation_key=" + trimmedKey);
        }
    }

    private String cleanPlainScalar(String value) {
        String cleaned = Objects.requireNonNull(value, "value must not be null").trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            throw new BusinessRuleException("Cannot generate YAML with a blank scalar value.");
        }
        return cleaned;
    }

    private String cleanComment(String value) {
        return cleanPlainScalar(value).replace("#", "");
    }

    private String escapeDoubleQuoted(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
