package edu.wpi.cs528project3;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class ActivityDetectService extends IntentService {

    protected static final String TAG = ActivityDetectService.class.getSimpleName();
    
    public ActivityDetectService(){
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        ArrayList<DetectedActivity> detectedActivities = new ArrayList<>(result.getProbableActivities());

        for (DetectedActivity activity : detectedActivities) {
            Log.i(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            broadCast(activity);
        }
    }

    private void broadCast(DetectedActivity activity) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
