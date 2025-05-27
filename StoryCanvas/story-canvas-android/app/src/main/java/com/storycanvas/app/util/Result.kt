package com.storycanvas.app.util

/**
 * A generic class that holds a value or an error message.
 * This is used to represent network operation results in the repository layer.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()

    /**
     * Helper method to check if the result is successful
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Helper method to get the data if the result is successful
     * Returns null otherwise
     */
    fun getOrNull(): T? = (this as? Success)?.data
    
    /**
     * Helper method to get the error message if the result is an error
     * Returns null otherwise
     */
    fun errorMessageOrNull(): String? = (this as? Error)?.message
} 