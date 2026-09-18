package com.pogotcghelper.app.data.network

import okhttp3.Interceptor
import okhttp3.Response

/** Adds the pokemontcg.io API key header when one is configured; a no-op otherwise. */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (apiKey.isBlank()) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("X-Api-Key", apiKey).build())
    }
}

/**
 * pokemontcg.io's anonymous (no API key) tier returns 5xx under rate-limit pressure rather
 * than a clean 429, especially when several requests fire in quick succession (e.g. tapping
 * through search results). Retrying idempotent GETs a couple of times smooths that over.
 */
class RetryOnServerErrorInterceptor(private val maxRetries: Int = 2) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var attempt = 0
        while (!response.isSuccessful && response.code in 500..599 && attempt < maxRetries) {
            response.close()
            attempt++
            Thread.sleep(attempt * 300L)
            response = chain.proceed(request)
        }
        return response
    }
}
