package com.example.team20_tim_djordjina.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ThemedSpinnerAdapter;

import androidx.annotation.NonNull;

import com.example.team20_tim_djordjina.activities.LoginActivity;
import com.example.team20_tim_djordjina.util.TokenManager;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Response;

/* Detects an expired or invalid session:
*  If a protected endpoint returns HTTP 401,
*  the stored token is cleared and the user
*  is sent back to the login screen */
public class SessionExpiredInterceptor implements Interceptor {

    private final Context appContext;
    private final TokenManager tokenManager;

    // Guards against launching LoginActivity multiple times if
    // several requests come back 401 at almost the same moment
    private static final AtomicBoolean isRedirecting = new AtomicBoolean(false);

    public SessionExpiredInterceptor(Context context, TokenManager tokenManager){
        this.appContext = context.getApplicationContext();
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        String path = chain.request().url().encodedPath();
        if (response.code() == 401 && !isAuthEndpoint(path)){
            handleSessionExpired();
        }
        return response;
    }

    private boolean isAuthEndpoint(String path){
        return path.contains("/api/auth/");
    }

    private void handleSessionExpired(){
        // If there is no session, nothing is expired
        if(!tokenManager.isLoggedIn()){
            return;
        }
        if(isRedirecting.compareAndSet(false,true)){
            return;
        }

        tokenManager.clearSession();

        // Activities start on the main thread
        new Handler(Looper.getMainLooper()).post(()->{
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
            isRedirecting.set(false);
        });
    }
}
