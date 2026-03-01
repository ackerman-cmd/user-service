package com.base.userservice.service

import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.user.ChangePasswordCommand
import com.base.userservice.domain.user.UpdateProfileCommand
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.exeption.InvalidPasswordException
import com.base.userservice.exeption.UserNotFoundException
import com.base.userservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun findById(id: UUID): UserResponse =
        UserResponse.from(
            getById(id),
        )

    fun findByIdWithRoles(id: UUID): UserResponse {
        val user = getById(id)
        val roles = userRepository.findRolesByUsername(user.username)
        return UserResponse.fromWithRoles(user, roles)
    }

    fun findByIdWithRolesAndPermissions(id: UUID): UserResponse {
        val user = getById(id)
        val roles = userRepository.findRolesByUsername(user.username)
        val perms = userRepository.findPermissionsByUsername(user.username)
        return UserResponse.fromWithRolesAndPermissions(user, roles, perms)
    }

    fun findByUsername(name: String): UserResponse =
        UserResponse.from(
            getByUsername(name),
        )

    fun findByUsernameWithRoles(name: String): UserResponse {
        val user = getByUsername(name)
        val roles = userRepository.findRolesByUsername(user.username)
        return UserResponse.fromWithRoles(user, roles)
    }

    fun findByUsernameWithRolesAndPermissions(name: String): UserResponse {
        val user = getByUsername(name)
        val roles = userRepository.findRolesByUsername(user.username)
        val perms = userRepository.findPermissionsByUsername(user.username)
        return UserResponse.fromWithRolesAndPermissions(user, roles, perms)
    }

    @Transactional
    fun updateProfile(
        id: UUID,
        command: UpdateProfileCommand,
    ): UserResponse {
        val user = getById(id)
        user.firstName = command.firstName
        user.lastName = command.lastName
        val saved = userRepository.save(user)
        return UserResponse.from(saved)
    }

    @Transactional
    fun changePassword(
        id: UUID,
        command: ChangePasswordCommand,
    ) {
        val user = getById(id)

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
    ): UserResponse {
        val user = getById(id)
        user.status = status
        val saved = userRepository.save(user)
        return UserResponse.from(saved)
    }

    private fun getById(id: UUID): User =
        userRepository.findById(id).orElseThrow {
            UserNotFoundException("User with id: $id not found")
        }

    private fun getByUsername(username: String): User =
        userRepository.findByUsername(username)
            ?: throw UserNotFoundException("User with username: $username not found:")
}
