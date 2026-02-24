package com.base.userservice.security

import com.base.userservice.domain.user.UserStatus
import com.base.userservice.repository.UserRepository
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

//        val disabled = user.status != UserStatus.ACTIVE || !user.emailVerified
        val locked = user.status == UserStatus.BLOCKED

        val roleNames = userRepository.findRolesByUsername(user.username)
        val permNames = userRepository.findPermissionsByUsername(user.username)

        val authorities =
            (roleNames + permNames)
                .distinct()
                .map { SimpleGrantedAuthority(it) }

        return org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.passwordHash)
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(locked)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }
}
