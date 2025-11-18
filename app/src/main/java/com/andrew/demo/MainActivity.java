package com.andrew.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.andrew.demo.adapters.PostsAdapter;
import com.andrew.demo.databinding.ActivityMainBinding;
import com.andrew.demo.models.Post;
import com.andrew.demo.models.PostResponse;
import com.andrew.demo.services.ApiService;
import com.andrew.demo.utils.AppRoom;
import com.andrew.demo.utils.AppStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppRoom db;

    private ApiService apiService;

    private PostsAdapter adapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AppStore.isLoggedIn(getApplicationContext())) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        adapter = new PostsAdapter(new ArrayList<>());
        db = AppRoom.getInstance(getApplicationContext());

        setSupportActionBar(binding.mainToolbar);

        String username = AppStore.getPref(getApplicationContext(), "username");
        binding.mainUsername.setText("Welcome, " + username);

        binding.postsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.postsRecyclerView.setAdapter(adapter);
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshFetch);

        fetchPosts();
    }

    private void fetchPosts() {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        executor.execute(() -> {
            int postCount = db.postDao().getPostCount();

            if (postCount > 0 && !binding.swipeRefreshLayout.isRefreshing()) {
                List<Post> posts = db.postDao().getAllPosts();
                mainHandler.postDelayed(() -> updateUIWithPosts(posts), 1500);;
            } else {
                fetchFromApi();
            }
        });
    }

    private void refreshFetch() {

        binding.progressBar.setVisibility(View.VISIBLE);

        adapter.clearData();

        executor.execute(() -> {

            db.postDao().deleteAll();

            try { Thread.sleep(1200); } catch (InterruptedException e) { }

            fetchFromApi();
        });
    }

    private void fetchFromApi() {

        apiService.getPosts().enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body().getPosts();

                   executor.execute(() -> {
                        db.postDao().insertAll(posts);
                    });

                    updateUIWithPosts(posts);
                } else {
                    handleFetchFailure("Failed to load posts");
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                handleFetchFailure("Error: " + t.getMessage());
            }
        });
    }

    private void updateUIWithPosts(List<Post> posts) {
        binding.progressBar.setVisibility(View.GONE);
        if (binding.swipeRefreshLayout.isRefreshing()) {
            binding.swipeRefreshLayout.setRefreshing(false);
        }
        adapter.updateData(posts);
    }

    private void handleFetchFailure(String message) {
        binding.progressBar.setVisibility(View.GONE);
        if (binding.swipeRefreshLayout.isRefreshing()) {
            binding.swipeRefreshLayout.setRefreshing(false);
        }
        Log.e("MainActivity", message);
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        // Clear local database on logout
        executor.execute(() -> {
            db.clearAllTables();
        });

        AppStore.isLoggedIn(getApplicationContext(), false);
        AppStore.clearPref(getApplicationContext(), "username");
        AppStore.clearPref(getApplicationContext(), "firstname");
        AppStore.clearPref(getApplicationContext(), "accesstoken");
        AppStore.clearPref(getApplicationContext(), "lastname");

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
