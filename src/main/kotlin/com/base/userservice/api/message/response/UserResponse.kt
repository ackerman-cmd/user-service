package com.base.userservice.api.message.response

import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Профиль пользователя")
data class UserResponse(
    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,
    @Schema(description = "Email пользователя", example = "user@example.com")
    val email: String,
    @Schema(description = "Имя пользователя", example = "john_doe")
    val username: String,
    @Schema(description = "Имя", example = "John")
    val firstName: String?,
    @Schema(description = "Фамилия", example = "Doe")
    val lastName: String?,
    @Schema(description = "Статус аккаунта", example = "ACTIVE")
    val status: UserStatus,
    @Schema(description = "Подтверждён ли email", example = "true")
    val emailVerified: Boolean,
    @Schema(
        description = "Список ролей пользователя (возвращается только для /me и admin-эндпоинтов)",
        example = "[\"ROLE_USER\"]",
        nullable = true,
    )
    val roles: List<String>?,
    @Schema(
        description = "Список permissions пользователя (возвращается только для /me и admin-эндпоинтов)",
        example = "[\"USER_READ\"]",
        nullable = true,
    )
    val permissions: List<String>?,
    @Schema(description = "Дата и время регистрации", example = "2025-01-15T10:30:00")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(user: User) =
            UserResponse(
                id = user.id!!,
                email = user.email,
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                status = user.status,
                emailVerified = user.emailVerified,
                roles = null,
                permissions = null,
                createdAt = user.createdAt,
            )

        fun fromWithRoles(
            user: User,
            roles: List<String>,
        ) = from(user).copy(roles = roles)

        fun fromWithRolesAndPermissions(
            user: User,
            roles: List<String>,
            permissions: List<String>,
        ) = from(user).copy(roles = roles, permissions = permissions)
    }
}
