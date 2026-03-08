package com.base.userservice.integration.controller

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.service.AuthService
import com.base.userservice.util.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

class AdminControllerV1IntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

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
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can get user by id`() {
        val registered =
            authService.register(
                TestUtils
                    .createRegisterUserCommand(username = "admin-user"),
            )

        mockMvc
            .get("/api/v1/admin/users/{id}", registered.id)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can change user status`() {
        val registered =
            authService.register(
                TestUtils
                    .createRegisterUserCommand(username = "admin-status"),
            )

        mockMvc
            .patch("/api/v1/admin/users/{id}/status", registered.id) {
                param("status", UserStatus.BLOCKED.name)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }
    }
}
