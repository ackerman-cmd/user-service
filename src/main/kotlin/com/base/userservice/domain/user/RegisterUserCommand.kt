package com.base.userservice.domain.user

data class RegisterUserCommand(
    val email: String,
    val username: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
)
