package com.bookyrself.bookyrself.services;

import com.bookyrself.bookyrself.models.SearchResponseEvents.SearchResponse2;
import com.bookyrself.bookyrself.models.SearchResponseUsers.SearchResponseUsers;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by benmedcalf on 8/11/17.
 */
public class SearchService {

    private static String BASE_URL_ES = "https://dogwood-9512546.us-east-1.bonsaisearch.net";


    public interface SearchAPI {
        @POST("/events/_search")
        Call<SearchResponse2> executeEventsSearch(@Body com.bookyrself.bookyrself.models.SearchRequest.Body query);

        @POST("/users/_search")
        Call<SearchResponseUsers> executeUsersSearch(@Body com.bookyrself.bookyrself.models.SearchRequest.Body query);
    }

    public SearchAPI getAPI(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_ES)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(SearchAPI.class);
    }
}