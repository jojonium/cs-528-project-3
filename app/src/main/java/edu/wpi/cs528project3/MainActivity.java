package edu.wpi.cs528project3;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

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
//    private final int dwellTime = 15000;
    private final int dwellTime = 1000;

    private int fullerVisits = 0;
    private int libraryVisits = 0;
    private final String fullerKey = "fuller";
    private final String libraryKey = "library";

    private final String geofenceToast = "You have been inside the %s Geofence for 15 seconds, incrementing counter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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

        Geofence fuller = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(fullerKey)

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
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(libraryKey)

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

    }

    private void updateGeofencesUI() {
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

        String locationProvider = LocationManager.NETWORK_PROVIDER;
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        if (mCurrentLocation != null) {
            updateMap(mCurrentLocation);
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom));
        }
    }

    private void updateMap(Location location) {
        LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, defaultZoom));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        updateGeofencesUI();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

    public class GeofenceBroadcastReceiver extends BroadcastReceiver {
        // ...
        public void onReceive(Context context, Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
//                String errorMessage = GeofenceStatusCodes.getErrorString(geofencingEvent.getErrorCode());
//                Log.e("geofenceBroadcastReceiver", errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                for (Geofence fence : triggeringGeofences) {
                    if (fence.getRequestId().equals(fullerKey)) {
                        Toast.makeText(context, String.format(geofenceToast, "Fuller"), Toast.LENGTH_LONG).show();
                        fullerVisits += 1;
                        updateGeofencesUI();
                    } else {
                        Toast.makeText(context, String.format(geofenceToast, "Gordon Library"), Toast.LENGTH_LONG).show();
                        libraryVisits += 1;
                        updateGeofencesUI();
                    }
                }

            }
        }
    }


}