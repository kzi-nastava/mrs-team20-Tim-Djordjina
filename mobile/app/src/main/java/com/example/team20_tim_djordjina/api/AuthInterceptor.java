package com.example.team20_tim_djordjina.api;

import androidx.annotation.NonNull;

import com.example.team20_tim_djordjina.util.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp interceptor that automatically attaches the JWT token as
 * "Authorization: Bearer <token>" to every outgoing request
 */
public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager){
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()){
            return chain.proceed(original);
        }
        Request authorized = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authorized);
    }
}
