package com.base.userservice.unit.controller

import com.base.userservice.TestDbCleaner
import com.base.userservice.api.v1.controller.UserControllerV1
import com.base.userservice.domain.user.UserStatus
import com.base.userservice.service.UserService
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class UserControllerV1Test {
    private  val userService: UserService =  mockk(relaxed = true)
    private  val controller: UserControllerV1 = UserControllerV1(userService)

    @Test
    fun getUserById_delegatesToService() {
        val id = UUID.randomUUID()

        controller.getUserById(id)

        verify { userService.findById(id) }
    }

    @Test
    fun getUserByName_delegatesToService() {
        val username = "user"

        controller.getUserByName(username)

        verify { userService.findByUsername(username) }
    }

    @Test
    fun changeUserStatus_delegatesToService() {
        val id = UUID.randomUUID()
        val status = UserStatus.BLOCKED

        controller.changeUserStatus(id, status)

        verify { userService.changeStatus(id, status) }
    }
}
