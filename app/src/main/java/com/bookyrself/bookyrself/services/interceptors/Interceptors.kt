package com.bookyrself.bookyrself.services.interceptors

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response

class MockSearchResultsInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (chain.request().url().pathSegments().contains("users")) {
            chain.proceed(chain.request())
                    .newBuilder()
                    .code(500)
                    .protocol(Protocol.HTTP_2)
                    .addHeader("content-type", "application/json")
                    .build()
        } else {
            chain.proceed((chain.request()))
        }
    }
}