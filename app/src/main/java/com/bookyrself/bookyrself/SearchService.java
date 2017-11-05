package com.bookyrself.bookyrself;

import com.bookyrself.bookyrself.models.searchresponse.SearchResponse2;

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

    private static String BASE_URL_BOOKYRSELF_FIREBASE = "https://bookyrself-staging.firebaseio.com/";
    private static String BASE_URL_ES = "https://pine-4785036.us-east-1.bonsaisearch.net/";


    public interface SearchAPI {
        @POST("/event_search/_search")
        Call<SearchResponse2> executeSearch(@Body com.bookyrself.bookyrself.models.searchrequest.Body query);
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