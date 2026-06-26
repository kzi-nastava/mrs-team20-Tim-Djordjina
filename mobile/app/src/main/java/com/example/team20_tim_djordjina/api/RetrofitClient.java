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

    private static Retrofit retrofit = null;
    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(Context context){
        TokenManager tokenManager = new TokenManager(context.getApplicationContext());

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
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
    public static Retrofit getClient(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public ApiService getApiService(){
        return apiService;
    }

    /*
    public static ApiService getApiService(){
        return getClient().create(ApiService.class);
    }*/
}
