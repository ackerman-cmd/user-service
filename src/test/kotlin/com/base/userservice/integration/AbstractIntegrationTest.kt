package com.base.userservice.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName::class)
abstract class AbstractIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        var postgres: PostgreSQLContainer<Nothing> =
            PostgreSQLContainer<Nothing>("postgres:16.1").apply {
                withDatabaseName("user_service")
                withUsername("test")
                withPassword("test")
                withInitScript("db/init.sql")
            }
    }
}
