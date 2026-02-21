package com.base.userservice.dto

import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val username: String,
    @field:NotBlank
    @field:Size(min = 8, max = 128)
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
)

data class UpdateProfileRequest(
    @field:Size(max = 64)
    val firstName: String? = null,
    @field:Size(max = 64)
    val lastName: String? = null,
)

data class ChangePasswordRequest(
    @field:NotBlank
    val currentPassword: String,
    @field:NotBlank
    @field:Size(min = 8, max = 128)
    val newPassword: String,
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: List<String>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(user: User): UserResponse =
            UserResponse(
                id = user.id,
                email = user.email,
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                status = user.status,
                emailVerified = user.emailVerified,
                roles = user.roles.map { it.name },
                createdAt = user.createdAt,
            )
    }
}
