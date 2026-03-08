package com.base.userservice.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${app.base-url}") private val baseUrl: String,
) {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("User Service API")
                    .version("1.0.0")
                    .description(
                        """
                        Централизованный сервис авторизации, регистрации и управления профилями пользователей.

                        ## Аутентификация

                        Сервис является **OAuth 2.0 Authorization Server** и поддерживает два типа клиентов:

                        - **SPA Client** — Authorization Code + PKCE для фронтенд-приложений
                        - **Service Client** — Client Credentials для межсервисного взаимодействия

                        ## Авторизация эндпоинтов

                        | Группа | Доступ |
                        |--------|--------|
                        | Auth (`/api/v1/auth/**`) | Публичный — регистрация и верификация email |
                        | Users (`/api/v1/users/**`) | Требуется Bearer JWT токен |
                        | Admin (`/api/v1/admin/**`) | Требуется роль `ADMIN` |

                        ## Формат ошибок

                        Все ошибки возвращаются в формате **RFC 7807 Problem Detail**.
                        """.trimIndent(),
                    ).contact(
                        Contact()
                            .name("User Service Team"),
                    ),
            ).servers(
                listOf(
                    Server().url(baseUrl).description("Current environment"),
                ),
            ).tags(
                listOf(
                    Tag()
                        .name("Auth")
                        .description("Регистрация пользователей и верификация email"),
                    Tag()
                        .name("Users")
                        .description("Управление профилем текущего пользователя и просмотр профилей"),
                    Tag()
                        .name("Admin")
                        .description("Административное управление пользователями (требуется ROLE_ADMIN)"),
                ),
            ).components(
                Components()
                    .addSecuritySchemes(
                        "bearer-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description(
                                "JWT access token, полученный через OAuth2 Authorization Code (PKCE) " +
                                    "или Client Credentials flow. " +
                                    "Содержит claims: `user_id`, `email`, `roles`, `permissions`, `status`.",
                            ),
                    ).addSecuritySchemes(
                        "oauth2",
                        SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                OAuthFlows()
                                    .authorizationCode(
                                        OAuthFlow()
                                            .authorizationUrl("$baseUrl/oauth2/authorize")
                                            .tokenUrl("$baseUrl/oauth2/token")
                                            .also {
                                                it.addExtension("x-usePkceWithAuthorizationCodeGrant", true)
                                            },
                                    ).clientCredentials(
                                        OAuthFlow()
                                            .tokenUrl("$baseUrl/oauth2/token"),
                                    ),
                            ),
                    ),
            ).addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
}
