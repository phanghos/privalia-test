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

    private int pg; // Current page used for pagination
    private MainView view; // Current view (context)
    private Retrofit mRetrofit;
    private MovieDbApi mApi;
    private Call<MovieResponse> curCall; // Current call in case it needs to be cancelled

    public MovieService(MainView view) {
        this.pg = 1;
        this.view = view;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();

        mApi = mRetrofit.create(MovieDbApi.class);
    }

    public void destroy() {
        view = null;
        mRetrofit = null;
        mApi = null;
        curCall = null;
    }

    /**
     * The method first checks for connection
     * If true, it executes the call asynchronously
     * Else, a Toast is shown with the error (network error)
     *
     * ProgressWheel will be shown if pg == 1, that is, if it is requesting the first page,
     * meaning the screen is blank
     */
    public void getMoviesList() {
        Call<MovieResponse> call = mApi.getMoviesList(pg);
        boolean isConnected = NetworkUtils.isConnected((MainActivity) view);
        if (!isConnected) {
            view.showNetworkError();
            return;
        }
        if (getPage() == 1) view.showProgressWheel();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                view.hideProgressWheel();
                view.hideRefreshLayout();

                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    ArrayList<MovieResponse.Movie> list = movieResponse.getResults();

                    if (getPage() == 1) view.setData(list); // Create adapter if requesting first page
                    else view.addItems(list); // Add items to existing adapter otherwise
                    pg++;
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.hideProgressWheel();
                view.hideRefreshLayout();
            }
        });
    }

    public void searchMovieByKeyword(final String query) {
        if (curCall != null) curCall.cancel();
        boolean isConnected = NetworkUtils.isConnected((MainActivity) view);
        if (!isConnected) {
            view.showNetworkError();
            view.hideRefreshLayout();
            return;
        }
        curCall = mApi.searchMovieByKeyword(query, pg);

        Callback<MovieResponse> callback = new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                view.hideProgressWheel();
                view.hideRefreshLayout();

                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    ArrayList<MovieResponse.Movie> list = movieResponse.getResults();
                    if (getPage() == 1) view.setData(list);
                    else view.addItems(list);
                    pg++;
                    view.setTitle("Results for '" + query + "'");
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.hideProgressWheel();
                view.hideRefreshLayout();
            }
        };

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
}
