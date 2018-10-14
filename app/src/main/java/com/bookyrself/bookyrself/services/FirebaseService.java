package com.bookyrself.bookyrself.services;

import com.bookyrself.bookyrself.models.SerializedModels.EventCreationResponse;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class FirebaseService {

    private static String BASE_URL_BOOKYRSELF_FIREBASE = "https://bookyrself-staging.firebaseio.com/";

    public FirebaseService.FirebaseApi getAPI() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_BOOKYRSELF_FIREBASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(FirebaseService.FirebaseApi.class);
    }

    public interface FirebaseApi {
        @GET("/events/{id}.json")
        Call<EventDetail> getEventData(@Path("id") String eventId);

        @GET("/users/{id}/events.json")
        Call<HashMap<String, EventInfo>> getUserEvents(@Path("id") String userId);

        @GET("/users/{id}/picture.json")
        Call<String> getUserThumbUrl(@Path("id") String userId);

        @GET("/users/{id}.json")
        Call<User> getUserDetails(@Path("id") String userId);

        @GET("/users/{id}/contacts.json")
        Call<List<String>> getUserContacts(@Path("id") String userId);

        @PUT("/users/{userId}.json")
        Call<User> addUser(@Body User user, @Path("userId") String userId);

        // Add event to user item
        // Passing in "eventArrayPosition" so ES doesn't break the mapping by accidentally creating
        // an object in firebase instead of maintaining the array.
        // This is my solution to https://github.com/firebase/flashlight/issues/178
//        //TODO: Revisit this one. Does it actually work? I was getting invalid jsonobject errors befre. look at addContact call for example of one thatworks
//        @PATCH("/users/{userId}/events/{eventArrayPosition}.json")
//        Call<MiniEvent> addEventToUser(@Body MiniEvent event, @Path("userId") String userId, @Path("eventArrayPosition") Long userArrayPosition);


        @PATCH("/users/{userId}.json")
        Call<User> patchUser(@Body User user, @Path("userId") String userId);

        // Add contact to user item
        // Passing in "contactArrayPosition" so ES doesn't break the mapping by accidentally creating
        // an object in firebase instead of maintaining the array.
        // This is my solution to https://github.com/firebase/flashlight/issues/178
        @PATCH("/users/{userId}/contacts.json")
        Call<Map<String, String>> addContactToUser(@Body Map<String, String> request, @Path("userId") String userId);

        //TODO: Rename EventDetail to MiniEvent or something more broad, since it is the JSON that represents not only the response but the actual event object in the db
        @POST("/events.json")
        Call<EventCreationResponse> createEvent(@Body EventDetail request);

        @POST("/users/{userId}/invites/events/{eventId}.json")
        Call<Boolean> sendInviteToUserForEvent(@Body Boolean isAccepted, @Path("userId") String userId, @Path("eventId") String eventId);

        //TODO: Clean this up. Find a way to minify the MiniEvent name
        @PUT("/users/{userId}/events/{eventId}.json")
        Call<HashMap<String, EventInfo>> addEventToUser(@Body HashMap<String, EventInfo> event, @Path("userId") String userId, @Path("eventId") String eventId);
    }
}
