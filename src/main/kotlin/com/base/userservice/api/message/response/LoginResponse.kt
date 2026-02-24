package com.base.userservice.api.message.response

import java.time.Instant

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
)
