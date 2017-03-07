package org.taitasciore.android.privaliatechnicaltest;

import java.util.ArrayList;

/**
 * Created by roberto on 06/03/17.
 */

public interface MainView {

    void setTitle(String title);
    void showProgressWheel();
    void hideProgressWheel();
    void hideRefreshLayout();
    void showButton();
    void hideButton();
    void showNetworkError();
    void setData(ArrayList<MovieResponse.Movie> list);
    void addItems(ArrayList<MovieResponse.Movie> list);
}
