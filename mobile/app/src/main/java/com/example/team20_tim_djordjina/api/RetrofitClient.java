package com.example.team20_tim_djordjina.api;

import android.content.Context;

import com.example.team20_tim_djordjina.BuildConfig;
import com.example.team20_tim_djordjina.util.TokenManager;
import com.google.gson.internal.GsonBuildConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://" + BuildConfig.IP_ADDR + ":8080/";
    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(Context context){
        TokenManager tokenManager = new TokenManager(context.getApplicationContext());

        OkHttpClient client = new OkHttpClient.Builder()
                // Adds "Authorization: Bearer <token>" to outgoing requests
                .addInterceptor(new AuthInterceptor(tokenManager))
                // Detects 401 on protected endpoints -> clears session plus routes to login
                .addInterceptor(new SessionExpiredInterceptor(context, tokenManager))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance(Context context){
        if (instance == null){
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public ApiService getApiService(){
        return apiService;
    }

}
