package com.base.userservice.service

import com.base.userservice.api.message.request.LoginRequest
import com.base.userservice.api.message.response.LoginResponse
import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.role.RoleType
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.domain.user.UserVerificationToken
import com.base.userservice.exeption.UserAlreadyExistsException
import com.base.userservice.exeption.VerificationTokenException
import com.base.userservice.repository.RoleRepository
import com.base.userservice.repository.UserRepository
import com.base.userservice.repository.UserVerificationTokenRepository
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
    private val userVerificationTokenRepository: UserVerificationTokenRepository,
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

        val verificationToken =
            UserVerificationToken(
                id = UUID.randomUUID(),
                userId = requireNotNull(saved.id),
                token = UUID.randomUUID().toString(),
                expiresAt = LocalDateTime.now().plusDays(1),
            )

        userVerificationTokenRepository.save(verificationToken)

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

    @Transactional
    fun verifyEmail(token: String): UserResponse {
        val verificationToken =
            userVerificationTokenRepository.findByToken(token)
                ?: throw VerificationTokenException("Verification token is invalid")

        if (verificationToken.usedAt != null) {
            throw VerificationTokenException("Verification token has already been used")
        }

        if (verificationToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw VerificationTokenException("Verification token has expired")
        }

        val userId = verificationToken.userId
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { VerificationTokenException("User for verification token not found") }

        user.status = UserStatus.ACTIVE
        user.emailVerified = true
        user.updatedAt = LocalDateTime.now()

        verificationToken.usedAt = LocalDateTime.now()

        return UserResponse.from(user)
    }
}
