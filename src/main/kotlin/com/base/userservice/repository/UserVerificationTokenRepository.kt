package com.base.userservice.repository

import com.base.userservice.domain.user.UserVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserVerificationTokenRepository : JpaRepository<UserVerificationToken, UUID> {
    fun findByToken(token: String): UserVerificationToken?
}
