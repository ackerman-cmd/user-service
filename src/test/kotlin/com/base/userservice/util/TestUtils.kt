package com.base.userservice.util

import com.base.userservice.api.message.request.RegisterRequest
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.User
import com.base.userservice.domain.user.UserStatus
import java.time.LocalDateTime
import java.util.UUID

object TestUtils {
    fun createRegisterRequest(
        email: String = TestConstants.DEFAULT_EMAIL,
        username: String = TestConstants.DEFAULT_USERNAME,
        password: String = TestConstants.DEFAULT_PASSWORD,
        firstName: String? = "John",
        lastName: String? = "Doe",
    ): RegisterRequest =
        RegisterRequest(
            email = email,
            username = username,
            password = password,
            firstName = firstName,
            lastName = lastName,
        )

    fun createRegisterUserCommand(
        email: String = TestConstants.DEFAULT_EMAIL,
        username: String = TestConstants.DEFAULT_USERNAME,
        password: String = TestConstants.DEFAULT_PASSWORD,
        firstName: String? = "John",
        lastName: String? = "Doe",
    ): RegisterUserCommand =
        RegisterUserCommand(
            email = email,
            username = username,
            password = password,
            firstName = firstName,
            lastName = lastName,
        )

    fun user(
        id: UUID = UUID.randomUUID(),
        email: String,
        username: String,
        passwordHash: String = "hashed",
        status: UserStatus = UserStatus.ACTIVE,
        emailVerified: Boolean = true,
    ): User =
        User(
            id = id,
            email = email,
            username = username,
            passwordHash = passwordHash,
            firstName = "John",
            lastName = "Doe",
            status = status,
            emailVerified = emailVerified,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
