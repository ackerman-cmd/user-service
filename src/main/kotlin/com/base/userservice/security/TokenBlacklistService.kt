package com.base.userservice.security

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class TokenBlacklistService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        private const val BLACKLIST_PREFIX = "token:blacklist:"
    }

    fun revoke(
        jti: String,
        expiresAt: Instant,
    ) {
        val ttl = Duration.between(Instant.now(), expiresAt)
        if (ttl.isNegative) return
        redisTemplate.opsForValue().set("$BLACKLIST_PREFIX$jti", "revoked", ttl)
    }

    fun isRevoked(jti: String): Boolean = redisTemplate.hasKey("$BLACKLIST_PREFIX$jti") == true
}
