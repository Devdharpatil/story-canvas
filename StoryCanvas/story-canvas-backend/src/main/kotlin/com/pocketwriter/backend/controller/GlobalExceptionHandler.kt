package com.pocketwriter.backend.controller // Or com.pocketwriter.backend.advice

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.pocketwriter.backend.dto.ErrorResponseDTO
import com.pocketwriter.backend.exception.ResourceNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice // Intercepts exceptions across all controllers
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message,
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class) // Handles @Valid DTO validation failures
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponseDTO> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors,
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Malformed JSON Request",
            message = "Request body is malformed or missing required fields. Detail: ${ex.mostSpecificCause.message}",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [JsonParseException::class, JsonProcessingException::class])
    fun handleJsonParsingExceptions(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid JSON Format",
            message = "The provided JSON is invalid or malformed: ${ex.message}",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Argument",
            message = ex.message ?: "An invalid argument was provided",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    // A more generic handler for other unhandled exceptions
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponseDTO> {
        // Log the full exception for debugging on the server
        logger.error("Unhandled exception occurred", ex)
        val errorResponse = ErrorResponseDTO(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please try again later.",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}