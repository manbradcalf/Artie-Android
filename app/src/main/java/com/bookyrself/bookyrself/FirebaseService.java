package com.bookyrself.bookyrself;

import com.bookyrself.bookyrself.models.EventDetailResponse.EventDetailResponse;
import com.bookyrself.bookyrself.models.SearchResponseUsers.Event;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class FirebaseService {

    private static String BASE_URL_BOOKYRSELF_FIREBASE = "https://bookyrself-staging.firebaseio.com/";

    public interface FirebaseApi {
        @GET("/events/{id}.json")
        Call<EventDetailResponse> getEventData(@Path("id") String eventId);

        @GET("/users/{id}/events.json")
        Call<List<Event>> getUserEvents(@Path("id") String userId);

        @GET("/users/{id}/picture.json")
        Call<String> getUserThumbUrl(@Path("id") String userId);

        // Add event to user item
        // Passing in "eventArrayPosition" so ES doesn't break the mapping by accidentally creating
        // an object in firebase instead of maintaining the array.
        // This is my solution to https://github.com/firebase/flashlight/issues/178
        @PATCH("/users/{userId}/events/{eventArrayPosition}.json")
        Call<Event> addEventToUser(@Body Event event, @Path("userId") String userId, @Path("eventArrayPosition") Long userArrayPosition);
    }

    public FirebaseService.FirebaseApi getAPI(){
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
}
