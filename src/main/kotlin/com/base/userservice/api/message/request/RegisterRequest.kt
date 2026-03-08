package com.base.userservice.api.message.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Запрос на регистрацию нового пользователя")
data class RegisterRequest(
    @field:NotBlank
    @field:Email
    @Schema(
        description = "Email пользователя (уникальный)",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val email: String,
    @field:NotBlank
    @field:Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    @Schema(
        description = "Имя пользователя (уникальное, 3–64 символа)",
        example = "john_doe",
        minLength = 3,
        maxLength = 64,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val username: String,
    @field:NotBlank
    @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Schema(
        description = "Пароль (8–128 символов, хешируется BCrypt)",
        example = "SecureP@ss123",
        minLength = 8,
        maxLength = 128,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val password: String,
    @Schema(description = "Имя", example = "John")
    val firstName: String? = null,
    @Schema(description = "Фамилия", example = "Doe")
    val lastName: String? = null,
)
