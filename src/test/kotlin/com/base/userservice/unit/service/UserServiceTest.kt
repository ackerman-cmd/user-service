package com.base.userservice.unit.service

import com.base.userservice.domain.user.ChangePasswordCommand
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.exeption.InvalidPasswordException
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
import java.util.Optional
import java.util.UUID

class UserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val service = UserService(userRepository, passwordEncoder)

    @Test
    fun `changePassword updates hash when current password matches`() {
        val id = UUID.randomUUID()
        val user = TestUtils.user(email = "e@example.com", username = "u", passwordHash = "old")
        every { userRepository.findById(id) } returns Optional.of(user)
        every { passwordEncoder.matches("current", "old") } returns true
        every { passwordEncoder.encode("new") } returns "new-hash"

        val command = ChangePasswordCommand(currentPassword = "current", newPassword = "new")

        service.changePassword(id, command)

        val slot: CapturingSlot<com.base.userservice.domain.user.User> = slot()
        verify { userRepository.save(capture(slot)) }
        assertEquals("new-hash", slot.captured.passwordHash)
    }

    @Test
    fun `changePassword throws when current password invalid`() {
        val id = UUID.randomUUID()
        val user = TestUtils.user(email = "e@example.com", username = "u", passwordHash = "old")
        every { userRepository.findById(id) } returns Optional.of(user)
        every { passwordEncoder.matches(any(), any()) } returns false

        val command = ChangePasswordCommand(currentPassword = "wrong", newPassword = "new")

        assertThrows<InvalidPasswordException> {
            service.changePassword(id, command)
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
}
