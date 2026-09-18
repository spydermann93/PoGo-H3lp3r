package com.pogotcghelper.app.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * TCGdex has no documented anonymous-tier throttling, but retrying idempotent GETs a
 * couple of times on a 5xx is cheap insurance against any transient failure.
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
