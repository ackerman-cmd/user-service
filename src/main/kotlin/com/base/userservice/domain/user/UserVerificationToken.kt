package com.base.userservice.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_verification_tokens", schema = "user_service")
class UserVerificationToken(
    @Id
    val id: UUID? = null,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(nullable = false, unique = true)
    val token: String,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    @Column(name = "used_at")
    var usedAt: LocalDateTime? = null,
)
