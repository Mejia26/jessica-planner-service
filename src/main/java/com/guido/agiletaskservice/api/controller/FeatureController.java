package com.guido.agiletaskservice.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guido.agiletaskservice.api.dto.FeatureDTO;
import com.guido.agiletaskservice.application.service.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/features")
public class FeatureController {

	private final FeatureService featureService;
	
	@GetMapping()
    @Operation(summary = "Get active features", description = "Returns only active features.")
    public List<FeatureDTO> getFeatures(   
    ) {
        return featureService.getActiveFeatures();
    }
}
