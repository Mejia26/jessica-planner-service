package com.guido.agiletaskservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.guido.agiletaskservice.api.dto.FeatureDTO;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.domain.entity.FeatureEntity;
import com.guido.agiletaskservice.domain.repository.FeatureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {

	private final FeatureRepository featureRepository;
	
	public List<FeatureDTO> getActiveFeatures(){
		log.info("get active features");
		List<FeatureEntity> features = featureRepository.findActiveFeatures();
		return features.stream().map(ApiMapper::fromFeatureEntityToDTO).toList();
	}
}
