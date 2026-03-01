package com.base.userservice.integration.service

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.util.TestUtils
import com.base.userservice.domain.user.ChangePasswordCommand
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.repository.UserRepository
import com.base.userservice.service.AuthService
import com.base.userservice.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
        val userId: UUID = registered.id

        val command = ChangePasswordCommand(currentPassword = "Password123!", newPassword = "NewPassword123!")

        userService.changePassword(userId, command)

        val user = userRepository.findById(userId).orElse(null)
        assertNotNull(user)
    }
}
