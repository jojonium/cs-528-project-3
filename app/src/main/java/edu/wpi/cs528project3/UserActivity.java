package edu.wpi.cs528project3;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class UserActivity extends DetectedActivity{
    public String mTime;
    private UUID mID;

    public UserActivity(int i, int i1) {
        super(i, i1);
        mTime = getCurrentTime();
        mID = UUID.randomUUID();
    }

    private String getCurrentTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(cal.getTime());

    }

    public String getActivityString(){
        System.out.println(getType());
        switch (getType()) {
            case DetectedActivity.IN_VEHICLE: {
                return "in_vehicle";
            }
            case DetectedActivity.RUNNING: {
                return "running";
            }
            case DetectedActivity.STILL: {
                return "still";
            }
            case DetectedActivity.WALKING: {
                return "walking";
            }
            default:
                return "unknown_activity";
        }
    }

    public String getTime(){ return mTime; }

    public UUID getId(){ return mID;}
}
