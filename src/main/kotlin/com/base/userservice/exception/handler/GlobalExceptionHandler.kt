package com.base.userservice.exception.handler

import com.base.userservice.exception.InvalidPasswordException
import com.base.userservice.exception.UserAlreadyExistsException
import com.base.userservice.exception.UserNotFoundException
import com.base.userservice.exception.VerificationTokenException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ProblemDetail {
        log.warn("User not found: {}", ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "User not found")
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException): ProblemDetail {
        log.warn("User already exists: {}", ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "User already exists")
    }

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPassword(ex: InvalidPasswordException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid password")

    @ExceptionHandler(VerificationTokenException::class)
    fun handleVerificationToken(ex: VerificationTokenException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Invalid or expired verification token",
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val errors =
            ex.bindingResult?.fieldErrors?.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }
        return ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed")
            .also { it.setProperty("errors", errors) }
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ProblemDetail {
        log.error("Illegal state: {}", ex.message, ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Illegal state")
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ProblemDetail {
        log.error("Unhandled exception: {}", ex.message, ex)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.message ?: "Internal server error",
        )
    }
}
