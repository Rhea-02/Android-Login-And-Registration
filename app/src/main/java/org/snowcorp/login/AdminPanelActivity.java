package org.snowcorp.login;


// Import necessary libraries
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

// Define the AdminPanelActivity class which extends AppCompatActivity
public class AdminPanelActivity extends AppCompatActivity {

    // Declare private variables for Firestore database, ListView, ArrayList and ArrayAdapter
    private FirebaseFirestore db;
    private ListView listView;
    private ArrayList<String> userList;
    private UserAdapter adapter;

    // Override the onCreate method which is called when the activity is starting
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Initialize Firestore database
        db = FirebaseFirestore.getInstance();

        // Initialize ListView, ArrayList and ArrayAdapter
        listView = findViewById(R.id.listView);
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        // Call getUsers method to retrieve users from Firestore
        getUsers();
    }

    // Method to retrieve users from Firestore
    private void getUsers() {
        // Access 'User_Requests' collection in Firestore
        db.collection("User_Requests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loop through the documents in the task result
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Check if user is approved
                            boolean isApproved = document.getBoolean("isApproved");
                            // If user is not approved, add them to the userList
                            if (!isApproved) {
                                String email = document.getString("email");
                                userList.add(email);
                            }
                        }
                        // Notify the adapter that the underlying data has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle any errors
                    }
                });
    }

    // Custom ArrayAdapter class
    class UserAdapter extends ArrayAdapter<String> {

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
    }

    // Method to approve a user
    public void approveUser(String email) {
        // Implement your logic for approving a user here
        // Access 'User_Requests' collection in Firestore where email equals selected email
        db.collection("User_Requests")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loop through the documents in the task result
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get document ID
                            String id = document.getId();
                            // Update 'isApproved' field in Firestore to true
                            db.collection("User_Requests").document(id)
                                    .update("isApproved", true)
                                    .addOnSuccessListener(aVoid -> {
                                        // Remove email from userList and notify the adapter
                                        userList.remove(email);
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    } else {
                        // Handle any errors
                    }
                });

    }

    // Method to reject a user
    public void rejectUser(String email) {
        // Implement your logic for rejecting a user here
        // Access 'User_Requests' collection in Firestore where email equals selected email
        db.collection("User_Requests")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loop through the documents in the task result
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get document ID
                            String id = document.getId();
                            // Delete document from Firestore
                            db.collection("User_Requests").document(id)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Remove email from userList and notify the adapter
                                        userList.remove(email);
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    } else {
                        // Handle any errors
                    }
                });


    }
}

//// Import necessary libraries
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.ArrayAdapter;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.ArrayList;
//
//// Define the AdminPanelActivity class which extends AppCompatActivity
//public class AdminPanelActivity extends AppCompatActivity {
//
//    // Declare private variables for Firestore database, ListView, ArrayList and ArrayAdapter
//    private FirebaseFirestore db;
//    private ListView listView;
//    private ArrayList<String> userList;
//    private ArrayAdapter<String> adapter;
//
//    // Override the onCreate method which is called when the activity is starting
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_panel);
//
//        // Initialize Firestore database
//        db = FirebaseFirestore.getInstance();
//
//        // Initialize ListView, ArrayList and ArrayAdapter
//        listView = findViewById(R.id.listView);
//        userList = new ArrayList<>();
//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
//        listView.setAdapter(adapter);
//
//        // Call getUsers method to retrieve users from Firestore
//        getUsers();
//
//        // Initialize approve button and set onClickListener
//        Button approveButton = findViewById(R.id.button_approve);
//        approveButton.setOnClickListener(this::approveUser);
//
//        // Initialize reject button and set onClickListener
//        Button rejectButton = findViewById(R.id.button_reject);
//        rejectButton.setOnClickListener(this::rejectUser);
//    }
//
//    // Method to retrieve users from Firestore
//    private void getUsers() {
//        // Access 'User_Requests' collection in Firestore
//        db.collection("User_Requests")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Loop through the documents in the task result
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            // Check if user is approved
//                            boolean isApproved = document.getBoolean("isApproved");
//                            // If user is not approved, add them to the userList
//                            if (!isApproved) {
//                                String email = document.getString("email");
//                                userList.add(email);
//                            }
//                        }
//                        // Notify the adapter that the underlying data has changed
//                        adapter.notifyDataSetChanged();
//                    } else {
//                        // Handle any errors
//                    }
//                });
//    }
//
//    // Method to approve a user
//    public void approveUser(View view) {
//        // Get selected email from ListView
//        String email = (String) listView.getSelectedItem();
//        // Access 'User_Requests' collection in Firestore where email equals selected email
//        db.collection("User_Requests")
//                .whereEqualTo("email", email)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Loop through the documents in the task result
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            // Get document ID
//                            String id = document.getId();
//                            // Update 'isApproved' field in Firestore to true
//                            db.collection("User_Requests").document(id)
//                                    .update("isApproved", true)
//                                    .addOnSuccessListener(aVoid -> {
//                                        // Remove email from userList and notify the adapter
//                                        userList.remove(email);
//                                        adapter.notifyDataSetChanged();
//                                    });
//                        }
//                    } else {
//                        // Handle any errors
//                    }
//                });
//    }
//
//    // Method to reject a user
//    public void rejectUser(View view) {
//        // Get selected email from ListView
//        String email = (String) listView.getSelectedItem();
//        // Access 'User_Requests' collection in Firestore where email equals selected email
//        db.collection("User_Requests")
//                .whereEqualTo("email", email)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Loop through the documents in the task result
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            // Get document ID
//                            String id = document.getId();
//                            // Delete document from Firestore
//                            db.collection("User_Requests").document(id)
//                                    .delete()
//                                    .addOnSuccessListener(aVoid -> {
//                                        // Remove email from userList and notify the adapter
//                                        userList.remove(email);
//                                        adapter.notifyDataSetChanged();
//                                    });
//                        }
//                    } else {
//                        // Handle any errors
//                    }
//                });
//    }
//}
