package com.example.team20_tim_djordjina.api;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "RideOnPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private final SharedPreferences prefs;


    public TokenManager(Context context){
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // After successful login, save everything
    public void saveSession(String token, Long userId, String email, String role){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        if (userId != null){
            editor.putLong(KEY_USER_ID, userId);
        }
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public String getToken(){
        return prefs.getString(KEY_TOKEN, null);
    }
    public long getUserId(){
        return prefs.getLong(KEY_USER_ID, -1L);
    }
    public String getEmail(){
        return prefs.getString(KEY_EMAIL, null);
    }
    public String getRole(){
        return prefs.getString(KEY_ROLE, null);
    }
    public boolean isLoggedIn(){
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

}
