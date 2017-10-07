package com.bookyrself.bookyrself;

import com.bookyrself.bookyrself.models.searchresponse.Hit;
import com.bookyrself.bookyrself.models.SearchRequest;

import java.util.List;

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
        Call<List<Hit>> executeSearch(@Body SearchRequest request);
    }

    public SearchAPI getAPI(){
       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_BOOKYRSELF_FIREBASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(SearchAPI.class);
    }
}