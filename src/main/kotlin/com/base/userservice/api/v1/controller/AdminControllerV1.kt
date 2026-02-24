package com.base.userservice.api.v1.controller

import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.service.impl.UserService
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
class AdminControllerV1(
    private val userService: UserService,
) {
    @GetMapping("/users/{id}")
    fun getUserById(
        @PathVariable id: UUID,
    ): UserResponse = userService.findByIdWithRolesAndPermissions(id)

    @GetMapping("/users/username")
    fun getUserByName(
        @RequestParam username: String,
    ): UserResponse = userService.findByUsernameWithRolesAndPermissions(username)

    @PatchMapping("/users/{id}/status")
    fun changeUserStatus(
        @PathVariable id: UUID,
        @RequestParam status: UserStatus,
    ): UserResponse = userService.changeStatus(id, status)
}
