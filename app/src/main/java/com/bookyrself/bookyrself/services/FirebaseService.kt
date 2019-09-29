package com.bookyrself.bookyrself.services

import com.bookyrself.bookyrself.data.ServerModels.EventCreationResponse
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.Host
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.ServerModels.User.User
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*

object FirebaseService {

    private const val BASE_URL_BOOKYRSELF_FIREBASE = "https://bookyrself-staging.firebaseio.com/"

    val instance: FirebaseApi by lazy {

        // Create the logging interceptor
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_BOOKYRSELF_FIREBASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        retrofit.create(FirebaseApi::class.java)
    }

    interface FirebaseApi {
        @GET("/events/{id}.json")
        fun getEventData(@Path("id") eventId: String): Flowable<EventDetail>

        @GET("/users/{id}.json")
        fun getUserDetails(@Path("id") userId: String): Flowable<User>

        @GET("/users/{id}/events.json")
        fun getUsersEventInvites(@Path("id") userId: String): Flowable<HashMap<String, EventInviteInfo>>

        @GET("/users/{id}/contacts.json")
        fun getUserContacts(@Path("id") userId: String): Flowable<HashMap<String, Boolean>>

        @PUT("/users/{userId}.json")
        fun updateUser(@Body user: User, @Path("userId") userId: String): Flowable<User>

        @PUT("/users/{userId}/events/{eventId}/isInviteRejected.json")
        fun rejectInvite(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Flowable<Boolean>

        @PUT("/users/{userId}/events/{eventId}/isInviteAccepted.json")
        fun acceptInvite(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Flowable<Boolean>

        @PUT("/events/{eventId}/users/{userId}.json")
        fun setEventUserAsAttending(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Flowable<Boolean>


        @PATCH("/users/{userId}.json")
        fun patchUser(@Body user: User, @Path("userId") userId: String): Flowable<User>

        @PATCH("/users/{userId}/contacts.json")
        fun addContactToUser(@Body request: HashMap<String, Boolean>, @Path("userId") userId: String): Flowable<HashMap<String, Boolean>>

        @POST("/events.json")
        fun createEvent(@Body request: EventDetail): Flowable<EventCreationResponse>

        @PUT("/users/{userId}/events/{eventId}.json")
        fun addEventToUser(@Body eventInviteInfo: EventInviteInfo, @Path("userId") userId: String, @Path("eventId") eventId: String): Flowable<EventInviteInfo>

        @DELETE("/events/{eventId}/users/{userId}.json")
        fun removeUserFromEvent(@Path("eventId") eventId: String, @Path("userId") userId: String): Flowable<Response<Void>>

        @PUT("/events/{eventId}/host.json")
        fun updateEventHost(@Body host: Host, @Path("eventId") eventId: String): Flowable<Host>

        @PUT("/users/{userId}/unavailable_dates/{date}.json")
        fun setDateUnavailableForUser(@Body unavailable: Boolean?, @Path("userId") userId: String,
                                      @Path("date") date: String): Flowable<Boolean>

    }
}
