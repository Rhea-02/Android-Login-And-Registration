package org.snowcorp.login.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    // A tag for log messages
    private static final String TAG = "DatabaseHandler";

    // Database Version
    private static final int DATABASE_VERSION = 1;
//    private static final int DATABASE_VERSION = 4;

    // Database Name
    public static final String DATABASE_NAME = "User";

    // User table name
    public static final String TABLE_NAME="User_Requests";

    // User Table Columns names
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_AFFILIATION = "affiliation";
    public static final String KEY_ID = "id";
    public static final String KEY_IS_APPROVED = "isApproved";
    public static final String KEY_SEND_EMAIL = "sendemail";
    public static final String KEY_SEND_OTP = "sendotp";

    // Constructor
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT,"
                + KEY_EMAIL + " TEXT,"
                + KEY_USERNAME + " TEXT,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_AFFILIATION + " TEXT,"
                + KEY_IS_APPROVED + " INTEGER DEFAULT 0,"
                + KEY_SEND_EMAIL + " TEXT,"
                + KEY_SEND_OTP + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
        Log.e(TAG, "Table created with query: " + CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.e(TAG, "Database upgraded from version " + oldVersion + " to " + newVersion);
    }
    /*public void createUserWithEmailAndPassword(String name, String phone, String email, String username, String password, String affiliation, String isApproved) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_PHONE, phone);
        values.put(KEY_EMAIL, email);
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_AFFILIATION, affiliation);
        values.put(KEY_IS_APPROVED, isApproved.equals("1"));

        long res = db.insert(TABLE_NAME, null, values);
        if (res == -1) {
            Log.e(TAG, "Error inserting new user");
        } else {
            Log.i(TAG, "User inserted with ID: " + res);
        }
    }*/

    public void createUserWithEmailAndPassword(String name, String phone, String email, String username, String password, String affiliation, String isApproved,String sendemail,int otp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_PHONE, phone);
        values.put(KEY_EMAIL, email);
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_AFFILIATION, affiliation);
        values.put(KEY_IS_APPROVED, isApproved.equals("1"));
        values.put(KEY_SEND_EMAIL, sendemail);
        values.put(KEY_SEND_OTP, otp);

        long res = db.insert(TABLE_NAME, null, values);
        if (res == -1) {
            Log.e(TAG, "Error inserting new user");
        } else {
            Log.i(TAG, "User inserted with ID: " + res);
        }
    }


    // Getting single user
    public User getUserDetails(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL, KEY_USERNAME, KEY_PASSWORD, KEY_AFFILIATION ,KEY_IS_APPROVED},
                KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_AFFILIATION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_APPROVED)) == 1 ? "1" : "0",
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEND_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEND_OTP))

            );
            cursor.close();
            return user;
        } else {
            if (cursor != null) {
                cursor.close();
            }
            Log.e(TAG, "User not found with ID: " + id);
            return null;
        }
    }

    // Getting all users
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // User class
    public class User {
        private int id;
        private String name, phone, email, username, password, affiliation, isApproved,sendemail,otp;

        public User(int id, String name, String phone, String email, String username, String password, String affiliation, String isApproved, String sendemail, String otp) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.username = username;
            this.password = password;
            this.affiliation = affiliation;
            this.isApproved = isApproved;
            this.sendemail = sendemail;
            this.otp = otp;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getAffiliation() { return affiliation; }
        public String isApproved() { return isApproved;}

        public String getSendemail() {
            return sendemail;
        }

        public void setSendemail(String sendemail) {
            this.sendemail = sendemail;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }
    }
}
