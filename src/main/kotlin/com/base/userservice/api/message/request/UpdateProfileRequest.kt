package com.base.userservice.api.message.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Запрос на обновление профиля пользователя. Поля со значением null не изменяются.")
data class UpdateProfileRequest(
    @field:Size(max = 64)
    @Schema(description = "Новое имя (до 64 символов, null — не менять)", example = "Alice")
    val firstName: String? = null,
    @field:Size(max = 64)
    @Schema(description = "Новая фамилия (до 64 символов, null — не менять)", example = "Smith")
    val lastName: String? = null,
)
