package com.base.userservice.api.v1.controller

import com.base.userservice.api.message.request.ChangePasswordRequest
import com.base.userservice.api.message.request.UpdateProfileRequest
import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "Users")
class UserControllerV1(
    private val userService: UserService,
) {
    @GetMapping("/me")
    @Operation(
        summary = "Получить профиль текущего пользователя",
        description = """
            Возвращает полный профиль аутентифицированного пользователя,
            включая назначенные роли и permissions.
            Идентификация по claim `user_id` из JWT.
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Профиль пользователя",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "JWT токен отсутствует или невалиден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun getCurrentUser(
        @Parameter(hidden = true) @AuthenticationPrincipal jwt: Jwt,
    ): UserResponse = userService.getCurrentUser(jwt)

    @PutMapping("/me/profile")
    @Operation(
        summary = "Обновить профиль текущего пользователя",
        description = """
            Обновляет имя и/или фамилию текущего пользователя.
            Поля со значением `null` не изменяются (partial update).
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Профиль обновлён",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "JWT токен отсутствует или невалиден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun updateProfile(
        @Parameter(hidden = true) @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): UserResponse = userService.updateProfile(jwt, request)

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Сменить пароль текущего пользователя",
        description = """
            Меняет пароль аутентифицированного пользователя.
            Требуется указать текущий пароль для подтверждения.
            Новый пароль хешируется через BCrypt.
        """,
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Пароль успешно изменён"),
        ApiResponse(
            responseCode = "400",
            description = "Текущий пароль неверен или новый пароль не прошёл валидацию",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "JWT токен отсутствует или невалиден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun changePassword(
        @Parameter(hidden = true) @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: ChangePasswordRequest,
    ) = userService.changePassword(jwt, request)

    @PatchMapping("/me/status")
    @Operation(
        summary = "Изменить статус текущего пользователя",
        description = """
            Позволяет пользователю изменить свой статус.
            Допустимые значения: `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING_VERIFICATION`.
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Статус изменён",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "JWT токен отсутствует или невалиден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun changeOwnStatus(
        @Parameter(hidden = true) @AuthenticationPrincipal jwt: Jwt,
        @Parameter(description = "Новый статус пользователя", required = true)
        @RequestParam status: UserStatus,
    ): UserResponse = userService.changeOwnStatus(jwt, status)

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить пользователя по ID",
        description = "Возвращает базовый профиль пользователя (без ролей и permissions).",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Пользователь найден",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "JWT токен отсутствует или невалиден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun getUserById(
        @Parameter(description = "UUID пользователя", required = true)
        @PathVariable id: UUID,
    ): UserResponse = userService.findById(id)

    @GetMapping("/username")
    @Operation(
        summary = "Получить пользователя по username",
        description = "Возвращает базовый профиль пользователя по имени (без ролей и permissions).",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Пользователь найден",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "JWT токен отсутствует или невалиден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun getUserByName(
        @Parameter(description = "Username пользователя", required = true, example = "john_doe")
        @RequestParam username: String,
    ): UserResponse = userService.findByUsername(username)
}
