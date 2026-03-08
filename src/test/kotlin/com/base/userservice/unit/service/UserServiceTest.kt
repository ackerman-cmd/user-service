package com.base.userservice.unit.service

import com.base.userservice.api.message.request.ChangePasswordRequest
import com.base.userservice.api.message.request.UpdateProfileRequest
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.exception.InvalidPasswordException
import com.base.userservice.exception.UserNotFoundException
import com.base.userservice.repository.UserRepository
import com.base.userservice.service.UserService
import com.base.userservice.util.TestUtils
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.Optional
import java.util.UUID

class UserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val service = UserService(userRepository, passwordEncoder)

    @Test
    fun `findById throws when user not found`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            service.findById(id)
        }
    }

    @Test
    fun `findById returns user response`() {
        val id = UUID.randomUUID()
        val user = TestUtils.user(id = id, email = "e@example.com", username = "u")
        every { userRepository.findById(id) } returns Optional.of(user)

        val result = service.findById(id)

        assertEquals(id, result.id)
        assertEquals("u", result.username)
    }

    @Test
    fun `updateProfile updates first and last name`() {
        val id = UUID.randomUUID()
        val jwt = buildJwt(id)
        val user = TestUtils.user(id = id, email = "e@example.com", username = "u")
        every { userRepository.findById(id) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val result = service.updateProfile(jwt, UpdateProfileRequest(firstName = "Alice", lastName = "Smith"))

        assertEquals("Alice", result.firstName)
        assertEquals("Smith", result.lastName)
    }

    @Test
    fun `updateProfile keeps existing values when fields are null`() {
        val id = UUID.randomUUID()
        val jwt = buildJwt(id)
        val user = TestUtils.user(id = id, email = "e@example.com", username = "u")
        every { userRepository.findById(id) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val result = service.updateProfile(jwt, UpdateProfileRequest(firstName = null, lastName = null))

        assertEquals("John", result.firstName)
        assertEquals("Doe", result.lastName)
    }

    @Test
    fun `changePassword updates hash when current password matches`() {
        val id = UUID.randomUUID()
        val jwt = buildJwt(id)
        val user = TestUtils.user(email = "e@example.com", username = "u", passwordHash = "old")
        every { userRepository.findById(id) } returns Optional.of(user)
        every { passwordEncoder.matches("current", "old") } returns true
        every { passwordEncoder.encode("new") } returns "new-hash"
        every { userRepository.save(any()) } answers { firstArg() }

        service.changePassword(jwt, ChangePasswordRequest(currentPassword = "current", newPassword = "new"))

        val slot: CapturingSlot<com.base.userservice.domain.user.User> = slot()
        verify { userRepository.save(capture(slot)) }
        assertEquals("new-hash", slot.captured.passwordHash)
    }

    @Test
    fun `changePassword throws when current password invalid`() {
        val id = UUID.randomUUID()
        val jwt = buildJwt(id)
        val user = TestUtils.user(email = "e@example.com", username = "u", passwordHash = "old")
        every { userRepository.findById(id) } returns Optional.of(user)
        every { passwordEncoder.matches(any(), any()) } returns false

        assertThrows<InvalidPasswordException> {
            service.changePassword(jwt, ChangePasswordRequest(currentPassword = "wrong", newPassword = "new"))
        }
    }

    @Test
    fun `changeStatus updates status`() {
        val id = UUID.randomUUID()
        val user = TestUtils.user(email = "e@example.com", username = "u", status = UserStatus.ACTIVE)
        every { userRepository.findById(id) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val result = service.changeStatus(id, UserStatus.BLOCKED)

        assertEquals(UserStatus.BLOCKED, result.status)
    }

    private fun buildJwt(userId: UUID = UUID.randomUUID()): Jwt =
        Jwt
            .withTokenValue("token")
            .header("alg", "RS256")
            .claim("user_id", userId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build()
}
