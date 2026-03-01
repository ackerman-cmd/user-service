package com.base.userservice

import com.base.userservice.domain.role.Permission
import com.base.userservice.domain.role.PermissionType
import com.base.userservice.domain.role.Role
import com.base.userservice.domain.role.RoleType
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.repository.PermissionRepository
import com.base.userservice.repository.RoleRepository
import com.base.userservice.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class TestDataInsertionUtils(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) {

    @Transactional
    fun insertUser(
        email: String = "user${UUID.randomUUID()}@example.com",
        username: String = "testuser${UUID.randomUUID()}",
        status: UserStatus = UserStatus.ACTIVE,
        emailVerified: Boolean = true,
        passwordHash: String = "hashed",
        id: UUID = UUID.randomUUID(),
    ): User {
        insertRole(RoleType.ROLE_USER)
        insertPermission(PermissionType.USER_READ)
        insertPermission(PermissionType.USER_WRITE)
        insertPermission(PermissionType.USER_DELETE)
        val now = LocalDateTime.now()
        return userRepository.save(
            User(
                id = id,
                email = email,
                username = username,
                passwordHash = passwordHash,
                firstName = "John",
                lastName = "Doe",
                status = status,
                emailVerified = emailVerified,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    @Transactional
    fun insertRole(
       roleType: RoleType,
       permissions: MutableSet<Permission> = mutableSetOf()
    ): Role =
        roleRepository.save(Role(name = roleType, permissions = permissions))

    @Transactional
    fun insertPermission(
        permissionType: PermissionType,
    ): Permission =
        permissionRepository.save(Permission(name = permissionType))


}