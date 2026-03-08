package com.base.userservice.integration.service

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.api.message.request.ChangePasswordRequest
import com.base.userservice.api.message.request.UpdateProfileRequest
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.repository.UserRepository
import com.base.userservice.service.AuthService
import com.base.userservice.service.UserService
import com.base.userservice.util.TestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class UserServiceIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
        inserter.insertUser()
    }

    @Test
    fun `change status of existing user`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand())
        val userId: UUID = registered.id

        val updated = userService.changeStatus(userId, UserStatus.BLOCKED)

        assertEquals(UserStatus.BLOCKED, updated.status)

        val fromDb = userRepository.findById(userId).orElse(null)
        assertNotNull(fromDb)
        assertEquals(UserStatus.BLOCKED, fromDb!!.status)
    }

    @Test
    fun `change password of existing user`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "user2"))
        val jwt = buildJwt(registered.id)

        userService.changePassword(jwt, ChangePasswordRequest("Password123!", "NewPassword123!"))

        val user = userRepository.findById(registered.id).orElse(null)
        assertNotNull(user)
    }

    @Test
    fun `update profile of existing user`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "user3"))
        val jwt = buildJwt(registered.id)

        val updated = userService.updateProfile(jwt, UpdateProfileRequest("Updated", "Name"))

        assertEquals("Updated", updated.firstName)
        assertEquals("Name", updated.lastName)

        val fromDb = userRepository.findById(registered.id).orElse(null)
        assertNotNull(fromDb)
        assertEquals("Updated", fromDb!!.firstName)
        assertEquals("Name", fromDb.lastName)
    }

    @Test
    fun `update profile keeps existing values when fields are null`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "user4"))
        val jwt = buildJwt(registered.id)

        val updated = userService.updateProfile(jwt, UpdateProfileRequest(null, null))

        assertEquals("John", updated.firstName)
        assertEquals("Doe", updated.lastName)
    }

    private fun buildJwt(userId: UUID): Jwt =
        Jwt
            .withTokenValue("token")
            .header("alg", "RS256")
            .claim("user_id", userId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build()
}
