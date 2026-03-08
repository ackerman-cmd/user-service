package com.base.userservice.api.v1.controller

import com.base.userservice.api.message.request.RegisterRequest
import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
class AuthControllerV1(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(
        summary = "Регистрация нового пользователя",
        description = """
            Создаёт нового пользователя со статусом `PENDING_VERIFICATION` и ролью `ROLE_USER`.
            Генерирует токен верификации email (действителен 24 часа).
            Аутентификация не требуется.
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Пользователь успешно зарегистрирован",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации входных данных",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "Пользователь с таким email или username уже существует",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): UserResponse =
        authService.register(
            RegisterUserCommand(
                email = request.email,
                username = request.username,
                password = request.password,
                firstName = request.firstName,
                lastName = request.lastName,
            ),
        )

    @GetMapping("/verify")
    @SecurityRequirements
    @Operation(
        summary = "Верификация email по токену",
        description = """
            Подтверждает email пользователя. После успешной верификации статус пользователя
            меняется на `ACTIVE`, флаг `emailVerified` устанавливается в `true`.
            Токен одноразовый и действителен 24 часа с момента регистрации.
            Аутентификация не требуется.
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Email успешно подтверждён",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Токен невалиден, истёк или уже был использован",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun verify(
        @RequestParam token: String,
    ): UserResponse = authService.verifyEmail(token)
}
