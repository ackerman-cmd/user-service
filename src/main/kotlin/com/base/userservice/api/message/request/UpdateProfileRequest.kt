package com.base.userservice.api.message.request

import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(max = 64)
    val firstName: String? = null,
    @field:Size(max = 64)
    val lastName: String? = null,
)
