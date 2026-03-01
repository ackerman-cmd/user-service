package com.base.userservice.integration.controller

import com.base.userservice.TestDataInsertionUtils
import com.base.userservice.TestDbCleaner
import com.base.userservice.api.message.request.RegisterRequest
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.util.TestConstants
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

class AuthControllerV1IntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

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
    fun `register returns 201 and user body`() {
        val request =
            RegisterRequest(
                email = TestConstants.DEFAULT_EMAIL,
                username = TestConstants.DEFAULT_USERNAME,
                password = TestConstants.DEFAULT_PASSWORD,
                firstName = "John",
                lastName = "Doe",
            )

        val result =
            mockMvc
                .post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isCreated() }
                }.andReturn()

        val responseBody = result.response.contentAsString
        // минимальная проверка, что тело не пустое
        assert(responseBody.isNotBlank())
    }
}
