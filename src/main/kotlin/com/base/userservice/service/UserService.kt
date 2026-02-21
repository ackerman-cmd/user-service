package com.base.userservice.service

import com.base.userservice.domain.user.ChangePasswordCommand
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.UpdateProfileCommand
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.exeption.InvalidPasswordException
import com.base.userservice.exeption.UserAlreadyExistsException
import com.base.userservice.exeption.UserNotFoundException
import com.base.userservice.repository.role.RoleRepository
import com.base.userservice.repository.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(command: RegisterUserCommand): User {
        if (userRepository.existsByEmail(command.email)) {
            throw UserAlreadyExistsException("Email already in use: ${command.email}")
        }
        if (userRepository.existsByUsername(command.username)) {
            throw UserAlreadyExistsException("Username already in use: ${command.username}")
        }

        val defaultRole =
            roleRepository.findByName("ROLE_USER")
                ?: throw IllegalStateException("Default role ROLE_USER not found")

        val user =
            User(
                email = command.email,
                username = command.username,
                passwordHash = passwordEncoder.encode(command.password)!!,
                firstName = command.firstName,
                lastName = command.lastName,
                verificationToken = UUID.randomUUID().toString(),
            )

        user.roles.add(defaultRole)

        return userRepository.save(user)
    }

    @Transactional
    fun verifyEmail(token: String): User {
        val user =
            userRepository.findByVerificationToken(token)
                ?: throw UserNotFoundException("Invalid verification token")

        user.emailVerified = true
        user.verificationToken = null
        user.status = UserStatus.ACTIVE

        return userRepository.save(user)
    }

    fun findById(id: UUID): User =
        userRepository.findById(id).orElseThrow {
            UserNotFoundException("User not found: $id")
        }

    fun findByUsername(username: String): User =
        userRepository.findByUsername(username)
            ?: throw UserNotFoundException("User not found: $username")

    @Transactional
    fun updateProfile(
        id: UUID,
        command: UpdateProfileCommand,
    ): User {
        val user = findById(id)
        user.firstName = command.firstName
        user.lastName = command.lastName
        return userRepository.save(user)
    }

    @Transactional
    fun changePassword(
        id: UUID,
        command: ChangePasswordCommand,
    ) {
        val user = findById(id)

        if (!passwordEncoder.matches(command.currentPassword, user.passwordHash)) {
            throw InvalidPasswordException("Current password is incorrect")
        }

        user.passwordHash = passwordEncoder.encode(command.newPassword)!!
        userRepository.save(user)
    }

    @Transactional
    fun changeStatus(
        id: UUID,
        status: UserStatus,
    ): User {
        val user = findById(id)
        user.status = status
        return userRepository.save(user)
    }
}
