package com.guido.agiletaskservice.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard error response returned by the API.")
public record ApiErrorResponse(
        @Schema(description = "UTC timestamp when the error was generated.", example = "2026-06-29T13:45:30.000Z")
        Instant timestamp,

        @Schema(description = "HTTP status code.", example = "400")
        int status,

        @Schema(description = "Stable application error code.", example = "VALIDATION_ERROR")
        String code,

        @Schema(description = "Human-readable error message.", example = "Request validation failed.")
        String message,

        @Schema(description = "Request path that produced the error.", example = "/api/v1/issues")
        String path,

        @Schema(description = "Field-level validation errors, when available.")
        List<FieldErrorItem> fieldErrors
) {
    public record FieldErrorItem(
            @Schema(description = "Invalid field name.", example = "title")
            String field,

            @Schema(description = "Validation error message.", example = "must not be blank")
            String message
    ) {
    }
}
