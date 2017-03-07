package org.taitasciore.android.privaliatechnicaltest;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Using a 'retained' fragment to better handle configuration changes
 * in particular, screen orientation changes. So state is persisted in WorkerFragment
 * even if host activity is destroyed during a screen orientation change
 * setRetainInstance(boolean) is the responsible method for this behavior
 */
public class MainActivity extends AppCompatActivity implements MainView {

    String query; // Search query

    WorkerFragment mWorkerFragment; // Retained fragment

    @BindView(R.id.list) RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutMngr;
    MovieAdapter mAdapter;

    @BindView(R.id.refresh_layout) SwipyRefreshLayout mRefreshLayout;
    @BindView(R.id.wheel) ProgressWheel wheel;
    @BindView(R.id.tv_empty) TextView tvEmpty;
    @BindView(R.id.btn_try_again) Button btnTryAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle("Popular movies");

        mLayoutMngr = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutMngr);

        mRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (query != null && !query.isEmpty()) mWorkerFragment.searchMovieByKeyword(query);
                else mWorkerFragment.getMoviesList();
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mWorkerFragment = (WorkerFragment) fm.findFragmentByTag("worker");
        if (mWorkerFragment == null) {
            mWorkerFragment = new WorkerFragment();
            fm.beginTransaction().add(mWorkerFragment, "worker").commit();
        } else if (savedInstanceState == null) {
            if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) handleSearchIntent(getIntent());
            else mWorkerFragment.getMoviesList();
        } else {
            if (savedInstanceState.containsKey("query")) query = savedInstanceState.getString("query");
            if (savedInstanceState.containsKey("list")) {
                ArrayList<MovieResponse.Movie> list = (ArrayList<MovieResponse.Movie>)
                        savedInstanceState.getSerializable("list");
                setData(list);
            }
        }

        /*
        if (savedInstanceState != null) {
            service = new MovieService(this);
            int pg = savedInstanceState.getInt("page");
            service.setPage(pg);
            if (savedInstanceState.containsKey("query")) query = savedInstanceState.getString("query");
            if (savedInstanceState.containsKey("list")) {
                ArrayList<MovieResponse.Movie> list = (ArrayList<MovieResponse.Movie>)
                        savedInstanceState.getSerializable("list");
                setData(list);
            }
        }
        */

        /*
        service = new MovieService(this);

        if (savedInstanceState == null) {
            if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) handleSearchIntent(getIntent());
            else service.getMoviesList();
        }
        else {
            int pg = savedInstanceState.getInt("page");
            service.setPage(pg);
            if (savedInstanceState.containsKey("query")) query = savedInstanceState.getString("query");
            if (savedInstanceState.containsKey("list")) {
                ArrayList<MovieResponse.Movie> list = (ArrayList<MovieResponse.Movie>)
                        savedInstanceState.getSerializable("list");
                setData(list);
            }
        }
        */
    }

    /*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getRefWatcher(this);
        refWatcher.watch(this);
    }
    */

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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.item_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_hint));

        // Expand SearchView if EditText was not empty before screen orientation change
        // and set text
        if (query != null && !query.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(query, false);
            searchView.clearFocus();
            // Hide keyboard after expaning SearchView and setting text
            // Remove this line to show keyboard (default behavior)
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*
                if (query.isEmpty()) return false;

                Intent i = new Intent(Intent.ACTION_SEARCH);
                i.setClass(getApplicationContext(), MainActivity.class);
                i.putExtra(SearchManager.QUERY, query);
                startActivity(i);
                searchItem.collapseActionView();

                return true;
                */
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query = newText;
                if (!query.isEmpty()) {
                    mWorkerFragment.searchMovieByKeyword(query, true);
                    //setTitle("Results for '" + query + "'");
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void handleSearchIntent(Intent intent) {
        query = intent.getStringExtra(SearchManager.QUERY);
        setTitle("Results for '" + query + "'");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mWorkerFragment.searchMovieByKeyword(query, true);
    }

    private void showEmptyText() {
        tvEmpty.setText("No results found for '" + query + "'");
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void hideEmptyText() {
        tvEmpty.setVisibility(View.GONE);
    }

    @Override
    public void showProgressWheel() {
        wheel.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressWheel() {
        wheel.setVisibility(View.GONE);
    }

    @Override
    public void hideRefreshLayout() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showButton() {
        btnTryAgain.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideButton() {
        btnTryAgain.setVisibility(View.GONE);
    }

    @Override
    public void showNetworkError() {
        Utils.toast(this, "Something went wrong. Please check your internet connection");
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
}
