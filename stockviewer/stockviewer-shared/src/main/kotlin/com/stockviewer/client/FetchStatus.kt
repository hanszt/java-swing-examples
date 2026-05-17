package com.stockviewer.client

/**
 * Represents the status of a fetch operation.
 */
sealed interface FetchStatus {
    data class Success<T : Any>(val data: T) : FetchStatus
    data class Error(val message: String) : FetchStatus
}