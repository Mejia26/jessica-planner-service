package com.guido.agiletaskservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        Provider provider,
        DataSize maxFileSize,
        Local local,
        AwsS3 awsS3,
        GoogleCloud googleCloud
) {
    public enum Provider {
        LOCAL,
        AWS_S3,
        GOOGLE_CLOUD_STORAGE
    }

    public record Local(String rootPath, String publicBaseUrl) {
    }

    public record AwsS3(String bucketName, String region, String keyPrefix, String publicBaseUrl) {
    }

    public record GoogleCloud(String bucketName, String projectId, String keyPrefix, String publicBaseUrl) {
    }
}
