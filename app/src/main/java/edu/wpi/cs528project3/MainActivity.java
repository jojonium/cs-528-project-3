package edu.wpi.cs528project3;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = MainActivity.class.getSimpleName();

    private StepCounter sc;

    private GoogleMap mMap;
    private Location mCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private GeofencingClient geofencingClient;
    private final LocationRequest locationRequest = LocationRequest.create().setInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private LocationCallback locationCallback;
    private PendingIntent geofencePendingIntent;

    private final LatLng defaultLocation = new LatLng(42.273762, -71.809470);
    private final LatLng fullerCoords = new LatLng(42.274885, -71.806627);
    private final LatLng libraryCoords = new LatLng(42.274254, -71.806589);

    private final float defaultZoom = 16;
    private final float geofenceRadius = 30;
    private final int dwellTime = 15000;
    private BroadcastReceiver activityUpdateReceiver;
    private BroadcastReceiver geofenceUpdateReceiver;
    private Geocoder geoCoder;
    private boolean locationPermissionDenied = false;
    MediaPlayer mediaPlayer;

    private final int GET_LAST_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final int ENABLE_MY_LOCATION_PERMISSION_REQUEST_CODE = 2;
    private final int START_LOCATION_UPDATES_PERMISSION_REQUEST_CODE = 3;
    private final int ENABLE_GEOFENCES_FOREGROUND_PERMISSION_REQUEST_CODE = 4;
    private final int ENABLE_GEOFENCES_BACKGROUND_PERMISSION_REQUEST_CODE = 5;

    private final int[] LOCATION_REQUEST_CODES = {GET_LAST_LOCATION_PERMISSION_REQUEST_CODE,
            ENABLE_MY_LOCATION_PERMISSION_REQUEST_CODE,
            START_LOCATION_UPDATES_PERMISSION_REQUEST_CODE,
            ENABLE_GEOFENCES_FOREGROUND_PERMISSION_REQUEST_CODE};

    private TextView txtActivity;
    private ImageView imgActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create step counter
        TextView textStepsCounter = (TextView) this.findViewById(R.id.stepsText);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        String textPattern = getResources().getString(R.string.steps_taken);
        sc = new StepCounter(sensorManager, textStepsCounter, textPattern);

        txtActivity = findViewById(R.id.activityDescription);
        imgActivity = findViewById(R.id.activityImage);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beat_02);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        enableForegroundLocationFeatures(GET_LAST_LOCATION_PERMISSION_REQUEST_CODE);
        geoCoder = new Geocoder(getApplicationContext());
        enableForegroundLocationFeatures(ENABLE_GEOFENCES_FOREGROUND_PERMISSION_REQUEST_CODE);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                mCurrentLocation = locationResult.getLastLocation();
                updateMap(mCurrentLocation);
            }
        };

        activityUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)){
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

        startTracking();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(geofenceUpdateReceiver);
    }

    private void handleUserActivity(int type, int confidence) {
        String label = "You are not still, walking, running, or in a car";
        Integer image = 0;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = "You are in a vehicle";
                image = R.drawable.in_vehicle;
                mediaPlayer.stop();cv 
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "You are running";
                image = R.drawable.running;
                mediaPlayer.start();
                break;
            }
            case DetectedActivity.STILL: {
                label = "You are still";
                image = R.drawable.still;
                mediaPlayer.stop();
                break;
            }
            case DetectedActivity.WALKING: {
                label = "You are walking";
                image = R.drawable.walking;
                mediaPlayer.start();
                break;
            }
            default:
                label = "You are not still, walking, running, or in a car";
                mediaPlayer.stop();
                break;
        }
        if (confidence > Constants.CONFIDENCE) {
            txtActivity.setText(label);
            imgActivity.setImageResource(image);
        }
    }


    public void updateGeofencesUI() {
        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.geofence_prefs_file), MODE_PRIVATE);
        int fullerVisits = prefs.getInt(getResources().getString(R.string.fuller_visits), 0);
        int libraryVisits = prefs.getInt(getResources().getString(R.string.library_visits), 0);
        TextView fullerText = findViewById(R.id.fullerLabsText);
        TextView libraryText = findViewById(R.id.libraryText);
        fullerText.setText(String.format(getResources().getString(R.string.fuller_labs_geofence), fullerVisits));
        libraryText.setText(String.format(getResources().getString(R.string.library_geofence), libraryVisits));
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest(Geofence g1, Geofence g2) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofence(g1);
        builder.addGeofence(g2);
        return builder.build();

    }

    private void enableForegroundLocationFeatures(int requestCode) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                PermissionUtils.requestPermission(this, requestCode,
                        Manifest.permission.ACCESS_FINE_LOCATION, false,
                        R.string.location_permission_required,
                        R.string.location_permission_rationale);
            }
        } else {
            if (requestCode == ENABLE_MY_LOCATION_PERMISSION_REQUEST_CODE) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else if (requestCode == START_LOCATION_UPDATES_PERMISSION_REQUEST_CODE) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                        Looper.getMainLooper());
            } else if (requestCode == GET_LAST_LOCATION_PERMISSION_REQUEST_CODE) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    mCurrentLocation = location;
                                }
                            }
                        });
            } else if (requestCode == ENABLE_GEOFENCES_FOREGROUND_PERMISSION_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= 29) {
                    enableBackgroundLocationFeatures(ENABLE_GEOFENCES_BACKGROUND_PERMISSION_REQUEST_CODE);
                } else {
                    Geofence fuller = new Geofence.Builder()
                            // Set the request ID of the geofence. This is a string to identify this
                            // geofence.
                            .setRequestId(getResources().getString(R.string.fuller))

                            .setCircularRegion(
                                    fullerCoords.latitude,
                                    fullerCoords.longitude,
                                    geofenceRadius
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setLoiteringDelay(dwellTime)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                            .build();

                    Geofence library = new Geofence.Builder()
                            .setRequestId(getResources().getString(R.string.library))

                            .setCircularRegion(
                                    libraryCoords.latitude,
                                    libraryCoords.longitude,
                                    geofenceRadius
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setLoiteringDelay(dwellTime)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                            .build();

                    geofencingClient.addGeofences(getGeofencingRequest(fuller, library), getGeofencePendingIntent());

                    geofenceUpdateReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            updateGeofencesUI();
                        }
                    };
                    registerReceiver(geofenceUpdateReceiver, new IntentFilter("GEOFENCE_UPDATE"));
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void enableBackgroundLocationFeatures(int requestCode) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT == 29) {
                PermissionUtils.requestPermission(this, requestCode,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION, false,
                        R.string.background_location_required,
                        R.string.location_permission_rationale);
            } else if (Build.VERSION.SDK_INT == 30) {
                PermissionUtils.requestPermission(this, requestCode,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION, false,
                        R.string.background_location_required,
                        R.string.background_location_rationale);
            }
        } else {
            Geofence fuller = new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(getResources().getString(R.string.fuller))

                    .setCircularRegion(
                            fullerCoords.latitude,
                            fullerCoords.longitude,
                            geofenceRadius
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(dwellTime)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build();

            Geofence library = new Geofence.Builder()
                    .setRequestId(getResources().getString(R.string.library))

                    .setCircularRegion(
                            libraryCoords.latitude,
                            libraryCoords.longitude,
                            geofenceRadius
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(dwellTime)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build();

            geofencingClient.addGeofences(getGeofencingRequest(fuller, library), getGeofencePendingIntent());
            geofenceUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateGeofencesUI();
                }
            };
            registerReceiver(geofenceUpdateReceiver, new IntentFilter("GEOFENCE_UPDATE"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ENABLE_MY_LOCATION_PERMISSION_REQUEST_CODE ||
                requestCode == START_LOCATION_UPDATES_PERMISSION_REQUEST_CODE ||
                requestCode == GET_LAST_LOCATION_PERMISSION_REQUEST_CODE ||
                requestCode == ENABLE_GEOFENCES_FOREGROUND_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
                for (int request : LOCATION_REQUEST_CODES) {
                    enableForegroundLocationFeatures(request);
                }
            }
//            else {
//                locationPermissionDenied = true;
//            }
        } else if (requestCode == ENABLE_GEOFENCES_BACKGROUND_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableBackgroundLocationFeatures(requestCode);
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableForegroundLocationFeatures(ENABLE_MY_LOCATION_PERMISSION_REQUEST_CODE);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);

        if (mCurrentLocation != null) {
            updateMap(mCurrentLocation);
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom));
        }
    }

    private void updateMap(Location location) {
        LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, defaultZoom));
        String bestMatch = "";
        try {
            List<Address> matches = geoCoder.getFromLocation(curLocation.latitude, curLocation.longitude, 1);
            bestMatch = (matches.isEmpty() ? "Unknown" : matches.get(0).getAddressLine(0));
        } catch (Exception e) {
            bestMatch = "Unknown";
        }
        TextView addressText = findViewById(R.id.addressText);
        addressText.setText(String.format(getResources().getString(R.string.address), bestMatch));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        // use shared preferences for saving fuller/library visits
        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.geofence_prefs_file), MODE_PRIVATE);
        int fullerVisits = prefs.getInt(getResources().getString(R.string.fuller_visits), 0);
        int libraryVisits = prefs.getInt(getResources().getString(R.string.library_visits), 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getResources().getString(R.string.fuller_visits), fullerVisits);
        editor.putInt(getResources().getString(R.string.library_visits), libraryVisits);
        editor.apply();
        updateGeofencesUI();
        LocalBroadcastManager.getInstance(this).registerReceiver(activityUpdateReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (locationPermissionDenied) {
            // Permission was not granted, display error dialog.
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(false, R.string.location_permission_denied,
                            R.string.location_permission_required)
                    .show(getSupportFragmentManager(), "dialog");
            locationPermissionDenied = false;
        }
    }

    private void startLocationUpdates() {
        enableForegroundLocationFeatures(START_LOCATION_UPDATES_PERMISSION_REQUEST_CODE);
    }


    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityUpdateReceiver);
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundActivityRecognition.class);
        startService(intent);
    }

}