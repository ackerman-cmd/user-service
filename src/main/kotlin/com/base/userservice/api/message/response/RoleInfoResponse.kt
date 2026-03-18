package com.base.userservice.api.message.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Информация о роли (для выбора при назначении)")
data class RoleInfoResponse(
    @Schema(description = "Имя роли (enum)", example = "ROLE_OPERATOR")
    val name: String,
    @Schema(description = "Описание роли", example = "Оператор: работа с обращениями клиентов")
    val description: String?,
)
