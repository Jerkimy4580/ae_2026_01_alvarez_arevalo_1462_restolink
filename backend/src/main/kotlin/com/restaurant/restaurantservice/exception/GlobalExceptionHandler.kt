package com.restaurant.restaurantservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(InvalidOrderStateException::class)
    fun handleInvalidOrderState(ex: InvalidOrderStateException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid order state")
        problem.title = "Bad request"
        return problem
    }

    @ExceptionHandler(AllergenConflictException::class)
    fun handleAllergenConflict(ex: AllergenConflictException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Allergen conflict")
        problem.title = "Allergen conflict"
        return problem
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation failed")
        problem.title = "Bad request"
        return problem
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResource(ex: DuplicateResourceException): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Resource already exists")
        problem.title = "Conflict"
        return problem
    }

    @ExceptionHandler(IllegalArgumentException::class, HttpMessageNotReadableException::class)
    fun handleBadRequest(ex: Exception): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
        problem.title = "Bad request"
        return problem
    }

    @ExceptionHandler(Exception::class)
    fun handleInternalServerError(ex: Exception): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Internal server error")
        problem.title = "Internal server error"
        return problem
    }
}
