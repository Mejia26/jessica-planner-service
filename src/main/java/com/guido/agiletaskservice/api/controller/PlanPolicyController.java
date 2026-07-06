package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.PlanPolicyDtos;
import com.guido.agiletaskservice.application.service.PlanPolicyYamlService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plan-policy")
@Tag(name = "Plan Policy YAML", description = "YAML generation APIs for SaaS plans and frontend policy mappings.")
public class PlanPolicyController {

    private static final MediaType YAML_MEDIA_TYPE = MediaType.parseMediaType("application/x-yaml");

    private final PlanPolicyYamlService planPolicyYamlService;

    @PostMapping(value = "/generate-plans-yaml", produces = "application/x-yaml")
    @Operation(
            summary = "Generate SaaS plans YAML",
            description = "Generates a YAML file with the exact plans.plans structure expected by the SaaS plan loader. Only enabled actions are included.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = PlanPolicyDtos.GeneratePlansYamlRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "plans": [
                                {
                                  "planId": "770e8400-e29b-41d4-a716-446655440201",
                                  "name": "Starter Plan",
                                  "actions": [
                                    {"id": "08fae65f-ec0e-43f6-8068-b60a0c8f2508", "enabled": true},
                                    {"id": "b6401cd6-f81f-40a8-986b-d62cb33163e8", "enabled": true},
                                    {"id": "a10c8165-65e6-4f29-b677-37c3b6ea6b03", "enabled": false}
                                  ]
                                }
                              ]
                            }
                            """)
            ))
    )
    @ApiResponse(responseCode = "200", description = "YAML file generated.", content = @Content(mediaType = "application/x-yaml"))
    @ApiResponse(responseCode = "400", description = "Invalid request or missing action data.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<byte[]> generatePlansYaml(@Valid @RequestBody PlanPolicyDtos.GeneratePlansYamlRequest request) {
        return yamlFile("saas-plans.yml", planPolicyYamlService.generatePlansYaml(request));
    }

    @PostMapping(value = "/generate-plan-policy-mapping", produces = "application/x-yaml")
    @Operation(
            summary = "Generate plan policy mapping YAML",
            description = "Generates abbreviation_key: user_policy_id mappings for every active feature followed by that feature's active actions.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{}")))
    )
    @ApiResponse(responseCode = "200", description = "YAML file generated.", content = @Content(
            mediaType = "application/x-yaml",
            examples = @ExampleObject(value = """
                    # Compliance Tracking
                    ct-global: 2b9fcb2a-8f47-4340-90b4-ccacf1516b9a
                    ct-audit-compliance: 9391c49d-0c6a-49d6-95ef-5adbe8bb40ef
                    """)
    ))
    @ApiResponse(responseCode = "400", description = "Missing abbreviation_key or user_policy_id data.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<byte[]> generatePlanPolicyMapping() {
        return yamlFile("plan-policy-mapping.yml", planPolicyYamlService.generatePlanPolicyMappingYaml());
    }

    private ResponseEntity<byte[]> yamlFile(String filename, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(YAML_MEDIA_TYPE)
                .contentLength(bytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(bytes);
    }
}
