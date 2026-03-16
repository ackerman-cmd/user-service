package com.base.userservice.event

import com.base.userservice.domain.outbox.OutboxEventType
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import java.time.LocalDateTime
import java.util.UUID

data class UserSyncEvent(
    val userId: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: List<String>,
    val eventType: OutboxEventType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(
            user: User,
            eventType: OutboxEventType,
        ) = UserSyncEvent(
            userId = user.id!!,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            status = user.status,
            emailVerified = user.emailVerified,
            roles = user.roles.map { it.name.name },
            eventType = eventType,
        )
    }
}
