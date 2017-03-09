package org.taitasciore.android.privaliatechnicaltest;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by roberto on 07/03/17.
 */

public class WorkerFragment extends Fragment {

    private String query;

    private MainView view;
    private MovieService service;
    private ArrayList<MovieResponse.Movie> listPopular;
    private Callback<MovieResponse> moviesListListener;
    private Callback<MovieResponse> searchListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        listPopular = new ArrayList<>();
        setupListeners();
        service = new MovieService(view);
        getMoviesList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        view = (MainView) context;
        if (service != null) service.setView(view); // Setting new view (recreated activity)
    }

    @Override
    public void onDetach() {
        super.onDetach();
        view = null;
    }

    /**
     * It will most likely be detected by the JVM's garbage collector
     * but added code anyway just in case, so there are no more references to the objects
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        service.destroy();
        view = null;
        service = null;
    }

    public void getMoviesList() {
        if (!isConnected()) {
            view.hideRefreshLayout();
            return;
        }
        if (service.getPage() == 1) {
            view.hideRefreshLayout();
            view.showLoader();
        }
        service.getMoviesList(moviesListListener);
    }

    public void searchMovieByKeyword(String query, boolean newSearch) {
        if (!isConnected()) {
            view.hideRefreshLayout();
            return;
        }
        if (newSearch) service.setPage(1);
        this.query = query;
        service.searchMovieByKeyword(query, searchListener);
    }

    public void searchMovieByKeyword(String query) {
        searchMovieByKeyword(query, false);
    }

    public ArrayList<MovieResponse.Movie> getList() {
        return listPopular;
    }

    public void cancelPendingRequests() {
        service.cancelPendingRequests();
    }

    private void setupListeners() {
        moviesListListener = new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                view.hideLoader();
                view.hideRefreshLayout();

                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    ArrayList<MovieResponse.Movie> list = movieResponse.getResults();
                    for (MovieResponse.Movie m : list) listPopular.add(m);
                    if (service.getPage() == 1) view.setData(list); // Create adapter if requesting first page
                    else view.addItems(list); // Add items to existing adapter otherwise
                    if (!list.isEmpty()) service.setPage(service.getPage() + 1); // Increment page if and only if results were returned;
                }
                else
                    view.showResponseErrorForMovies();
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.hideLoader();
                view.hideRefreshLayout();
            }
        };

        searchListener = new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                view.hideLoader();
                view.hideRefreshLayout();

                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    ArrayList<MovieResponse.Movie> list = movieResponse.getResults();
                    if (list.isEmpty() && service.getPage() > 1) {
                        view.showEmptyListError();
                        return;
                    }
                    else if (service.getPage() == 1) view.setData(list);
                    else view.addItems(list);
                    // Increment page if and only if results were returned;
                    if (!list.isEmpty()) service.setPage(service.getPage() + 1);
                    view.setTitle("Results for '" + query + "'");
                }
                else
                    view.showResponseErrorForSearch(query, service.getPage() == 1);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                view.hideLoader();
                view.hideRefreshLayout();
            }
        };
    }

    private boolean isConnected() {
        return NetworkUtils.isConnected((MainActivity) view);
    }
}
