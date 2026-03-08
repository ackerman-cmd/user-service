package com.base.userservice.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object TokenHasher {
    private val secureRandom = SecureRandom()
    private val urlEncoder = Base64.getUrlEncoder().withoutPadding()

    fun generate(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return urlEncoder.encodeToString(bytes)
    }

    fun hash(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return urlEncoder.encodeToString(digest.digest(rawToken.toByteArray(Charsets.UTF_8)))
    }
}
