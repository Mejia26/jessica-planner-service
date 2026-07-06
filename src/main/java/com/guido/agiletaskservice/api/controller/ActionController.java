package com.guido.agiletaskservice.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guido.agiletaskservice.api.dto.ActionDTO;
import com.guido.agiletaskservice.application.service.ActionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/actions")
public class ActionController {

	private final ActionService actionService;
	
	@GetMapping()
    @Operation(summary = "Get active actions", description = "Returns only active actions.")
    public List<ActionDTO> getActions(   
    ) {
        return actionService.getActiveActions();
    }
}
