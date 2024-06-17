package org.snowcorp.login;

//Import necessary libraries

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.helper.DatabaseHandler;
import org.snowcorp.login.helper.Functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import androidx.databinding.DataBindingUtil;
//import org.snowcorp.login.databinding.ActivityDrawerNavigationBinding;


// Define the AdminPanelActivity class which extends AppCompatActivity
public class AdminPanelActivity extends DrawerNavigationActivity {
    MaterialButton logout;
    //    private UserAdapter adapter;
    CustomAdapter adapter;
    //ActivityDrawerNavigationBinding activityDrawerNavigationBinding;
    //Declare private variables for SQLite database, ListView, ArrayList and ArrayAdapter
    private SQLiteDatabase db;
    private ListView listView;
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<String> emailList = new ArrayList<>();
    private ArrayList<Integer> statusList = new ArrayList<>();

    // Override the onCreate method which is called when the activity is starting
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        //activityDrawerNavigationBinding=ActivityDrawerNavigationBinding.inflate(getLayoutInflater());
        //setContentView(activityDrawerNavigationBinding.getRoot());

        // Initialize SQLite database
        DatabaseHandler dbHelper = new DatabaseHandler(this);
        db = dbHelper.getReadableDatabase();

        // Initialize ListView, ArrayList and ArrayAdapter
        listView = findViewById(R.id.listView);
        logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("login_response", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(AdminPanelActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        userList = new ArrayList<>();
//        adapter = new UserAdapter(this, userList);
//        listView.setAdapter(adapter);

        // Call getUsers method to retrieve users from SQLite database
        getUsers();
    }

    private void showDialog(String title) {
        Functions.showProgressDialog(AdminPanelActivity.this, title);
    }

    private void hideDialog() {
        Functions.hideProgressDialog(AdminPanelActivity.this);
    }


    // Method to retrieve users from SQLite database
    private void getUsers() {
        String tag_string_req = "req_login";
        showDialog("Loading ...");
        StringRequest strReq = new StringRequest(Request.Method.GET, Functions.USER_LIST_URL, response -> {
            Log.e("TAG", "Login Response----------: " + response);
            hideDialog();
            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("success");

                if (error) {
                    JSONArray dataArray = jObj.getJSONArray("data");

                    userList.clear();
                    emailList.clear();
                    statusList.clear();

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject userObject = dataArray.getJSONObject(i);
                        String name = userObject.getString("name");
                        String email = userObject.getString("email");
                        int status = userObject.getInt("status");

                        userList.add(name);
                        emailList.add(email);
                        statusList.add(status);

                        adapter = new CustomAdapter(getBaseContext(), userList,emailList,statusList);
                        listView.setAdapter(adapter);

                    }
                    adapter.notifyDataSetChanged();
                } else {
                    String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Log.e("TAG", "Login Error: " + error.getMessage());
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }
        };

        addRequestToQueue(strReq, tag_string_req);
    }

    public void addRequestToQueue(StringRequest strReq, String tag_string_req) {
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    // Method to approve a user
    public void approveUser(String email) {
        // Implement your logic for approving a user here
        /*DatabaseHandler dbHelper = new DatabaseHandler(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define selection and selectionArgs for the query
        String selection = DatabaseHandler.KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.KEY_IS_APPROVED, true);

        // Update 'isApproved' field in SQLite database to true
        int count = db.update(
                DatabaseHandler.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // If update successful, remove email from userList and notify the adapter
        if (count > 0) {
            userList.remove(email);
            adapter.notifyDataSetChanged();
        }*/
    }

    // Method to reject a user
    public void rejectUser(String email) {
        // Implement your logic for rejecting a user here
       /* DatabaseHandler dbHelper = new DatabaseHandler(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define selection and selectionArgs for the query
        String selection = DatabaseHandler.KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        // Delete user from SQLite database
        int deletedRows = db.delete(DatabaseHandler.TABLE_NAME, selection, selectionArgs);

        // If delete successful, remove email from userList and notify the adapter
        if (deletedRows > 0) {
            userList.remove(email);
            adapter.notifyDataSetChanged();
        }*/
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    // Custom ArrayAdapter class
    /*class UserAdapter extends ArrayAdapter<String> {

        UserAdapter(AppCompatActivity context, ArrayList<String> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String email = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            // Lookup view for data population
            TextView textEmail = convertView.findViewById(R.id.text_email);
            ImageButton buttonApprove = convertView.findViewById(R.id.button_approve);
            ImageButton buttonReject = convertView.findViewById(R.id.button_reject);

            // Populate the data into the template view using the data object
            textEmail.setText(email);

            // Attach click listeners to the approve and reject buttons
            buttonApprove.setOnClickListener(view -> approveUser(email));
            buttonReject.setOnClickListener(view -> rejectUser(email));

            // Return the completed view to render on screen
            return convertView;
        }
    }*/

    public class CustomAdapter extends ArrayAdapter<String> {
        TextView nameTextView, text_name;
        private Context context;
        private ArrayList<String> names;
        private ArrayList<String> emails;
        private ArrayList<Integer> statuses; // Add this line

        public CustomAdapter(Context context, ArrayList<String> names, ArrayList<String> emails, ArrayList<Integer> statuses) {
            super(context, R.layout.list_item, names);
            this.context = context;
            this.names = names;
            this.emails = emails;
            this.statuses = statuses; // Add this line
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item, parent, false);

            nameTextView = rowView.findViewById(R.id.text_email);

            Button button_approve = rowView.findViewById(R.id.button_approve);
            Button button_reject = rowView.findViewById(R.id.button_reject);
            text_name = rowView.findViewById(R.id.text_name);

            button_approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callapi( emails.get(position),"approve");
                    Log.e("TAG", "onClick approve: " + emails.get(position));
                }
            });

            button_reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callapi( emails.get(position),"reject");
                    Log.e("TAG", "onClick reject: " + emails.get(position));
                }
            });

            nameTextView.setText(names.get(position));
            text_name.setText(emails.get(position));

            int status = statuses.get(position);
            if (status == 1) {
                button_approve.setEnabled(false);
                button_reject.setEnabled(false);
                button_approve.setVisibility(View.INVISIBLE);
                button_reject.setText("APPROVED");
            } else if (status == 2) {
                button_approve.setEnabled(false);
                button_reject.setEnabled(false);
                button_approve.setVisibility(View.INVISIBLE);
                button_reject.setText("REJECTED");
            } else {
                button_approve.setEnabled(true);
                button_reject.setEnabled(true);
            }

            return rowView;
        }

        public void callapi(String email,String action) {
            String tag_string_req = "req_login";
            showDialog("Loading ...");
            StringRequest strReq = new StringRequest(Request.Method.POST, Functions.STATUS_URL, response -> {
                Log.e("TAG", "Login Response----------: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);

                    boolean error = jObj.getBoolean("success");


                    Log.e("TAG", "error----------: " + error);

                    if (error) {
                        notifyDataSetChanged();
                        getUsers();
                    } else {
                        // Handle response when there's no error
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, error -> {
                Log.e("TAG", "Login Error: " + error.getMessage());
                hideDialog();
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("tag", action);
                    return params;
                }
            };

            addRequestToQueue(strReq, tag_string_req);
        }

    }
}
