package com.base.userservice.api.message.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Schema(description = "Назначение ролей пользователю. Полный список ролей — предыдущие заменяются.")
data class AssignRolesRequest(
    @field:NotEmpty(message = "Список ролей не может быть пустым")
    @field:NotNull
    @Schema(
        description = "Список ролей пользователя (заменяет текущие). Допустимые: ROLE_USER, ROLE_OPERATOR, ROLE_ADMIN",
        example = "[\"ROLE_USER\", \"ROLE_OPERATOR\"]",
        required = true,
    )
    val roles: List<String>,
)
