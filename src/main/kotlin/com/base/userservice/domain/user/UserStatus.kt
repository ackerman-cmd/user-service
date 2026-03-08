package com.base.userservice.domain.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Статус аккаунта пользователя",
    enumAsRef = true,
)
enum class UserStatus {
    @Schema(description = "Аккаунт активен, пользователь может авторизоваться")
    ACTIVE,

    @Schema(description = "Аккаунт деактивирован пользователем или администратором")
    INACTIVE,

    @Schema(description = "Аккаунт заблокирован администратором")
    BLOCKED,

    @Schema(description = "Ожидает подтверждения email (начальный статус после регистрации)")
    PENDING_VERIFICATION,
}
