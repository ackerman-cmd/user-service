package com.base.userservice.api.v1.controller

import com.base.userservice.api.message.request.AssignRolesRequest
import com.base.userservice.api.message.response.RoleInfoResponse
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
import org.springframework.http.ProblemDetail
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
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
    @GetMapping("/roles")
    @Operation(
        summary = "Список доступных ролей",
        description = """
            Возвращает все роли системы для выбора при назначении пользователю.
            Доступно только пользователям с ролью `ROLE_ADMIN`.
        """,
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Список ролей"),
        ApiResponse(
            responseCode = "403",
            description = "Нет прав администратора",
            content = [Content(schema = Schema(implementation = ProblemDetail::class))],
        ),
    )
    fun getAvailableRoles(): List<RoleInfoResponse> = userService.getAvailableRoles()

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

    @PatchMapping("/users/{id}/roles")
    @Operation(
        summary = "Назначить роли пользователю (admin)",
        description = """
            Заменяет текущие роли пользователя на переданный список.
            Допустимые значения: ROLE_USER, ROLE_OPERATOR, ROLE_ADMIN.
            После изменения ролей событие синхронизируется в другие сервисы (user-sync).
        """,
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Роли обновлены",
            content = [Content(schema = Schema(implementation = UserResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Некорректный список ролей",
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
    fun assignRoles(
        @Parameter(description = "UUID пользователя", required = true)
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignRolesRequest,
    ): UserResponse = userService.assignRoles(id, request.roles)

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
