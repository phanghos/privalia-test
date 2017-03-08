package org.taitasciore.android.privaliatechnicaltest;

import java.util.ArrayList;

/**
 * Created by roberto on 06/03/17.
 */

public interface MainView {

    void setTitle(String title);
    void showLoader();
    void hideLoader();
    void hideRefreshLayout();
    void showEmptyListError();
    void showNetworkError();
    void showResponseErrorForMovies();
    void showResponseErrorForSearch(String query, boolean newSearch);
    void setData(ArrayList<MovieResponse.Movie> list);
    void addItems(ArrayList<MovieResponse.Movie> list);
}
