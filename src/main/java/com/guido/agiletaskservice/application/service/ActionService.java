package com.guido.agiletaskservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.guido.agiletaskservice.api.dto.ActionDTO;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.domain.entity.ActionEntity;
import com.guido.agiletaskservice.domain.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionService {

	private final ActionRepository actionRepository;
	
	public List<ActionDTO> getActiveActions() {
		log.info("get active actions");
		
		List<ActionEntity> actions = actionRepository.findActiveActions();
		return actions.stream().map(ApiMapper::fromActionEntityToDTO).toList();
	}
	
	
}
