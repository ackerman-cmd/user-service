package com.base.userservice.integration.service

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.util.TestUtils
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.repository.UserRepository
import com.base.userservice.repository.UserVerificationTokenRepository
import com.base.userservice.service.AuthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AuthServiceIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tokenRepository: UserVerificationTokenRepository

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
    fun `register and verify user`() {
        val command: RegisterUserCommand = TestUtils.createRegisterUserCommand()

        val registered = authService.register(command)

        val savedUser = userRepository.findByUsername(registered.username)
        assertNotNull(savedUser)
        assertEquals(UserStatus.PENDING_VERIFICATION, savedUser!!.status)

        val token = tokenRepository.findAll().firstOrNull()
        assertNotNull(token)

        val verified = authService.verifyEmail(token!!.token)

        val updatedUser = userRepository.findByUsername(verified.username)
        assertNotNull(updatedUser)
        assertEquals(UserStatus.ACTIVE, updatedUser!!.status)
    }
}
