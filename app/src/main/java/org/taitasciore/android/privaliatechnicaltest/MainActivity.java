package org.taitasciore.android.privaliatechnicaltest;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Using a 'retained' fragment to better handle configuration changes
 * in particular, screen orientation changes. So state is persisted in WorkerFragment
 * even if host activity is destroyed during a screen orientation change
 * setRetainInstance(boolean) is the responsible method for this behavior
 */
public class MainActivity extends AppCompatActivity implements MainView {

    private static final String TAG_WORKER_FRAGMENT = "worker";

    @BindString(R.string.toolbar_title) String toolbarTitle;
    @BindString(R.string.search_hint) String searchHint;
    @BindString(R.string.network_error) String networkError;
    @BindString(R.string.response_error) String responseError;
    @BindString(R.string.no_more_results) String noMoreResults;

    String query = ""; // Search query

    WorkerFragment mWorkerFragment; // Retained fragment

    @BindView(R.id.list) RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutMngr;
    MovieAdapter mAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.refresh_layout) SwipyRefreshLayout mRefreshLayout;
    @BindView(R.id.loader) AVLoadingIndicatorView loader;
    @BindView(R.id.tv_empty) TextView tvEmpty;
    @BindView(R.id.main_layout) RelativeLayout layout;

    /**
     * Scroll to top of list when toolbar is clicked
     */
    @OnClick(R.id.toolbar) void onToolbarClicked() {
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setTitle(toolbarTitle);
        setupRecyclerView();
        addRefreshListener();

        FragmentManager fm = getSupportFragmentManager();
        mWorkerFragment = (WorkerFragment) fm.findFragmentByTag(TAG_WORKER_FRAGMENT);
        if (mWorkerFragment == null) {
            mWorkerFragment = new WorkerFragment();
            fm.beginTransaction().add(mWorkerFragment, TAG_WORKER_FRAGMENT).commit();
        } else {
            if (savedInstanceState.containsKey("query"))
                query = savedInstanceState.getString("query");
            if (savedInstanceState.containsKey("list")) {
                ArrayList<MovieResponse.Movie> list = (ArrayList<MovieResponse.Movie>)
                        savedInstanceState.getSerializable("list");
                setData(list);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) outState.putSerializable("list", mAdapter.getList());
        if (query != null && !query.isEmpty()) outState.putString("query", query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        final MenuItem searchItem = menu.findItem(R.id.item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(searchHint);

        // Expand SearchView if EditText was not empty before screen orientation change
        // and set text
        if (query != null && !query.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(query, false);
            searchView.clearFocus();
            // Hide keyboard after expanding SearchView and setting text
            // Remove this line to show keyboard (default behavior)
        }

        /**
         * If the close button in the SearchView was clicked (the X), the list of popular movies
         * will be shown again if it's not empty
         */
        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery("", false);
                if (!mWorkerFragment.getList().isEmpty()) {
                    mWorkerFragment.cancelPendingRequests();
                    setData(mWorkerFragment.getList());
                    setTitle(toolbarTitle);
                }
            }
        });

        /**
         * The list of popular movies will be shown again if and only if the user
         * leaves the EditText field manually (by erasing every character)
         *
         * If the back button (left arrow) was clicked, return and do nothing.
         * This check is done by comparing the newText with the current value stored in
         * query. If the length of current value is > 1 and the newText is empty, that means
         * that the user clicked on the back button. Else, it means that every character was,
         * erased one by one by the user, using the keyboard, thus leaving the field blank, which
         * will cause the list of popular movies to be shown again if it's not empty
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals(query)) {
                    if (query.length() > 1 && newText.isEmpty()) return false;
                    query = newText;
                }
                else return false;

                // If SearchView is empty and list of popular movies is not empty
                // list of popular movies will be shown again. Otherwhise, such list
                // would be lost and could not be shown again unless the app was restarted
                // Pending requests must be cancelled or unexpected behaviors can happen
                // Otherwhise start asynchronous search
                if (query.isEmpty() && !mWorkerFragment.getList().isEmpty()) {
                    mWorkerFragment.cancelPendingRequests();
                    setData(mWorkerFragment.getList());
                    setTitle(toolbarTitle);
                }
                else
                    mWorkerFragment.searchMovieByKeyword(query, true);

                return true;
            }
        });

        return true;
    }

    @Override
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void showEmptyText() {
        tvEmpty.setText("No results found for '" + query + "'");
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void hideEmptyText() {
        tvEmpty.setVisibility(View.GONE);
    }

    @Override
    public void showLoader() {
        loader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        loader.setVisibility(View.GONE);
    }

    @Override
    public void hideRefreshLayout() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showEmptyListError() {
        Utils.showSnackbar(layout, noMoreResults);
    }

    @Override
    public void showNetworkError() {
        Utils.showSnackbar(layout, networkError);
    }

    @Override
    public void showResponseErrorForMovies() {
        Utils.showSnackbar(layout, responseError, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWorkerFragment.getMoviesList();
            }
        });
    }

    @Override
    public void showResponseErrorForSearch(final String query, final boolean newSearch) {
        Utils.showSnackbar(layout, responseError, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWorkerFragment.searchMovieByKeyword(query, newSearch);
            }
        });
    }

    @Override
    public void setData(ArrayList<MovieResponse.Movie> list) {
        mAdapter = new MovieAdapter(this, list);
        mRecyclerView.setAdapter(mAdapter);
        if (list.isEmpty()) showEmptyText();
        else hideEmptyText();
    }

    @Override
    public void addItems(ArrayList<MovieResponse.Movie> list) {
        if (mAdapter != null)
            for (MovieResponse.Movie m : list)
                mAdapter.add(m);
    }

    private void setupRecyclerView() {
        mLayoutMngr = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutMngr);
    }

    private void addRefreshListener() {
        mRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (query != null && !query.isEmpty()) mWorkerFragment.searchMovieByKeyword(query);
                else mWorkerFragment.getMoviesList();
            }
        });
    }
}
