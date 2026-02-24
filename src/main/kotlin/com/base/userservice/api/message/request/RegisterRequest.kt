package com.base.userservice.api.message.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    @field:Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    val username: String,
    @field:NotBlank
    @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
)
