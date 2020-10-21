package edu.wpi.cs528project3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.wpi.cs528project3.database.UserActivityBaseHelper;
import edu.wpi.cs528project3.database.UserActivityDbSchema;

public class UserActivityDBClient {
    private static UserActivityDBClient sUserActivityDBClient;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static UserActivityDBClient get(Context context) {
        if (sUserActivityDBClient == null) {
            sUserActivityDBClient = new UserActivityDBClient(context);
        }
        return sUserActivityDBClient;
    }

    private UserActivityDBClient(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new UserActivityBaseHelper(mContext)
                .getWritableDatabase();
    }


    public void addUserActivity(UserActivity a) {
        ContentValues values = getContentValues(a);

        mDatabase.insert(UserActivityDbSchema.UserActivityTable.NAME, null, values);
    }

//    public List<UserActivity> getActivities() {
//        List<UserActivity> activities = new ArrayList<>();
//
//        CrimeCursorWrapper cursor = queryActivities(null, null);
//
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            crimes.add(cursor.getCrime());
//            cursor.moveToNext();
//        }
//        cursor.close();
//
//        return activities;
//    }
//
    private static ContentValues getContentValues(UserActivity activity) {
        ContentValues values = new ContentValues();
        values.put(UserActivityDbSchema.UserActivityTable.Cols.UUID, activity.getId().toString());
        values.put(UserActivityDbSchema.UserActivityTable.Cols.TITLE, activity.getActivityString());
        values.put(UserActivityDbSchema.UserActivityTable.Cols.TIME, activity.getTime());
        System.out.println(values);
        return values;
    }

//    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
//        Cursor cursor = mDatabase.query(
//                CrimeTable.NAME,
//                null, // Columns - null selects all columns
//                whereClause,
//                whereArgs,
//                null, // groupBy
//                null, // having
//                null  // orderBy
//        );
//
//        return new CrimeCursorWrapper(cursor);
//    }
}
