package edu.wpi.cs528project3.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserActivityBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "UserActivityBaseHelper";
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "userActivityBase.db";

    public UserActivityBaseHelper(Context context) { super(context, DATABASE_NAME, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + UserActivityDbSchema.UserActivityTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                UserActivityDbSchema.UserActivityTable.Cols.UUID + ", " +
                UserActivityDbSchema.UserActivityTable.Cols.TITLE + ", " +
                UserActivityDbSchema.UserActivityTable.Cols.TIME + ", " +
                UserActivityDbSchema.UserActivityTable.Cols.TIMESTAMP +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
