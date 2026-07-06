package com.guido.agiletaskservice.api.dto;

import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class FeatureDTO {

	private UUID id;
	private String featureName;
	private String description;
	private UUID userPolicyId;
	private String abbreviationKey;
}
