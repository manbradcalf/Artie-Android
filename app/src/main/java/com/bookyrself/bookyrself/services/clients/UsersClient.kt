package com.bookyrself.bookyrself.services.clients

import com.bookyrself.bookyrself.data.serverModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.serverModels.User.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*

object UsersClient {

    private const val USERS_PATH = "https://bookyrself-staging.firebaseio.com/users/"

    val service: UsersClient by lazy {

        // Create the logging interceptor
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
                .baseUrl(USERS_PATH)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        retrofit.create(UsersClient::class.java)
    }

    interface UsersClient {

        @GET("{id}.json")
        suspend fun getUserDetails(@Path("id") userId: String): Response<User>

        @GET("{id}/events.json")
        suspend fun getUsersEventInvites(@Path("id") userId: String): Response<HashMap<String, EventInviteInfo>>

        @GET("{id}/contacts.json")
        suspend fun getUserContacts(@Path("id") userId: String): Response<HashMap<String, Boolean>>

        @PUT("{userId}.json")
        suspend fun updateUser(@Body user: User, @Path("userId") userId: String): Response<User>

        @PUT("{userId}/events{eventId}/isInviteRejected.json")
        suspend fun rejectInvite(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<Boolean>

        @PUT("{userId}/events{eventId}/isInviteAccepted.json")
        suspend fun acceptInvite(@Body bool: Boolean?, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<Boolean>

        @PATCH("{userId}.json")
        suspend fun patchUser(@Body user: User, @Path("userId") userId: String): Response<User>

        //TODO: contactWasAdded will always be true. Should I update this? Firebase wont take a post or put with no body and I need the value to be "true" here
        @PUT("{userId}/contacts{contactId}.json")
        suspend fun addContactToUserAsync(@Body isContact: Boolean, @Path("userId") userId: String, @Path("contactId") contactId: String): Response<Boolean>

        @PUT("{userId}/events{eventId}.json")
        suspend fun addEventToUser(@Body eventInviteInfo: EventInviteInfo, @Path("userId") userId: String, @Path("eventId") eventId: String): Response<EventInviteInfo>

        @DELETE("{eventId}/users{userId}.json")
        suspend fun removeUserFromEvent(@Path("eventId") eventId: String, @Path("userId") userId: String): Response<Response<Void>>

        @PUT("{userId}/unavailable_dates{date}.json")
        suspend fun setDateUnavailableForUser(@Body unavailable: Boolean?, @Path("userId") userId: String,
                                              @Path("date") date: String): Response<Boolean>

    }
}