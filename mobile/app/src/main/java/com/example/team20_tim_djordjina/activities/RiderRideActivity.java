package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityRiderRideBinding;
import com.example.team20_tim_djordjina.model.CancelRideRequest;
import com.example.team20_tim_djordjina.model.InconsistencyRequest;
import com.example.team20_tim_djordjina.model.PanicItem;
import com.example.team20_tim_djordjina.model.PanicRequest;
import com.example.team20_tim_djordjina.model.Tracking;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiderRideActivity extends AppCompatActivity {


    public static final String EXTRA_RIDE_ID = "ride_id";
    private static final long POLL_MS = 10_000L;
    private ActivityRiderRideBinding binding;
    private ApiService apiService;
    private long rideId;
    private MapView map;
    private String currentStatus;

    private boolean staticMarkersAdded = false;
    private Marker pickupMarker, destinationMarker, vehicleMarker;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            fetchTracking();
            handler.postDelayed(this, POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rider_ride);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        Configuration.getInstance().setUserAgentValue("RideOn-team20-kzi");

        binding = ActivityRiderRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rideId = getIntent().getLongExtra(EXTRA_RIDE_ID, -1);
        if (rideId <= 0) { finish(); return;}

        apiService = RetrofitClient.getInstance(this).getApiService();

        map = binding.mapRider;
        map.setTileSource(TileSourceFactory.OpenTopo);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.0);

        binding.btnRiderPanic.setOnClickListener(v -> showPanicConfirm());
        binding.btnRiderCancel.setOnClickListener(v -> showCancelConfirm());
        binding.btnReportInconsistency.setOnClickListener(v -> showReportDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        handler.post(poll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        handler.removeCallbacks(poll);
    }

    private void fetchTracking() {
        apiService.getTracking(rideId).enqueue(new Callback<Tracking>() {
            @Override
            public void onResponse(Call<Tracking> call, Response<Tracking> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUi(response.body());
                }
            }

            @Override
            public void onFailure(Call<Tracking> call, Throwable t) {

            }
        });
    }

    private void updateUi(Tracking t) {
        android.util.Log.d("RiderTrack",
                "veh=" + t.getVehicleLatitude() + "," + t.getVehicleLongitude()
                        + " pickup=" + t.getPickupLatitude() + "," + t.getPickupLongitude()
                        + " eta=" + t.getEtaMinutes() + " status=" + t.getStatus());

        currentStatus = t.getStatus();

        binding.tvRiderStatus.setText(t.getStatus());
        binding.tvRiderEta.setText(getString(R.string.eta_minutes, t.getEtaMinutes()));
        if (t.getDriverName() != null) {
            binding.tvRiderDriver.setText(getString(R.string.driver_label, t.getDriverName()));
            binding.tvRiderDriver.setVisibility(View.VISIBLE);
        }

        // Static pickup/destination markers (once)
        if (!staticMarkersAdded) {
            GeoPoint pickup = new GeoPoint(t.getPickupLatitude(), t.getPickupLongitude());
            GeoPoint destination = new GeoPoint(t.getDestinationLatitude(), t.getDestinationLongitude());
            pickupMarker = addMarker(pickup, getString(R.string.pickup));
            destinationMarker = addMarker(destination, getString(R.string.destination));
            map.getController().setCenter(pickup);
            map.getController().setZoom(14.0);
            staticMarkersAdded = true;
        }

        // Moving vehicle marker
        if (t.getVehicleLatitude() != null && t.getVehicleLongitude() != null) {
            GeoPoint vp = new GeoPoint(t.getVehicleLatitude(), t.getVehicleLongitude());
            if (vehicleMarker == null) {
                vehicleMarker = addMarker(vp, getString(R.string.vehicle));
                map.getController().animateTo(vp);
            } else {
                vehicleMarker.setPosition(vp);
                map.invalidate();
            }
        }

        // Button visibility by state
        boolean active = "ASSIGNED".equals(currentStatus) || "IN_PROGRESS".equals(currentStatus) ||
                "SCHEDULED".equals(currentStatus);
        boolean inProgress = "IN_PROGRESS".equals(currentStatus);
        show(binding.btnRiderPanic, "ASSIGNED".equals(currentStatus) || inProgress);
        show(binding.btnRiderCancel, "ASSIGNED".equals(currentStatus) || "SCHEDULED".equals(currentStatus));
        show(binding.btnReportInconsistency, inProgress);

        // Terminal - stop polling and wrap up
        if (!active) {
            handler.removeCallbacks(poll);
            toast(getString(R.string.ride_ended));
            binding.btnRiderPanic.setVisibility(View.GONE);
            binding.btnRiderCancel.setVisibility(View.GONE);
            binding.btnReportInconsistency.setVisibility(View.GONE);
        }
    }


    private Marker addMarker(GeoPoint point, String title) {
        Marker m = new Marker(map);
        m.setPosition(point);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setTitle(title);
        map.getOverlays().add(m);
        map.invalidate();
        return m;
    }

    // ---------- Panic ----------

    private void showPanicConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.panic_title)
                .setMessage(R.string.panic_confirm)
                .setPositiveButton(R.string.panic_send, (d, w) -> triggerPanic())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void triggerPanic() {
        apiService.triggerPanic(rideId, new PanicRequest(null)).enqueue(new Callback<PanicItem>() {
            @Override
            public void onResponse(Call<PanicItem> call, Response<PanicItem> response) {
                if (response.isSuccessful()) toast(getString(R.string.panic_sent));
                else if (response.code() == 403) toast(getString(R.string.panic_not_participant));
                else if (response.code() == 409) toast(getString(R.string.panic_not_active));
                else toast(getString(R.string.something_went_wrong));
            }

            @Override
            public void onFailure(Call<PanicItem> call, Throwable t) {
                toast(getString(R.string.network_error));
            }
        });
    }

    // ---------- Cancel ----------

    private void showCancelConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cancel_ride)
                .setMessage(R.string.rider_cancel_confirm)
                .setPositiveButton(R.string.cancel_ride_confirm, (d, w) -> cancelRide())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void cancelRide() {
        apiService.cancelRide(rideId, new CancelRideRequest(null)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    toast(getString(R.string.ride_cancelled));
                    finish();
                } else if (response.code() == 409) {
                    toast(getString(R.string.cancel_too_late));
                } else if (response.code() == 403) {
                    toast(getString(R.string.panic_not_participant));
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toast(getString(R.string.network_error));
            }
        });
    }

    // ---------- Inconsistency ----------

    private void showReportDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.inconsistency_hint);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.report_inconsistency)
                .setMessage(R.string.inconsistency_prompt)
                .setView(input)
                .setPositiveButton(R.string.submit, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            pos.setOnClickListener(v -> {
                String text = input.getText().toString().trim();
                if (text.isEmpty()) {
                    input.setError(getString(R.string.inconsistency_required));
                    return;
                }
                reportInconsistency(text);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void reportInconsistency(String text) {
        apiService.reportInconsistency(rideId, new InconsistencyRequest(text)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) toast(getString(R.string.inconsistency_submitted));
                else if (response.code() == 403) toast(getString(R.string.panic_not_participant));
                else toast(getString(R.string.something_went_wrong));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toast(getString(R.string.network_error));
            }
        });
    }

    // ---------- Helpers ----------

    private void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(poll);
        binding = null;
    }
}