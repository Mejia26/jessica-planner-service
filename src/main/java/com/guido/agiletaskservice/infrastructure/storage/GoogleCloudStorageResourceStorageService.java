package com.guido.agiletaskservice.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.guido.agiletaskservice.application.port.ResourceStorageService;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "GOOGLE_CLOUD_STORAGE")
public class GoogleCloudStorageResourceStorageService implements ResourceStorageService {

    private final StorageProperties storageProperties;
    private final Storage storage;

    @Override
    public StoredResource store(String folder, MultipartFile file) {
        validateFile(file);

        String sanitizedName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "resource" : file.getOriginalFilename());
        String storageKey = buildStorageKey(folder, sanitizedName);

        if (!isConfigured()) {
            log.warn("Google Cloud Storage is selected but not fully configured. Attachment metadata will be saved without uploading the object: key={}", storageKey);
            return fallbackResource(storageKey, file);
        }

        try {
            BlobId blobId = BlobId.of(storageProperties.googleCloud().bucketName(), storageKey);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                    .build();
            
            // Reemplazado: Usamos la instancia 'storage' inyectada directamente
            storage.create(blobInfo, file.getBytes());
            
            String publicUrl = buildPublicUrl(storageKey);
            log.info("Stored file in Google Cloud Storage: bucket={}, key={}, sizeBytes={}, contentType={}",
                    storageProperties.googleCloud().bucketName(), storageKey, file.getSize(), file.getContentType());
            return new StoredResource(storageKey, publicUrl, blobInfo.getContentType(), file.getSize());
        } catch (IOException exception) {
            log.error("Failed to read upload before storing in Google Cloud Storage: key={}", storageKey, exception);
            throw new BusinessRuleException("Could not store uploaded file.");
        } catch (RuntimeException exception) {
            log.warn("Failed to store file in Google Cloud Storage. Continuing with attachment metadata only: key={}", storageKey, exception);
            return fallbackResource(storageKey, file);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }
        if (!isConfigured()) {
            log.warn("Google Cloud Storage is selected but not configured. Delete was skipped: key={}", storageKey);
            return;
        }
        try {
            // Reemplazado: Usamos la instancia 'storage'
            boolean deleted = storage.delete(BlobId.of(storageProperties.googleCloud().bucketName(), storageKey));
            log.info("Deleted Google Cloud Storage file: key={}, deleted={}", storageKey, deleted);
        } catch (RuntimeException exception) {
            log.warn("Could not delete Google Cloud Storage file: key={}", storageKey, exception);
        }
    }

    private boolean isConfigured() {
        return StringUtils.hasText(storageProperties.googleCloud().bucketName());
    }

    // ELIMINADO: El método privado 'private Storage storage()' ya no es necesario aquí.

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("Uploaded file cannot be empty.");
        }
        long maxBytes = storageProperties.maxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleException("File exceeds the configured maximum size.");
        }
    }

    private String buildStorageKey(String folder, String sanitizedName) {
        String prefix = storageProperties.googleCloud().keyPrefix();
        String normalizedPrefix = StringUtils.hasText(prefix) ? prefix.replaceAll("^/+|/+$", "") + "/" : "";
        String normalizedFolder = StringUtils.hasText(folder) ? folder.replaceAll("^/+|/+$", "") + "/" : "";
        return normalizedPrefix + normalizedFolder + UUID.randomUUID() + "-" + sanitizedName;
    }

    private String buildPublicUrl(String storageKey) {
        String baseUrl = storageProperties.googleCloud().publicBaseUrl();
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl.replaceAll("/$", "") + "/" + storageKey;
        }
        return "https://storage.googleapis.com/" + storageProperties.googleCloud().bucketName() + "/" + storageKey;
    }

    private StoredResource fallbackResource(String storageKey, MultipartFile file) {
        return new StoredResource(storageKey, null, file.getContentType() == null ? "application/octet-stream" : file.getContentType(), file.getSize());
    }
}
