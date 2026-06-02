package com.turkcell.core.network

class NetworkException(cause: Throwable) : RuntimeException("Network Error", cause)

class ApiException(
    val code: Int,
    val errorMessage: String?,
    cause: Throwable? = null,
) : RuntimeException("HTTP $code: $errorMessage", cause)
