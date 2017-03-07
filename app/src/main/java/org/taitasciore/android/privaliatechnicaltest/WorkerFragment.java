package org.taitasciore.android.privaliatechnicaltest;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by roberto on 07/03/17.
 */

public class WorkerFragment extends Fragment {

    private MainView view;
    private MovieService service;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        service = new MovieService(view);
        service.getMoviesList();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.destroy();
        view = null;
        service = null;
    }

    public void getMoviesList() {
        service.getMoviesList();
    }

    public void searchMovieByKeyword(String query, boolean newSearch) {
        if (newSearch) service.setPage(1);
        service.searchMovieByKeyword(query);
    }

    public void searchMovieByKeyword(String query) {
        searchMovieByKeyword(query, false);
    }
}
