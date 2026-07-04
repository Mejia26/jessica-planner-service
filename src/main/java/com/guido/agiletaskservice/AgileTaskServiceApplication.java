package com.guido.agiletaskservice;

import com.guido.agiletaskservice.config.CacheProperties;
import com.guido.agiletaskservice.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
//@EnableConfigurationProperties({StorageProperties.class, CacheProperties.class})
public class AgileTaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgileTaskServiceApplication.class, args);
    }
}
