package com.base.userservice.api.v1.controller

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
import org.springframework.http.ProblemDetail
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
class AdminControllerV1(
    private val userService: UserService,
) {
    @GetMapping("/users/{id}")
    @Operation(
        summary = "Получить пользователя по ID (admin)",
        description = """
            Возвращает полный профиль пользователя, включая роли и permissions.
            Доступно только пользователям с ролью `ROLE_ADMIN`.
        """,
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
            responseCode = "403",
            description = "Нет прав администратора",
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
    ): UserResponse = userService.findByIdWithRolesAndPermissions(id)

    @GetMapping("/users/username")
    @Operation(
        summary = "Получить пользователя по username (admin)",
        description = """
            Возвращает полный профиль пользователя по username, включая роли и permissions.
            Доступно только пользователям с ролью `ROLE_ADMIN`.
        """,
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
            responseCode = "403",
            description = "Нет прав администратора",
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
    ): UserResponse = userService.findByUsernameWithRolesAndPermissions(username)

    @PatchMapping("/users/{id}/status")
    @Operation(
        summary = "Изменить статус пользователя (admin)",
        description = """
            Позволяет администратору изменить статус любого пользователя.
            Используется для блокировки, разблокировки или деактивации аккаунтов.
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
        ApiResponse(
            responseCode = "403",
            description = "Нет прав администратора",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun changeUserStatus(
        @Parameter(description = "UUID пользователя", required = true)
        @PathVariable id: UUID,
        @Parameter(description = "Новый статус пользователя", required = true)
        @RequestParam status: UserStatus,
    ): UserResponse = userService.changeStatus(id, status)
}
