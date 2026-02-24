package com.base.userservice.service.impl

import com.base.userservice.api.message.request.LoginRequest
import com.base.userservice.api.message.response.LoginResponse
import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.role.RoleType
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.exeption.UserAlreadyExistsException
import com.base.userservice.repository.RoleRepository
import com.base.userservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(command: RegisterUserCommand): UserResponse {
        validateEmailAndUsername(command.email, command.username)

        val defaultRole = validateAndGetDefaultRole()

        val user =
            User(
                id = UUID.randomUUID(),
                email = command.email,
                username = command.username,
                passwordHash = passwordEncoder.encode(command.password)!!,
                firstName = command.firstName,
                lastName = command.lastName,
                status = UserStatus.PENDING_VERIFICATION,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        user.roles.add(defaultRole)

        val saved = userRepository.save(user)

        return UserResponse.from(saved)
    }

    fun authenticate(request: LoginRequest): LoginResponse =
        LoginResponse(accessToken = " ", refreshToken = " ", expiresAt = Instant.now().plusSeconds(10000))

    private fun validateEmailAndUsername(
        email: String,
        username: String,
    ) {
        if (userRepository.findByEmail(email) != null) {
            throw UserAlreadyExistsException("User with email: $email already exists")
        }

        if (userRepository.findByUsername(username) != null) {
            throw UserAlreadyExistsException("User with username: $username already exists")
        }
    }

    private fun validateAndGetDefaultRole() =
        roleRepository.findByName(RoleType.ROLE_USER)
            ?: throw IllegalStateException("Default role ${RoleType.ROLE_USER} not found")
}
