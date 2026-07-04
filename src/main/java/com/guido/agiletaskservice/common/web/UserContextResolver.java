package com.guido.agiletaskservice.common.web;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class UserContextResolver {

    public static final String USER_ID_HEADER = "X-User-Id";

    private UserContextResolver() {
    }

    @Parameter(description = "Authenticated user id supplied by the gateway.", required = true, example = "f1f5b914-8ba9-4e17-9d68-a84f26f7a572")
    public @NotNull UUID userIdHeader(UUID userId) {
        return userId;
    }
}
