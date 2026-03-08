package com.base.userservice.integration.controller

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.api.message.request.ChangePasswordRequest
import com.base.userservice.api.message.request.UpdateProfileRequest
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.service.AuthService
import com.base.userservice.util.TestUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.put

class UserControllerV1IntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

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
    fun `get user by id returns 200`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand())

        mockMvc
            .get("/api/v1/users/{id}", registered.id) {
                with(jwt())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `get current user returns 200 with roles`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "me-user"))

        mockMvc
            .get("/api/v1/users/me") {
                with(jwt().jwt { it.claim("user_id", registered.id.toString()) })
            }.andExpect {
                status { isOk() }
                jsonPath("$.username") { value("me-user") }
                jsonPath("$.roles") { isNotEmpty() }
            }
    }

    @Test
    fun `update profile returns 200`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "profile-user"))
        val request = UpdateProfileRequest(firstName = "Updated", lastName = "Name")

        mockMvc
            .put("/api/v1/users/me/profile") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                with(jwt().jwt { it.claim("user_id", registered.id.toString()) })
            }.andExpect {
                status { isOk() }
                jsonPath("$.firstName") { value("Updated") }
                jsonPath("$.lastName") { value("Name") }
            }
    }

    @Test
    fun `change own status returns 200`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "status-user"))

        mockMvc
            .patch("/api/v1/users/me/status") {
                param("status", UserStatus.INACTIVE.name)
                with(jwt().jwt { it.claim("user_id", registered.id.toString()) })
            }.andExpect {
                status { isOk() }
                jsonPath("$.status") { value("INACTIVE") }
            }
    }

    @Test
    fun `change password returns 204`() {
        val registered = authService.register(TestUtils.createRegisterUserCommand(username = "pw-user"))
        val request = ChangePasswordRequest(currentPassword = "Password123!", newPassword = "NewPassword123!")

        mockMvc
            .patch("/api/v1/users/me/password") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                with(jwt().jwt { it.claim("user_id", registered.id.toString()) })
            }.andExpect {
                status { isNoContent() }
            }
    }
}
