package com.base.userservice.service

import com.base.userservice.api.message.request.ChangePasswordRequest
import com.base.userservice.api.message.request.UpdateProfileRequest
import com.base.userservice.api.message.response.RoleInfoResponse
import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.outbox.OutboxEventType
import com.base.userservice.domain.role.RoleType
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.event.EventPublisher
import com.base.userservice.exception.InvalidPasswordException
import com.base.userservice.exception.InvalidRoleException
import com.base.userservice.exception.UserNotFoundException
import com.base.userservice.repository.RoleRepository
import com.base.userservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: EventPublisher,
) {
    fun findById(id: UUID): UserResponse = UserResponse.from(getById(id))

    @Transactional(readOnly = true)
    fun findByIdWithRolesAndPermissions(id: UUID): UserResponse = toResponseWithRolesAndPermissions(getByIdWithRolesAndPermissions(id))

    fun findByUsername(name: String): UserResponse = UserResponse.from(getByUsername(name))

    @Transactional(readOnly = true)
    fun findByUsernameWithRolesAndPermissions(name: String): UserResponse {
        val user =
            userRepository.findByUsernameWithRolesAndPermissions(name)
                ?: throw UserNotFoundException("User with username: $name not found")
        return toResponseWithRolesAndPermissions(user)
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(jwt: Jwt): UserResponse = toResponseWithRolesAndPermissions(getByIdWithRolesAndPermissions(extractUserId(jwt)))

    @Transactional
    fun updateProfile(
        jwt: Jwt,
        request: UpdateProfileRequest,
    ): UserResponse {
        val user = getById(extractUserId(jwt))
        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        val saved = userRepository.save(user)
        eventPublisher.publishUserSync(saved, OutboxEventType.USER_PROFILE_UPDATED)
        return UserResponse.from(saved)
    }

    @Transactional
    fun changePassword(
        jwt: Jwt,
        request: ChangePasswordRequest,
    ) {
        val user = getById(extractUserId(jwt))

        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw InvalidPasswordException("Current password is incorrect")
        }

        user.passwordHash = passwordEncoder.encode(request.newPassword)!!
        val saved = userRepository.save(user)
        eventPublisher.publishUserSync(saved, OutboxEventType.USER_PASSWORD_CHANGED)
    }

    @Transactional
    fun changeOwnStatus(
        jwt: Jwt,
        status: UserStatus,
    ): UserResponse {
        val user = getById(extractUserId(jwt))
        user.status = status
        val saved = userRepository.save(user)
        eventPublisher.publishUserSync(saved, OutboxEventType.USER_STATUS_CHANGED)
        return UserResponse.from(saved)
    }

    @Transactional
    fun changeStatus(
        id: UUID,
        status: UserStatus,
    ): UserResponse {
        val user = getById(id)
        user.status = status
        val saved = userRepository.save(user)
        eventPublisher.publishUserSync(saved, OutboxEventType.USER_STATUS_CHANGED)
        return UserResponse.from(saved)
    }

    @Transactional(readOnly = true)
    fun getAvailableRoles(): List<RoleInfoResponse> =
        roleRepository.findAllByOrderByNameAsc().map {
            RoleInfoResponse(name = it.name.name, description = it.description)
        }

    @Transactional
    fun assignRoles(
        userId: UUID,
        roleNames: List<String>,
    ): UserResponse {
        val roleTypes =
            roleNames.map { name ->
                try {
                    RoleType.valueOf(name)
                } catch (e: IllegalArgumentException) {
                    throw InvalidRoleException(
                        "Неизвестная роль: $name. Допустимые: ${RoleType.entries.joinToString { r -> r.name }}",
                    )
                }
            }
        val distinctTypes = roleTypes.distinct()
        val roles = roleRepository.findByNameIn(distinctTypes)
        if (roles.size != distinctTypes.size) {
            val foundNames = roles.map { it.name }.toSet()
            val missing = distinctTypes.filter { it !in foundNames }
            throw InvalidRoleException("Роли не найдены в БД: ${missing.joinToString { it.name }}. Обратитесь к администратору.")
        }
        val user = getByIdWithRolesAndPermissions(userId)
        user.roles.clear()
        user.roles.addAll(roles)
        val saved = userRepository.save(user)
        eventPublisher.publishUserSync(saved, OutboxEventType.USER_ROLES_CHANGED)
        return toResponseWithRolesAndPermissions(saved)
    }

    private fun extractUserId(jwt: Jwt): UUID = UUID.fromString(jwt.getClaimAsString("user_id"))

    private fun getById(id: UUID): User =
        userRepository.findById(id).orElseThrow {
            UserNotFoundException("User with id: $id not found")
        }

    private fun getByIdWithRolesAndPermissions(id: UUID): User =
        userRepository.findByIdWithRolesAndPermissions(id)
            ?: throw UserNotFoundException("User with id: $id not found")

    private fun getByUsername(username: String): User =
        userRepository.findByUsername(username)
            ?: throw UserNotFoundException("User with username: $username not found")

    private fun toResponseWithRolesAndPermissions(user: User): UserResponse {
        val roles = user.roles.map { it.name.name }
        val perms =
            user.roles
                .flatMap { it.permissions }
                .map { it.name.name }
                .distinct()
        return UserResponse.fromWithRolesAndPermissions(user, roles, perms)
    }
}
