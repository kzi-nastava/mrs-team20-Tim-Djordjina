package com.example.team20_tim_djordjina.api;

import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.BlockUserRequest;
import com.example.team20_tim_djordjina.model.ChangePasswordRequest;
import com.example.team20_tim_djordjina.model.DriverRegistrationRequest;
import com.example.team20_tim_djordjina.model.FavouriteRoute;
import com.example.team20_tim_djordjina.model.LoginRequest;
import com.example.team20_tim_djordjina.model.LoginResponse;
import com.example.team20_tim_djordjina.model.NotificationItem;
import com.example.team20_tim_djordjina.model.PasswordResetRequest;
import com.example.team20_tim_djordjina.model.PasswordResetSubmit;
import com.example.team20_tim_djordjina.model.ProfileChangeRequestItem;
import com.example.team20_tim_djordjina.model.ProfileResponse;
import com.example.team20_tim_djordjina.model.RegistrationRequest;
import com.example.team20_tim_djordjina.model.RideHistoryItem;
import com.example.team20_tim_djordjina.model.RideRequest;
import com.example.team20_tim_djordjina.model.RideResponse;
import com.example.team20_tim_djordjina.model.UpdateProfileRequest;
import com.example.team20_tim_djordjina.model.UserListItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @POST("api/auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body PasswordResetRequest request);

    @POST("api/auth/reset-password")
    Call<ApiResponse> resetPassword(@Body PasswordResetSubmit request);

    @POST("api/admin/drivers")
    Call<ApiResponse> registerDriver(@Body DriverRegistrationRequest request);

    @GET("api/admin/users")
    Call<List<UserListItem>> listUsers(@Query("role") String role);

    @POST("api/admin/users/{id}/block")
    Call<UserListItem> blockUser(@Path("id") Long id, @Body BlockUserRequest request);

    @POST("api/admin/users/{id}/unblock")
    Call<UserListItem> unblockUser(@Path("id") Long id);

    @GET("api/users/me")
    Call<ProfileResponse> getProfile();

    @PUT("api/users/me")
    Call<ApiResponse> updateProfile(@Body UpdateProfileRequest request);

    @POST("api/users/me/change-password")
    Call<ApiResponse> changePassword(@Body ChangePasswordRequest request);

    @GET("api/admin/profile-changes")
    Call<List<ProfileChangeRequestItem>> getPendingProfileChanges();

    @POST("api/admin/profile-changes/{id}/approve")
    Call<ApiResponse> approveProfileChange(@Path("id") Long id);

    @POST("api/admin/profile-changes/{id}/reject")
    Call<ApiResponse> rejectProfileChange(@Path("id") Long id);

    @POST("api/rides")
    Call<RideResponse> requestRide(@Body RideRequest request);

    @GET("api/notifications")
    Call<List<NotificationItem>> getNotifications();

    @GET("api/notifications/unread-count")
    Call<Long> getUnreadCount();

    @POST("api/notifications/{id}/read")
    Call<ApiResponse> markNotificationRead(@Path("id") Long id);

    @GET("api/rides/current")
    Call<RideResponse> getCurrentRide();

    @POST("api/rides/{id}/start")
    Call<RideResponse> startRide(@Path("id") Long id);

    @POST("api/rides/{id}/finish")
    Call<RideResponse> finishRide(@Path("id") Long id);

    @GET("api/rides/linked")
    Call<List<RideResponse>> getLinkedRides();

    @GET("api/favourite-routes")
    Call<List<FavouriteRoute>> getFavouriteRoutes();

    @POST("api/favourite-routes")
    Call<FavouriteRoute> saveFavouriteRoute(@Body FavouriteRoute route);

    @DELETE("api/favourite-routes/{id}")
    Call<ApiResponse> deleteFavouriteRoute(@Path("id") Long id);


    @GET("api/rides/history")
    Call<List<RideHistoryItem>> getRideHistory();

    @GET("api/rides/history/{id}")
    Call<RideHistoryItem> getRideHistoryDetail(@Path("id") Long id);
}
