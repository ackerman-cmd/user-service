package com.base.userservice.exeption

class UserNotFoundException(
    message: String,
) : RuntimeException(message)
