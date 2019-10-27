package com.bookyrself.bookyrself.services.interceptors

import com.bookyrself.bookyrself.data.serverModels.SearchResponseEvents.User
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.ByteArrayInputStream
import kotlin.random.Random

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