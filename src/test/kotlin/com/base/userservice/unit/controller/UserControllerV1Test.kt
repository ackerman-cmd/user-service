package com.base.userservice.unit.controller

import com.base.userservice.api.message.request.ChangePasswordRequest
import com.base.userservice.api.message.request.UpdateProfileRequest
import com.base.userservice.api.v1.controller.UserControllerV1
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.service.UserService
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class UserControllerV1Test {
    private val userService: UserService = mockk(relaxed = true)
    private val controller: UserControllerV1 = UserControllerV1(userService)

    @Test
    fun getUserById_delegatesToService() {
        val id = UUID.randomUUID()

        controller.getUserById(id)

        verify { userService.findById(id) }
    }

    @Test
    fun getUserByName_delegatesToService() {
        val username = "user"

        controller.getUserByName(username)

        verify { userService.findByUsername(username) }
    }

    @Test
    fun getCurrentUser_delegatesToService() {
        val jwt = buildJwt()

        controller.getCurrentUser(jwt)

        verify { userService.getCurrentUser(jwt) }
    }

    @Test
    fun changeOwnStatus_delegatesToService() {
        val jwt = buildJwt()

        controller.changeOwnStatus(jwt, UserStatus.INACTIVE)

        verify { userService.changeOwnStatus(jwt, UserStatus.INACTIVE) }
    }

    @Test
    fun updateProfile_delegatesToService() {
        val jwt = buildJwt()
        val request = UpdateProfileRequest(firstName = "New", lastName = "Name")

        controller.updateProfile(jwt, request)

        verify { userService.updateProfile(jwt, request) }
    }

    @Test
    fun changePassword_delegatesToService() {
        val jwt = buildJwt()
        val request = ChangePasswordRequest(currentPassword = "old", newPassword = "newPassword1")

        controller.changePassword(jwt, request)

        verify { userService.changePassword(jwt, request) }
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
