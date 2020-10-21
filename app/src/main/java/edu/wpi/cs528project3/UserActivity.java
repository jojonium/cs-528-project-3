package edu.wpi.cs528project3;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class UserActivity extends DetectedActivity{
    private String mTime;
    private long mTimestamp;
    private UUID mID;

    public UserActivity(int i, int i1) {
        super(i, i1);
        mTime = getCurrentTime();
        mTimestamp = System.currentTimeMillis();
        mID = UUID.randomUUID();
    }

    private String getCurrentTime() {
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

    public String getTotalTimeString(long fromTime){
        long durationInMillis = fromTime - mTimestamp;

        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d seconds", second);
        if (minute > 0 || hour > 0) {
            time = String.format("%02d minutes, ", minute ) + time;
        }
        if ( hour > 0) {
            time = String.format("%02d hours, ", hour) + time;
        }
        return time;
    }

    public String getTime(){ return mTime; }

    public UUID getId(){ return mID;}

    public long getTimestamp(){ return mTimestamp; }
}
