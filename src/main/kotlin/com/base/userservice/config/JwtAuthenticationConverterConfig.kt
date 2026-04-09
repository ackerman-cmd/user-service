package com.base.userservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

@Configuration
class JwtAuthenticationConverterConfig {
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val scopeAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(
            Converter { jwt ->
                val fromRoles = authoritiesFromRolesClaim(jwt)
                val fromScopes = scopeAuthoritiesConverter.convert(jwt) ?: emptyList()
                (fromRoles + fromScopes).distinctBy { it.authority }
            },
        )
        return converter
    }

    private fun authoritiesFromRolesClaim(jwt: Jwt): List<GrantedAuthority> {
        jwt.getClaimAsStringList("roles")?.takeIf { it.isNotEmpty() }?.let { roles ->
            return roles.map { SimpleGrantedAuthority(it) }
        }
        val raw = jwt.claims["roles"] ?: return emptyList()
        val names =
            when (raw) {
                is Collection<*> -> raw.mapNotNull { it?.toString()?.takeIf(String::isNotEmpty) }
                is Array<*> -> raw.mapNotNull { it?.toString()?.takeIf(String::isNotEmpty) }
                is String -> listOf(raw)
                else -> emptyList()
            }
        return names.map { SimpleGrantedAuthority(it) }
    }
}
