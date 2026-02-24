package com.base.userservice.api.message.response

import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: List<String>?,
    val permissions: List<String>?,
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
