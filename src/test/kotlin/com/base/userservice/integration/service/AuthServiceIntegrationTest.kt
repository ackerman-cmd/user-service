package com.base.userservice.integration.service

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.event.EmailVerificationEvent
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.repository.OutboxEventRepository
import com.base.userservice.repository.UserRepository
import com.base.userservice.repository.UserVerificationTokenRepository
import com.base.userservice.service.AuthService
import com.base.userservice.util.TestUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
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
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    fun `register creates user and saves outbox event`() {
        val command: RegisterUserCommand = TestUtils.createRegisterUserCommand()

        val registered = authService.register(command)

        val savedUser = userRepository.findByUsername(registered.username)
        assertNotNull(savedUser)
        assertEquals(UserStatus.PENDING_VERIFICATION, savedUser!!.status)

        val outboxEvents = outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc()
        assertEquals(1, outboxEvents.size)

        val outbox = outboxEvents.first()
        assertEquals("User", outbox.aggregateType)
        assertEquals(registered.id.toString(), outbox.aggregateId)
        assertEquals("EmailVerification", outbox.eventType)
        assertNull(outbox.sentAt)

        val payload = objectMapper.readValue(outbox.payload, EmailVerificationEvent::class.java)
        assertEquals(command.email, payload.email)
        assertEquals(command.username, payload.username)
        assertTrue(payload.verificationUrl.contains(payload.verificationToken))
    }

    @Test
    fun `register and verify user`() {
        val command: RegisterUserCommand = TestUtils.createRegisterUserCommand()

        val registered = authService.register(command)

        val outbox = outboxEventRepository.findAll().first { it.aggregateId == registered.id.toString() }
        val event = objectMapper.readValue(outbox.payload, EmailVerificationEvent::class.java)

        val verified = authService.verifyEmail(event.verificationToken)

        val updatedUser = userRepository.findByUsername(verified.username)
        assertNotNull(updatedUser)
        assertEquals(UserStatus.ACTIVE, updatedUser!!.status)
        assertTrue(updatedUser.emailVerified)
    }
}
