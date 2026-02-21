package com.base.userservice.exeption

class UserAlreadyExistsException(
    message: String,
) : RuntimeException(message)
