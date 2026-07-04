package com.guido.agiletaskservice.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ResourceStorageService {

    StoredResource store(String folder, MultipartFile file);

    void delete(String storageKey);

    record StoredResource(String storageKey, String publicUrl, String contentType, long sizeBytes) {
    }
}
