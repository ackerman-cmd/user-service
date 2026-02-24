package com.base.userservice.api.v1.controller

import com.base.userservice.service.impl.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Validated
class UserControllerV1(
    private val userService: UserService,
)
