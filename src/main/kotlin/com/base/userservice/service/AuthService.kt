package com.base.userservice.service

import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.outbox.OutboxEventType
import com.base.userservice.domain.role.RoleType
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.domain.user.UserVerificationToken
import com.base.userservice.event.EmailVerificationEvent
import com.base.userservice.event.EventPublisher
import com.base.userservice.exception.UserAlreadyExistsException
import com.base.userservice.exception.VerificationTokenException
import com.base.userservice.repository.RoleRepository
import com.base.userservice.repository.UserRepository
import com.base.userservice.repository.UserVerificationTokenRepository
import com.base.userservice.security.TokenHasher
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userVerificationTokenRepository: UserVerificationTokenRepository,
    private val eventPublisher: EventPublisher,
    @Value("\${app.front-url}") private val frontUrl: String,
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

        val rawToken = TokenHasher.generate()
        val tokenHash = TokenHasher.hash(rawToken)

        val verificationToken =
            UserVerificationToken(
                id = UUID.randomUUID(),
                userId = requireNotNull(saved.id),
                token = tokenHash,
                expiresAt = LocalDateTime.now().plusDays(1),
            )

        userVerificationTokenRepository.save(verificationToken)

        eventPublisher.publishEmailVerification(
            EmailVerificationEvent(
                userId = saved.id,
                email = saved.email,
                username = saved.username,
                verificationToken = rawToken,
                verificationUrl = "$frontUrl/auth/verify?token=$rawToken",
            ),
        )

        eventPublisher.publishUserSync(saved, OutboxEventType.USER_REGISTERED)

        return UserResponse.from(saved)
    }

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
        val tokenHash = TokenHasher.hash(token)
        val verificationToken =
            userVerificationTokenRepository.findByToken(tokenHash)
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

        eventPublisher.publishUserSync(user, OutboxEventType.USER_EMAIL_VERIFIED)

        return UserResponse.from(user)
    }
}
