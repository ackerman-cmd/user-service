package com.base.userservice.exception

class UserAlreadyExistsException(
    message: String,
) : RuntimeException(message)
