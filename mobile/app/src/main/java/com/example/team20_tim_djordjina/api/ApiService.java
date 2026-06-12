package com.example.team20_tim_djordjina.api;

import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.LoginRequest;
import com.example.team20_tim_djordjina.model.LoginResponse;
import com.example.team20_tim_djordjina.model.RegistrationRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("api/auth/logout")
    Call<ApiResponse> logout(@Query("userId") Long userId);

    @POST("api/auth/register")
    Call<ApiResponse> register(@Body RegistrationRequest request);

    @GET("api/auth/activate")
    Call<ApiResponse> activateAccount(@Query("token") String token);

    @POST("api/auth/resend-activation")
    Call<ApiResponse> resendActivation(@Query("email") String email);
}
