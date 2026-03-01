package com.base.userservice.api.v1.controller

import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Validated
class UserControllerV1(
    private val userService: UserService,
) {
    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: UUID,
    ): UserResponse = userService.findById(id)

    @GetMapping("/username")
    fun getUserByName(
        @RequestParam username: String,
    ): UserResponse = userService.findByUsername(username)

    @PatchMapping("/{id}/status")
    fun changeUserStatus(
        @PathVariable id: UUID,
        @RequestParam status: UserStatus,
    ): UserResponse = userService.changeStatus(id, status)
}
