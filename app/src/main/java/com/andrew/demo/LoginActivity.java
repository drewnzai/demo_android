package com.andrew.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.andrew.demo.databinding.ActivityLoginBinding;
import com.andrew.demo.dtos.LoginRequest;
import com.andrew.demo.models.User;
import com.andrew.demo.services.ApiService;
import com.andrew.demo.utils.AppStore;
import com.andrew.demo.viewmodels.LoginViewModel;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);


        binding.buttonLogin.setOnClickListener( view ->
        {
            loginViewModel.username.setValue(binding.username.getText().toString().trim());
            loginViewModel.password.setValue(binding.password.getText().toString().trim());

            LoginRequest loginRequest = new LoginRequest();
            /*
            username: 'emilys',
            password: 'emilyspass',
            * */
            loginRequest.setUsername(loginViewModel.username.getValue());
            loginRequest.setPassword(loginViewModel.password.getValue());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://dummyjson.com/auth/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            Call<User> login = apiService.login(loginRequest);

            login.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful() && response.body() != null){
                        User user = response.body();
                        AppStore.addPref(getApplicationContext(), "username", user.getUsername());
                        AppStore.addPref(getApplicationContext(), "firstname", user.getFirstName());
                        AppStore.addPref(getApplicationContext(), "lastname", user.getLastName());
                        AppStore.addPref(getApplicationContext(), "accesstoken", user.getAccessToken());

                        AppStore.isLoggedIn(getApplicationContext(), true);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("Login", "Failed to login: " + response.code());
                        binding.username.setText("");
                        binding.password.setText("");
                        Snackbar.make(binding.getRoot(), "Failed to login: Invalid credentials", Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    binding.username.setText("");
                    binding.password.setText("");
                    Snackbar.make(binding.getRoot(), "Failed to login: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
}
