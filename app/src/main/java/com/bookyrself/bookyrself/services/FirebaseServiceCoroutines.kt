package com.bookyrself.bookyrself.services


import com.bookyrself.bookyrself.data.ServerModels.EventCreationResponse
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.Host
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*

object FirebaseServiceCoroutines {

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
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()

        retrofit.create(FirebaseApi::class.java)
    }

    interface FirebaseApi {
        @GET("/events/{id}.json")
        suspend fun getEventData(@Path("id") eventId: String): Response<EventDetail>

        @GET("/users/{id}.json")
        suspend fun getUserDetails(@Path("id") userId: String): Response<User>

        @GET("/users/{id}/events.json")
        suspend fun getUsersEventInvites(@Path("id") userId: String): Response<HashMap<String, EventInviteInfo>>

        @GET("/users/{id}/contacts.json")
        suspend fun getUserContacts(@Path("id") userId: String): Response<HashMap<String, Boolean>>

        @PUT("/users/{userId}.json")
        suspend fun updateUser(@Body user: User, @Path("userId") userId: String): Response<User>

        @PUT("/users/{userId}/events/{eventId}/isInviteRejected.json")
        suspend fun rejectInvite(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<Boolean>

        @PUT("/users/{userId}/events/{eventId}/isInviteAccepted.json")
        suspend fun acceptInvite(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<Boolean>

        @PUT("/events/{eventId}/users/{userId}.json")
        suspend fun setEventUserAsAttending(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<Boolean>

        @PATCH("/users/{userId}.json")
        suspend fun patchUser(@Body user: User, @Path("userId") userId: String): Response<User>

        //TODO: contactWasAdded will always be true. Should I update this? Firebase wont take a post or put with no body and I need the value to be "true" here
        @PUT("/users/{userId}/contacts/{contactId}.json")
        suspend fun addContactToUserAsync(@Body isContact: Boolean, @Path("userId") userId: String, @Path("contactId") contactId: String): Response<Boolean>

        @POST("/events.json")
        suspend fun createEvent(@Body request: EventDetail): Response<EventCreationResponse>

        @PUT("/users/{userId}/events/{eventId}.json")
        suspend fun addEventToUser(@Body eventInviteInfo: EventInviteInfo, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<EventInviteInfo>

        @DELETE("/events/{eventId}/users/{userId}.json")
        suspend fun removeUserFromEvent(@Path("eventId") eventId: String, @Path("userId") userId: String): Response<Response<Void>>

        @PUT("/events/{eventId}/host.json")
        suspend fun updateEventHost(@Body host: Host, @Path("eventId") eventId: String): Response<Host>

        @PUT("/users/{userId}/unavailable_dates/{date}.json")
        suspend fun setDateUnavailableForUser(@Body unavailable: Boolean?, @Path("userId") userId: String,
                                              @Path("date") date: String): Response<Boolean>
    }
}
