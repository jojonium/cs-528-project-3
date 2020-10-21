package edu.wpi.cs528project3.database;

public class UserActivityDbSchema {
    public static final class UserActivityTable {
        public static final String NAME = "user_activities";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String TIME = "time";
        }
    }
}
