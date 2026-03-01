package com.base.userservice.api.v1.controller

import com.base.userservice.api.message.request.RegisterRequest
import com.base.userservice.api.message.response.UserResponse
import com.base.userservice.domain.user.RegisterUserCommand
import com.base.userservice.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthControllerV1(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
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
    fun verify(
        @RequestParam token: String,
    ): UserResponse = authService.verifyEmail(token)
}
