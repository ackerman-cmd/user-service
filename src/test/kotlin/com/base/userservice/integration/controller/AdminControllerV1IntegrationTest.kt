package com.base.userservice.integration.controller

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.domain.role.RoleType
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

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
        inserter.insertRoleIfAbsent(RoleType.ROLE_OPERATOR)
        inserter.insertRoleIfAbsent(RoleType.ROLE_ADMIN)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can get available roles`() {
        mockMvc
            .get("/api/v1/admin/roles")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)) }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can assign roles to user`() {
        val registered =
            authService.register(
                TestUtils.createRegisterUserCommand(username = "operator-user"),
            )
        mockMvc
            .patch("/api/v1/admin/users/${registered.id}/roles") {
                content = """{"roles": ["ROLE_USER", "ROLE_OPERATOR"]}"""
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.roles") { value(org.hamcrest.Matchers.hasItems("ROLE_USER", "ROLE_OPERATOR")) }
            }
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
