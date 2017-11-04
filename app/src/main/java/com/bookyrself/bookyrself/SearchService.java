package com.bookyrself.bookyrself;

import com.bookyrself.bookyrself.models.searchrequest.SearchRequest;
import com.bookyrself.bookyrself.models.searchresponse.Hit;

import java.util.List;

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
  
    public interface SearchAPI {
        @POST("/search/request.json")
        Call<List<Hit>> executeSearch(@Body SearchRequest query);
    }

    public SearchAPI getAPI(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_BOOKYRSELF_FIREBASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(SearchAPI.class);
    }
}