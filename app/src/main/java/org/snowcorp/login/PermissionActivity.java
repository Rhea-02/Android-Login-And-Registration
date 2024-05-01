// This is the package name for the Java class.
package org.snowcorp.login;

// These are the import statements. They include the libraries and classes that are used in this code.
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.util.Log;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.AdapterView;
import com.google.firebase.firestore.WriteBatch;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseUser;

// This is the main class for the PermissionActivity. It extends AppCompatActivity, which is a base class for activities that use the support library action bar features.
public class PermissionActivity extends AppCompatActivity {
    // Add the TAG here
    private static final String TAG = PermissionActivity.class.getSimpleName();
    //private DocumentReference mDocRef =FirebaseFirestore.getInstance().document("User_Requests");
    // These are private instance variables for the FirebaseAuth, FirebaseFirestore, and several EditText and AutoCompleteTextView objects.
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextUsername;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private AutoCompleteTextView editTextAffiliation;

    // The onCreate method is called when the activity is starting. This is where most initialization happens.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This method call sets the activity content from a layout resource.
        setContentView(R.layout.activity_permission);

        // Initialize Firebase Auth and Firestore instances.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find the button by its ID and set an OnClickListener to it. When the button is clicked, it starts the LoginActivity.
        Button buttonGoToLogin = findViewById(R.id.buttonGoToLogin);
        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Find the EditText fields by their IDs.
        TextInputLayout textInputLayoutUsername = findViewById(R.id.editTextUsername);
        EditText editTextUsername = textInputLayoutUsername.getEditText();

        TextInputLayout textInputLayoutPhone = findViewById(R.id.editTextPhone);
        EditText editTextPhone = textInputLayoutPhone.getEditText();

        TextInputLayout textInputLayoutEmail = findViewById(R.id.editTextEmail);
        EditText editTextEmail = textInputLayoutEmail.getEditText();

        TextInputLayout textInputLayoutName = findViewById(R.id.editTextName);
        EditText editTextName = textInputLayoutName.getEditText();

        TextInputLayout textInputLayoutPassword = findViewById(R.id.editTextPassword);
        EditText editTextPassword = textInputLayoutPassword.getEditText();

        TextInputLayout textInputLayoutAffiliation = findViewById(R.id.editTextAffiliation);
        AutoCompleteTextView editTextAffiliation = findViewById(R.id.autoCompleteTextView);


        // Set up the AutoCompleteTextView for the affiliation field with an ArrayAdapter.
        String[] affiliations = new String[]{"Academic Officer", "Scientific Officer", "Technical Assistant", "Auxillary Team", "Visiting Fellows", "Students", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, affiliations);
        editTextAffiliation.setAdapter(adapter);

        // Set an OnItemClickListener to the AutoCompleteTextView. If the selected item is "Other", show another EditText for the user to type in.
        editTextAffiliation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Other")) {
                    findViewById(R.id.editTextOtherAffiliation).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.editTextOtherAffiliation).setVisibility(View.GONE);
                }
            }
        });

        // Find the register button by its ID and set an OnClickListener to it. When the button is clicked, it gets the text from the EditText fields,
        // registers the user in Firebase Authentication, and stores the additional fields in Firestore,simultaneously.


//        Button buttonRegister = findViewById(R.id.buttonRegister);
//        buttonRegister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String username = editTextUsername.getText().toString();
//                final String phone = editTextPhone.getText().toString();
//                final String email = editTextEmail.getText().toString();
//                final String name = editTextName.getText().toString();
//                final String password = editTextPassword.getText().toString();
//                 String affiliation = editTextAffiliation.getText().toString();
//
//// If the selected affiliation is "Other", get the manually typed affiliation
//                if (affiliation.equals("Other")) {
//                    TextInputLayout textInputOtherAffiliation = findViewById(R.id.editTextOtherAffiliation);
//                    affiliation = textInputOtherAffiliation.getEditText().getText().toString();
//                }
//
//                final String finalAffiliation = affiliation;
//
//                mAuth.createUserWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//
//                                    // OTPs are generated on a random basis to be sent to the Ph No. and email of registered user
//                                    Random rand = new Random();
//                                    int otpForSms = rand.nextInt(900000) + 100000;
//                                    int otpForEmail = rand.nextInt(900000) + 100000;
//
//                                    System.out.println("Generated OTP for SMS: " + otpForSms);
//                                    System.out.println("Generated OTP for Email: " + otpForEmail);
//
//                                    // ...
//                                } else {
//                                    // OTP Generation failed
//                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                    Toast.makeText(PermissionActivity.this, "OTP Generation Failed",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                                }
//                            }
//                        });
//
//                                    // Create a new user with the given fields
//                                    Map<String, Object> userMap = new HashMap<>();
//                                    userMap.put("name", name);
//                                    userMap.put("phone", phone);
//                                    userMap.put("username", username);
//                                    userMap.put("affiliation", finalAffiliation);
//                                    userMap.put("isApproved", false);
//
//                                    // Add a new document with a generated ID
//                                    db.collection("User_Requests")
//                                            .document(user.getUid())
//                                            .set(userMap)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + user.getUid());
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.w(TAG, "Error adding document", e);
//                                                }
//                                            });
//                                } else {
//                                    // If sign up fails, display a message to the user.
//                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                    Toast.makeText(PermissionActivity.this, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//            }
//        });
//    };
//
//
//}
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = editTextUsername.getText().toString();
                final String phone = editTextPhone.getText().toString();
                final String email = editTextEmail.getText().toString();
                final String name = editTextName.getText().toString();
                final String password = editTextPassword.getText().toString();
                String affiliation = editTextAffiliation.getText().toString();

                // If the selected affiliation is "Other", get the manually typed affiliation
                if (affiliation.equals("Other")) {
                    TextInputLayout textInputOtherAffiliation = findViewById(R.id.editTextOtherAffiliation);
                    affiliation = textInputOtherAffiliation.getEditText().getText().toString();
                }

                final String finalAffiliation = affiliation;

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Generate OTPs
                                    Random rand = new Random();
                                    int otpForSms = rand.nextInt(900000) + 100000;
                                    int otpForEmail = rand.nextInt(900000) + 100000;

                                    System.out.println("Generated OTP for SMS: " + otpForSms);
                                    System.out.println("Generated OTP for Email: " + otpForEmail);

                                    // Create a new user with the given fields
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("name", name);
                                    userMap.put("phone", phone);
                                    userMap.put("username", username);
                                    userMap.put("affiliation", finalAffiliation);
                                    userMap.put("isApproved", false);
                                    userMap.put("otpForSms", otpForSms);
                                    userMap.put("otpForEmail", otpForEmail);

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Add a new document with a generated ID
                                    db.collection("User_Requests")
                                            .document(user.getUid())
                                            .set(userMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + user.getUid());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error adding document", e);
                                                }
                                            });
                                } else {
                                    // If sign up fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(PermissionActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    };


}




//                                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    // Document was successfully written!
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    // Write failed
////                                                    Log.d("Firestore Error", e.getMessage());
//                                            });
//                                };
//                            };
//                        });
//            }
//        });
//    }
//}




