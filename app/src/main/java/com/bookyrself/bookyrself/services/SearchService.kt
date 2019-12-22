package com.bookyrself.bookyrself.services

import com.bookyrself.bookyrself.data.serverModels.SearchRequest.RequestBody
import com.bookyrself.bookyrself.data.serverModels.SearchResponseEvents.SearchResponse2
import com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers.SearchResponseUsers

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by benmedcalf on 8/11/17.
 */
class SearchService {

    val api: SearchAPI
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL_ES)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(SearchAPI::class.java)
        }

    interface SearchAPI {
        @POST("/events/_search")
        fun executeEventsSearch(@Body query: RequestBody): Call<SearchResponse2>

        @POST("/users/_search")
        fun executeUsersSearch(@Body query: RequestBody): Call<SearchResponseUsers>
    }

    companion object {
        private val BASE_URL_ES = "https://dogwood-9512546.us-east-1.bonsaisearch.net"
    }
}