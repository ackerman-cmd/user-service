package com.base.userservice.api.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginPageController(
    @Value("\${app.front-url}") private val frontUrl: String,
) {
    @GetMapping("/login", produces = [MediaType.TEXT_HTML_VALUE])
    fun loginPage(
        @RequestParam(required = false) error: String?,
        request: HttpServletRequest,
    ): ResponseEntity<String> {
        var html =
            ClassPathResource("templates/login.html")
                .inputStream
                .bufferedReader()
                .readText()

        html = html.replace("\${frontUrl}", frontUrl)

        val csrf = request.getAttribute(CsrfToken::class.java.name) as? CsrfToken
        val csrfField =
            if (csrf != null) {
                """<input type="hidden" name="${csrf.parameterName}" value="${csrf.token}"/>"""
            } else {
                ""
            }
        html = html.replace("<!-- CSRF_PLACEHOLDER -->", csrfField)

        val errorBlock =
            if (error != null) {
                """
                <div class="alert">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <circle cx="8" cy="8" r="7" stroke="#a8071a" stroke-width="1.5"/>
                        <path d="M8 4.5v4" stroke="#a8071a" stroke-width="1.5" stroke-linecap="round"/>
                        <circle cx="8" cy="11" r="0.75" fill="#a8071a"/>
                    </svg>
                    <span>Неверное имя пользователя или пароль</span>
                </div>
                """.trimIndent()
            } else {
                ""
            }
        html = html.replace("<!-- ERROR_PLACEHOLDER -->", errorBlock)

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html)
    }
}
