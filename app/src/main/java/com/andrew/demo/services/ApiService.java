package com.andrew.demo.services;

import com.andrew.demo.dtos.LoginRequest;
import com.andrew.demo.models.PostResponse;
import com.andrew.demo.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("login")
    Call<User> login(@Body LoginRequest loginRequest);

    @GET("posts")
    Call<PostResponse> getPosts();
}
