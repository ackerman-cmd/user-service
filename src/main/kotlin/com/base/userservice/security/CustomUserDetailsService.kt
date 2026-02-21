package com.base.userservice.security

import com.base.userservice.domain.user.UserStatus
import com.base.userservice.repository.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("User not found: $username")

        val authorities =
            (
                user.roles.map { SimpleGrantedAuthority(it.name) } +
                    user.allPermissions().map { SimpleGrantedAuthority(it) }
            )

        return org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.passwordHash)
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(user.status == UserStatus.BLOCKED)
            .credentialsExpired(false)
            .disabled(user.status == UserStatus.INACTIVE)
            .build()
    }
}
