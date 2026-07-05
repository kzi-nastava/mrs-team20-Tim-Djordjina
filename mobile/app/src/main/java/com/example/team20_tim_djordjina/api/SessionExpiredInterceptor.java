package com.example.team20_tim_djordjina.api;

import android.content.Context;

import com.example.team20_tim_djordjina.util.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/* Detects an expired or invalid session:
*  If a protected endpoint returns HTTP 401,
*  the stored token is cleared and the user
*  is sent back to the login screen */
public class SessionExpiredInterceptor implements Interceptor {

    private final Context appContext;
    private final TokenManager tokenManager;

    public SessionExpiredInterceptor(Context context, TokenManager tokenManager){
        this.appContext = context.getApplicationContext();
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}
