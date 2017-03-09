package org.taitasciore.android.privaliatechnicaltest;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by roberto on 06/03/17.
 */

public class MovieService {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private int pg; // Current page used for pagination
    private MainView view; // Current view (context)
    private Retrofit retrofit;
    private MovieDbApi api;
    private Call<MovieResponse> curCall; // Current call in case it needs to be cancelled

    public MovieService(MainView view) {
        this.pg = 1;
        this.view = view;

        // Using this naming policy so that Java camel-cased variables will map to lower case
        // with underscores in JSON and vice versa i.e. posterPath => poster_path
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();

        api = retrofit.create(MovieDbApi.class);
    }

    /**
     * It will most likely be detected by the JVM's garbage collector
     * but added code anyway just in case, so there are no more references to the objects
     */
    public void destroy() {
        view = null;
        retrofit = null;
        api = null;
        curCall = null;
    }

    /**
     * This method first determines network status
     * If it's connected, it executes the call asynchronously, sending the list of popular movies
     * to the view (Activity) via the MainView interface in order to populate RecyclerView.Adapter
     */
    public void getMoviesList(Callback<MovieResponse> callback) {
        cancelPendingRequests();
        curCall = api.getMoviesList(pg);
        curCall.enqueue(callback);
    }

    /**
     * This method first determines network status
     * If it's connected, it executes the call asynchronously, sending the list of search results
     * to the view (Activity) via the MainView interface in order to populate RecyclerView.Adapter
     * @param query Search query
     */
    public void searchMovieByKeyword(final String query, Callback<MovieResponse> callback) {
        cancelPendingRequests();
        curCall = api.searchMovieByKeyword(query, pg);
        curCall.enqueue(callback);
    }

    public int getPage() {
        return pg;
    }

    public void setPage(int pg) {
        this.pg = pg;
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public void cancelPendingRequests() {
        if (curCall != null) {
            curCall.cancel();
            curCall = null;
        }
    }
}
