//package com.guido.agiletaskservice.infrastructure.storage;
//
//import com.guido.agiletaskservice.application.port.ResourceStorageService;
//import com.guido.agiletaskservice.common.exception.BusinessRuleException;
//import com.guido.agiletaskservice.config.StorageProperties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "LOCAL", matchIfMissing = true)
//public class LocalResourceStorageService implements ResourceStorageService {
//
//    private final StorageProperties storageProperties;
//
//    @Override
//    public StoredResource store(String folder, MultipartFile file) {
//        validateFileSize(file);
//        String sanitizedName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "resource" : file.getOriginalFilename());
//        String storageKey = folder + "/" + UUID.randomUUID() + "-" + sanitizedName;
//        Path rootPath = Path.of(storageProperties.local().rootPath()).toAbsolutePath().normalize();
//        Path targetPath = rootPath.resolve(storageKey).normalize();
//
//        if (!targetPath.startsWith(rootPath)) {
//            throw new BusinessRuleException("Invalid storage path.");
//        }
//
//        try {
//            Files.createDirectories(targetPath.getParent());
//            file.transferTo(targetPath);
//            String publicUrl = buildPublicUrl(storageKey);
//            log.info("Stored file locally: key={}, sizeBytes={}, contentType={}", storageKey, file.getSize(), file.getContentType());
//            return new StoredResource(storageKey, publicUrl, file.getContentType(), file.getSize());
//        } catch (IOException exception) {
//            log.error("Failed to store file locally: key={}", storageKey, exception);
//            throw new BusinessRuleException("Could not store uploaded file.");
//        }
//    }
//
//    @Override
//    public void delete(String storageKey) {
//        Path rootPath = Path.of(storageProperties.local().rootPath()).toAbsolutePath().normalize();
//        Path targetPath = rootPath.resolve(storageKey).normalize();
//        if (!targetPath.startsWith(rootPath)) {
//            throw new BusinessRuleException("Invalid storage path.");
//        }
//        try {
//            Files.deleteIfExists(targetPath);
//            log.info("Deleted local file: key={}", storageKey);
//        } catch (IOException exception) {
//            log.warn("Could not delete local file: key={}", storageKey, exception);
//        }
//    }
//
//    private void validateFileSize(MultipartFile file) {
//        long maxBytes = storageProperties.maxFileSize().toBytes();
//        if (file.getSize() > maxBytes) {
//            throw new BusinessRuleException("File exceeds the configured maximum size.");
//        }
//    }
//
//    private String buildPublicUrl(String storageKey) {
//        String baseUrl = storageProperties.local().publicBaseUrl();
//        if (!StringUtils.hasText(baseUrl)) {
//            return null;
//        }
//        return baseUrl.replaceAll("/$", "") + "/" + storageKey;
//    }
//}
