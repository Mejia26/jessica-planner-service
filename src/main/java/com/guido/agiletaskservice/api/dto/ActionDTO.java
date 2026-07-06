package com.guido.agiletaskservice.api.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionDTO {

	private UUID id;
	private String actionName;
	private String description;
	private UUID featureId;
	private UUID userPolicyId;
	private String abbreviationKey;
}
