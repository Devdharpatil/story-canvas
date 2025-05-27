package com.pocketwriter.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND) // Automatically sets the HTTP status if this exception is unhandled
class ResourceNotFoundException(message: String) : RuntimeException(message)