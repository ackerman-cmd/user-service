package com.base.userservice.unit.service

import com.base.userservice.util.TestUtils
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.repository.UserRepository
import com.base.userservice.security.CustomUserDetailsService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {
    private val userRepository: UserRepository = mockk()
    private val service = CustomUserDetailsService(userRepository)

    @Test
    fun `user is disabled when not active or email not verified`() {
        val user =
            TestUtils.user(
                email = "e@example.com",
                username = "u",
                status = UserStatus.PENDING_VERIFICATION,
                emailVerified = false,
            )

        every { userRepository.findByUsername("u") } returns user
        every { userRepository.findRolesByUsername(any()) } returns emptyList()
        every { userRepository.findPermissionsByUsername(any()) } returns emptyList()

        val details = service.loadUserByUsername("u")

        assertFalse(details.isEnabled)
        assertFalse(details.isAccountNonLocked)
    }

    @Test
    fun `throws when user not found`() {
        every { userRepository.findByUsername("missing") } returns null

        assertThrows<UsernameNotFoundException> {
            service.loadUserByUsername("missing")
        }
    }
}
