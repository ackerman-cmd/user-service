package com.base.userservice.security

import com.base.userservice.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.stereotype.Component

@Component
class CustomTokenCustomizer(
    private val userRepository: UserRepository,
) : OAuth2TokenCustomizer<JwtEncodingContext> {
    override fun customize(context: JwtEncodingContext) {
        val principal = context.getPrincipal<Authentication>()
        val user = userRepository.findByUsername(principal.name) ?: return

        val roles = userRepository.findRolesByUsername(user.username)
        val perms = userRepository.findPermissionsByUsername(user.username)

        context.claims
            .claim("user_id", user.id.toString())
            .claim("email", user.email)
            .claim("username", user.username)
            .claim("roles", roles)
            .claim("permissions", perms)
            .claim("status", user.status.name)
    }
}
