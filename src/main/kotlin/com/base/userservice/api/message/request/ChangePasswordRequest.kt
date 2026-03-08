package com.base.userservice.api.message.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Запрос на смену пароля")
data class ChangePasswordRequest(
    @field:NotBlank
    @Schema(
        description = "Текущий пароль для подтверждения личности",
        example = "OldP@ssword123",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val currentPassword: String,
    @field:NotBlank
    @field:Size(min = 8, max = 128)
    @Schema(
        description = "Новый пароль (8–128 символов)",
        example = "NewSecureP@ss456",
        minLength = 8,
        maxLength = 128,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val newPassword: String,
)
