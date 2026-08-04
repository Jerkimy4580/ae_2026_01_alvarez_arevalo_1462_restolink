package com.restaurant.restaurantservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")
        problem.title = "Resource not found"
        return problem
    }

    @ExceptionHandler(ForbiddenAccessException::class, AccessDeniedException::class)
    fun handleForbidden(ex: RuntimeException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.message ?: "Access denied")
        problem.title = "Forbidden"
        return problem
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException::class)
    fun handleAuthentication(ex: AuthenticationCredentialsNotFoundException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.message ?: "Authentication required")
        problem.title = "Unauthorized"
        return problem
    }

    @ExceptionHandler(InvalidOrderStatusException::class)
    fun handleInvalidStatus(ex: InvalidOrderStatusException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid order status")
        problem.title = "Bad request"
        return problem
    }

    @ExceptionHandler(IllegalArgumentException::class, HttpMessageNotReadableException::class)
    fun handleBadRequest(ex: Exception): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
        problem.title = "Bad request"
        return problem
    }
}
