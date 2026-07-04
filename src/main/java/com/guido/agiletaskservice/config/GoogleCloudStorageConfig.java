//package com.guido.agiletaskservice.config;
//
//import com.google.cloud.storage.Storage;
//import com.google.cloud.storage.StorageOptions;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.StringUtils;
//
//@Configuration
//@RequiredArgsConstructor
//public class GoogleCloudStorageConfig {
//
//    private final StorageProperties storageProperties;
//
//    @Bean
//    @ConditionalOnProperty(
//            prefix = "app.storage",
//            name = "provider",
//            havingValue = "GOOGLE_CLOUD_STORAGE"
//    )
//    public Storage googleCloudStorage() {
//        StorageOptions.Builder builder = StorageOptions.newBuilder();
//
//        if (storageProperties.googleCloud() != null
//                && StringUtils.hasText(storageProperties.googleCloud().projectId())) {
//            builder.setProjectId(storageProperties.googleCloud().projectId());
//        }
//
//        return builder.build().getService();
//    }
//}