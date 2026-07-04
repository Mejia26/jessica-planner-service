package com.guido.agiletaskservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI agileTaskServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agile Task Service API")
                        .version("v1")
                        .description("Microservice API for Scrum and Kanban project planning, boards, sprints, issues, comments, and attachments.")
                        .contact(new Contact().name("Agile Platform Team"))
                        .license(new License().name("Internal use")));
    }
}
