package edu.wpi.cs528project3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
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
                String geofenceToast = context.getResources().getString(R.string.geofence_toast);
                if (fence.getRequestId().equals(context.getResources().getString(R.string.fuller))) {
                    SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.geofence_prefs_file), MODE_PRIVATE);
                    int fullerVisits = prefs.getInt(context.getResources().getString(R.string.fuller_visits), 0) + 1;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(context.getResources().getString(R.string.fuller_visits), fullerVisits);
                    editor.apply();
                    Toast.makeText(context, String.format(geofenceToast, "Fuller"), Toast.LENGTH_LONG).show();
                    context.sendBroadcast(new Intent("GEOFENCE_UPDATE"));
                } else if (fence.getRequestId().equals(context.getResources().getString(R.string.library))) {
                    SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.geofence_prefs_file), MODE_PRIVATE);
                    int libraryVisits = prefs.getInt(context.getResources().getString(R.string.library_visits), 0) + 1;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(context.getResources().getString(R.string.library_visits), libraryVisits);
                    editor.apply();
                    Toast.makeText(context, String.format(geofenceToast, "Gordon Library"), Toast.LENGTH_LONG).show();
                    context.sendBroadcast(new Intent("GEOFENCE_UPDATE"));
                }
            }

        }
    }

}
