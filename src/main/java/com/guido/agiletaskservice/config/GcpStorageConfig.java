package com.guido.agiletaskservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "GOOGLE_CLOUD_STORAGE")
public class GcpStorageConfig {

    private final StorageProperties storageProperties;

    public GcpStorageConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Bean
    public Storage googleCloudStorage() {
        StorageOptions.Builder builder = StorageOptions.newBuilder();
        
        String projectId = storageProperties.googleCloud().projectId();
        if (StringUtils.hasText(projectId)) {
            builder.setProjectId(projectId);
        }

        // 1. Intentar leer la variable de entorno que usaremos en Render
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS");
        
        if (StringUtils.hasText(credentialsJson)) {
            try {
                builder.setCredentials(GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
                ));
                log.info("Google Cloud Storage configurado exitosamente usando la variable de entorno en memoria.");
            } catch (Exception e) {
                log.error("Error al inicializar las credenciales desde GOOGLE_CREDENTIALS", e);
            }
        } else {
            // 2. Si no existe la variable (Entorno Local), el SDK usará automáticamente tu sesión de gcloud local
            log.info("No se detectó GOOGLE_CREDENTIALS. Usando Application Default Credentials de forma local.");
        }

        return builder.build().getService();
    }
}