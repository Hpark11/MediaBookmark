package hpark.instagramfollowers_prototype.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * Created by hpark_ipl on 2017. 8. 14..
 */

public class DatabaseManager {
    private SQLiteDatabase sqLiteDatabase;

    // Database
    public static final String databaseName = "InstaCheckers";

    // Tables
    public static final String shareGroupTableName = "ShareGroups";

    // Columns
    public static final String colShareGroupName = "ShareGroupName";
    public static final String colUsersInfo = "UsersInfo";
    public static final String colId = "ID";

    // Version
    public static final int databaseVersion = 1;

    // Table Creation
    public static final String createShareGroupTable = "Create table IF NOT EXISTS " + shareGroupTableName +
            "(ID integer PRIMARY KEY AUTOINCREMENT," + colUsersInfo + " text);";

    public DatabaseManager(Context context) {
        DatabaseManagerHelper dbHelper = new DatabaseManagerHelper(context);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    public long insertShareGroupValue(ContentValues values) {
        long id = sqLiteDatabase.insert(shareGroupTableName, "", values);
        return id;
    }

    public Cursor queryShareGroupValues(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(shareGroupTableName);
        Cursor cursor = queryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    public int deleteShareGroupValues(String selection, String[] selectionArgs) {
        int count = sqLiteDatabase.delete(shareGroupTableName, selection, selectionArgs);
        return count;
    }

    public int updateShareGroupValue(ContentValues values, String selection,  String[] selectionArgs) {
        int count = sqLiteDatabase.update(shareGroupTableName, values, selection, selectionArgs);
        return count;
    }

    public static class DatabaseManagerHelper extends SQLiteOpenHelper {
        Context context;

        DatabaseManagerHelper(Context context) {
            super(context, databaseName, null, databaseVersion);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(createShareGroupTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP Table IF EXISTS " + shareGroupTableName);
            onCreate(db);
        }
    }
}
