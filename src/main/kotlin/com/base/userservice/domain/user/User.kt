package com.base.userservice.domain.user

import com.base.userservice.domain.role.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users", schema = "user_service")
class User(
    @Id
    val id: UUID? = null,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false, unique = true)
    val username: String,
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
    @Column(name = "first_name")
    var firstName: String? = null,
    @Column(name = "last_name")
    var lastName: String? = null,
    @Enumerated(EnumType.STRING)
    @Column
    var status: UserStatus,
    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        schema = "user_service",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    val roles: MutableSet<Role> = mutableSetOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime,
)
