package com.base.userservice.domain.role

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "permissions", schema = "user_service")
class Permission(
    @Id
    val id: UUID? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val name: PermissionType,
    @Column
    val description: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
