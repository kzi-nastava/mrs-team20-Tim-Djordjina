package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityRideRequestBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.RideRequest;
import com.example.team20_tim_djordjina.model.RideResponse;
import com.example.team20_tim_djordjina.model.RideStopRequest;
import com.google.gson.Gson;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Ride request screen, using OpenStreetMap */
public class RideRequestActivity extends AppCompatActivity {

    private enum Mode {PICKUP, DESTINATION, STOP}

    private ActivityRideRequestBinding binding;
    private ApiService apiService;

    private Mode mode = Mode.PICKUP;

    private GeoPoint pickupPoint;
    private GeoPoint destinationPoint;
    private final List<GeoPoint> stopPoints = new ArrayList<>();

    private Marker pickupMarker;
    private Marker destinationMarker;
    private final List<Marker> stopMarkers = new ArrayList<>();

    private Calendar scheduledTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

        // osmdroid configuration
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue("RideOn-team20-kzi");

        binding = ActivityRideRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        setupMap();
        setupVehicleSpinner();
        updateModHint();

        binding.btnModePickup.setOnClickListener(v -> setMode(Mode.PICKUP));
        binding.btnModeDestination.setOnClickListener(v -> setMode(Mode.DESTINATION));
        binding.btnModeAddStop.setOnClickListener(v -> setMode(Mode.STOP));
        binding.btnRequestRide.setOnClickListener(v -> requestRide());
        binding.cbScheduleLater.setOnCheckedChangeListener((btn, checked) -> {
            binding.scheduleRow.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (!checked) {
                scheduledTime = null;
                binding.tvScheduledTime.setText(R.string.no_time_selected);
            }
        });
        binding.btnPickTime.setOnClickListener(v -> pickDateTime());

    }

    private void setupMap() {
        binding.map.setTileSource(TileSourceFactory.OpenTopo);
        binding.map.setMultiTouchControls(true);
        binding.map.getController().setZoom(14.0);
        binding.map.getController().setCenter(new GeoPoint(45.2671, 19.8335));  // Novi Sad

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                handleTap(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        binding.map.getOverlays().add(new MapEventsOverlay(receiver));

    }

    private void setupVehicleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.vehicle_types, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRideVehicleType.setAdapter(adapter);
    }

    private void setMode(Mode m) {
        mode = m;
        updateModHint();
    }
    private void updateModHint() {
        int res;
        switch (mode) {
            case DESTINATION: res = R.string.tap_to_set_destination; break;
            case STOP: res = R.string.tap_to_add_stop; break;
            case PICKUP:
            default: res = R.string.tap_to_set_pickup; break;
        }
        binding.tvMapHint.setText(res);
    }

    private void handleTap(GeoPoint p) {
        switch (mode) {
            case PICKUP:
                pickupPoint = p;
                pickupMarker = placeMarker(pickupMarker, p, getString(R.string.pickup));
                if (TextUtils.isEmpty(text(binding.etPickupAddress.getText()))) {
                    binding.etPickupAddress.setText(formatCoords(p));
                }
                break;
            case DESTINATION:
                destinationPoint = p;
                destinationMarker = placeMarker(destinationMarker, p, getString(R.string.destination));
                if (TextUtils.isEmpty(text(binding.etDestinationAddress.getText()))) {
                    binding.etDestinationAddress.setText(formatCoords(p));
                }
                break;
            case STOP:
                stopPoints.add(p);
                Marker m = placeMarker(null, p, getString(R.string.stop) + stopPoints.size());
                stopMarkers.add(m);
                binding.tvStopCount.setText(getString(R.string.stops_added, stopPoints.size()));
                break;
        }
        binding.map.invalidate();
    }

    /** Adds or moves a marker; returns the new marker */
    private Marker placeMarker(Marker existing, GeoPoint p, String title) {
        if (existing != null) {
            binding.map.getOverlays().remove(existing);
        }
        Marker marker = new Marker(binding.map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        binding.map.getOverlays().add(marker);
        return marker;
    }

    private void requestRide() {
        hideError();

        if (pickupPoint == null) { showError(getString(R.string.set_pickup_first)); return; }
        if (destinationPoint == null) { showError(getString(R.string.set_destination_first)); return; }

        String pickupAddress = orCoords(text(binding.etPickupAddress.getText()), pickupPoint);
        String destinationAddress = orCoords(text(binding.etDestinationAddress.getText()), destinationPoint);

        List<RideStopRequest> stops = new ArrayList<>();
        for (int i = 0; i < stopPoints.size(); i++) {
            GeoPoint sp = stopPoints.get(i);
            stops.add(new RideStopRequest(formatCoords(sp), sp.getLatitude(), sp.getLongitude(), i));
        }

        String scheduledFor = null;
        if (binding.cbScheduleLater.isChecked()) {
            if (scheduledTime == null) { showError(getString(R.string.pick_a_time)); return;}
            long diffMs = scheduledTime.getTimeInMillis() - System.currentTimeMillis();
            if (diffMs <= 0) { showError(getString(R.string.time_must_be_future)); return;}
            if (diffMs > 5 * 60 * 60 * 1000L) { showError(getString(R.string.max_5h_ahead)); return;}
            scheduledFor = isoString(scheduledTime);
        }

        RideRequest request = new RideRequest(
                pickupAddress, pickupPoint.getLatitude(), pickupPoint.getLongitude(),
                destinationAddress, destinationPoint.getLatitude(), destinationPoint.getLongitude(),
                stops,
                binding.spinnerRideVehicleType.getSelectedItem().toString(),
                binding.cbBabyTransport.isChecked(),
                binding.cbPetTransport.isChecked(), scheduledFor
        );

        setLoading(true);
        apiService.requestRide(request).enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showResult(response.body());
                } else {
                    showError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error));
            }
        });
    }

    private void showResult(RideResponse r) {
        if ("SCHEDULED".equals(r.getStatus())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ride_scheduled)
                    .setMessage(r.getMessage() != null ? r.getMessage()
                            : getString(R.string.ride_scheduled_msg))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (d, w) -> finish())
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.fare_label, r.getFare())).append("\n");
        sb.append(getString(R.string.distance_label, r.getDistanceKm())).append("\n");
        if (r.getDriver() != null) {
            sb.append(getString(R.string.driver_label, r.getDriver().getFullName())).append("\n");
            sb.append(getString(R.string.vehicle_label,
                    r.getDriver().getVehicleModel(), r.getDriver().getLicensePlate()));
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.ride_assigned)
                .setMessage(sb.toString())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (d, w) -> finish())
                .show();
    }

    private String parseError(Response<RideResponse> response) {
        String bodyStr = "";
        try {
            if (response.errorBody() != null) { bodyStr = response.errorBody().string(); }
        } catch (Exception ignored) {}
        android.util.Log.e("RideRequest", "requestRide failed: HTTP "
            + response.code() + " body=" + bodyStr);

        try {
            ApiResponse err = new Gson().fromJson(bodyStr, ApiResponse.class);
            if (err != null && err.getMessage() != null && !err.getMessage().isEmpty()) {
                return err.getMessage();
            }

        } catch (Exception ignored) {}
        if (response.code() == 409) return getString(R.string.no_drivers_available);
        if (response.code() == 403) return getString(R.string.cannot_order_ride);
        return getString(R.string.something_went_wrong);
    }

    private void pickDateTime() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    TimePickerDialog timeDialog = new TimePickerDialog(this,
                            (tView, hour, minute) -> {
                                Calendar chosen = Calendar.getInstance();
                                chosen.set(year, month, day, hour, minute, 0);
                                scheduledTime = chosen;
                                binding.tvScheduledTime.setText(
                                        String.format(Locale.US, "%04d-%02d-%02d %02d:%02d",
                                                year, month + 1, day, hour, minute));
                            },
                            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                    timeDialog.show();
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dateDialog.getDatePicker().setMinDate(now.getTimeInMillis());
        dateDialog.show();
    }

    /** Builds an ISO-8601 LocalDateTime string that backend can parse */
    private String isoString(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02dT%02d:%02d:00",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE));
    }

    // ---------- Helpers ----------

    private String formatCoords(GeoPoint p){
        return String.format("%.5f, %.5f", p.getLatitude(), p.getLongitude());
    }
    private String orCoords(String value, GeoPoint p){
        return TextUtils.isEmpty(value) ? formatCoords(p) : value;
    }
    private String text(CharSequence cs) { return cs == null ? "" : cs.toString().trim(); }

    private void setLoading(boolean loading) {
        binding.progressRide.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRequestRide.setEnabled(!loading);
    }

    private void showError(String message){
        binding.tvRideError.setText(message);
        binding.tvRideError.setVisibility(View.VISIBLE);
    }
    private void hideError(){
        binding.tvRideError.setText("");
        binding.tvRideError.setVisibility(View.GONE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        binding.map.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        binding.map.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        binding = null;
    }
}