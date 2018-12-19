package com.bookyrself.bookyrself.services;

import com.bookyrself.bookyrself.models.SerializedModels.EventCreationResponse;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import java.util.HashMap;

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
        Call<HashMap<String, EventInfo>> getUsersEventInfo(@Path("id") String userId);

        @GET("/users/{id}/picture.json")
        Call<String> getUserThumbUrl(@Path("id") String userId);

        @GET("/users/{id}.json")
        Call<User> getUserDetails(@Path("id") String userId);

        @GET("/users/{id}/contacts.json")
        Call<HashMap<String, Boolean>> getUserContacts(@Path("id") String userId);

        @PUT("/users/{userId}.json")
        Call<User> addUser(@Body User user, @Path("userId") String userId);


        @PATCH("/users/{userId}.json")
        Call<User> patchUser(@Body User user, @Path("userId") String userId);

        @PATCH("/users/{userId}/contacts.json")
        Call<HashMap<String, Boolean>> addContactToUser(@Body HashMap<String, Boolean> request, @Path("userId") String userId);

        @POST("/events.json")
        Call<EventCreationResponse> createEvent(@Body EventDetail request);

        //TODO: Clean this up. Find a way to minify the MiniEvent name
        @PUT("/users/{userId}/events/{eventId}.json")
        Call<EventInfo> addEventToUser(@Body EventInfo eventInfo, @Path("userId") String userId, @Path("eventId") String eventId);
    }
}
