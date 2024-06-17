package org.snowcorp.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.helper.DatabaseHandler;
import org.snowcorp.login.helper.Functions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = PermissionActivity.class.getSimpleName();
    private static final int REQUEST_CODE_GOOGLE_PLAY_SERVICES = 1000;

    private SQLiteDatabase db;
    private String username;
    private String countryCode;
    private String email;
    private String password;
    private String affiliation;
    private String phone;
    private int otpForSms;

    private EditText editTextUsername;
    private EditText editTextName;
    private EditText editTextPhone;
    private Spinner spinnerCountryCode;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private AutoCompleteTextView editTextAffiliation;
    private EditText editTextOtp;
    private Button buttonSubmitOtp;
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_BACKOFF_DELAY_MS = 1000; // 1 second
    private int currentRetry = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        DatabaseHandler dbHelper = new DatabaseHandler(getBaseContext());
        db = dbHelper.getWritableDatabase();

        initializeUI();
//        checkSmsPermission();

        // Check Google Play Services version
        checkGooglePlayServices(this);
    }

    private void checkGooglePlayServices(Context context) {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (status != ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, status, REQUEST_CODE_GOOGLE_PLAY_SERVICES);
            if (dialog != null) {
                dialog.show();
            } else {
                // If the dialog cannot be shown, prompt user to update directly
                GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
            }
        }
    }



    private void initializeUI() {
        setupLoginButton();
        setupCountryCodeSpinner();
        setupTextInputFields();
        setupAffiliationDropdown();
        setupRegisterButton();
//        setupOtpInput();
    }

    private void setupLoginButton() {
        Button buttonGoToLogin = findViewById(R.id.buttonGoToLogin);
        buttonGoToLogin.setOnClickListener(v -> startActivity(new Intent(PermissionActivity.this, LoginActivity.class)));
    }

    private void setupCountryCodeSpinner() {
        spinnerCountryCode = findViewById(R.id.country_code_spinner);
        ArrayAdapter<CharSequence> adapterCountryCode = ArrayAdapter.createFromResource(this, R.array.country_codes_array, android.R.layout.simple_spinner_item);
        adapterCountryCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCountryCode);
    }

    private void setupTextInputFields() {
        TextInputLayout textInputLayoutUsername = findViewById(R.id.editTextUsername);
        TextInputLayout textInputLayoutPhone = findViewById(R.id.editTextPhone);
        TextInputLayout textInputLayoutEmail = findViewById(R.id.editTextEmail);
        TextInputLayout textInputLayoutName = findViewById(R.id.editTextName);
        TextInputLayout textInputLayoutPassword = findViewById(R.id.editTextPassword);

        editTextUsername = textInputLayoutUsername.getEditText();
        editTextPhone = textInputLayoutPhone.getEditText();
        editTextEmail = textInputLayoutEmail.getEditText();
        editTextName = textInputLayoutName.getEditText();
        editTextPassword = textInputLayoutPassword.getEditText();
    }

    private void setupAffiliationDropdown() {
        editTextAffiliation = findViewById(R.id.autoCompleteTextView);
        String[] affiliations = new String[]{"Academic Officer", "Scientific Officer", "Technical Assistant", "Auxillary Team", "Visiting Fellows", "Students", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, affiliations);
        editTextAffiliation.setAdapter(adapter);
        editTextAffiliation.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            findViewById(R.id.editTextOtherAffiliation).setVisibility(selectedItem.equals("Other") ? View.VISIBLE : View.GONE);
        });
    }

    private void setupRegisterButton() {
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(v -> {
            username = editTextUsername.getText().toString().trim();
            countryCode = spinnerCountryCode.getSelectedItem().toString();
            phone = editTextPhone.getText().toString().trim();
            email = editTextEmail.getText().toString().trim();
            String name = editTextName.getText().toString().trim();
            password = editTextPassword.getText().toString().trim();
            affiliation = editTextAffiliation.getText().toString().trim();

            if (affiliation.equals("Other")) {
                TextInputLayout textInputOtherAffiliation = findViewById(R.id.editTextOtherAffiliation);
                affiliation = textInputOtherAffiliation.getEditText().getText().toString();
            }

            if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || name.isEmpty() || password.isEmpty() || affiliation.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, phone, email, username, password, affiliation);
            // Show OTP dialog box
//            showOtpDialog();
        });
    }
//    private void showOtpDialog() {
//        Dialog otpDialog = new Dialog(this);
//        otpDialog.setContentView(R.layout.otp_dialog_box);
//        EditText editTextOtp = otpDialog.findViewById(R.id.editTextOtp);
//        Button buttonSubmitOtp = otpDialog.findViewById(R.id.buttonSubmitOtp);
//        Button buttonResendOtp = otpDialog.findViewById(R.id.buttonResendOtp);
//
//        // Set click listeners for submit and resend buttons
//        buttonSubmitOtp.setOnClickListener(v -> {
//            String enteredOtp = editTextOtp.getText().toString().trim();
//            if (String.valueOf(otpForSms).equals(enteredOtp)) {
//                Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show();
//                registerUserInDatabase();
//                otpDialog.dismiss();
//            } else {
//                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//            }
//        });

//        buttonResendOtp.setOnClickListener(v -> {
//            // Resend OTP logic here
//
//            // For example: resendOtp();
//        });
//        otpDialog.show();
//    }

//    private void setupOtpInput() {
//        editTextOtp = findViewById(R.id.editTextOtp); buttonSubmitOtp = findViewById(R.id.buttonSubmitOtp);
//        buttonSubmitOtp.setOnClickListener(v -> {
//            String enteredOtp = editTextOtp.getText().toString().trim();
//            if (String.valueOf(otpForSms).equals(enteredOtp)) {
//                Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show();
//                registerUserInDatabase();
//            } else {
//                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void checkSmsPermission() {
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 100);
//        }
//    }

    private void registerUser(final String name, final String phone, final String email, final String username, final String password, final String affiliation) {
        String tag_string_req = "req_register";
        showDialog("Registering ...");

        StringRequest strReq = new StringRequest(Request.Method.POST, Functions.REGISTER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (error) {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    } else {
                        // Registration was successful
                        String successMsg = "User Details Received";
                        Toast.makeText(getApplicationContext(), successMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMsg = "Unknown error occurred."; // Default error message

                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        // Try to parse error response
                        String errorString = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        JSONObject errorObj = new JSONObject(errorString);

                        // Check if "message" key exists in the error response
                        if (errorObj.has("message")) {
                            errorMsg = errorObj.getString("message"); // Get error message from response
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Log the error
                Log.e(TAG, "Register Error: " + errorMsg);

                // Show error message to the user
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

                // Hide any dialogs if shown
                hideDialog();
            }


        }) {
            // getParams implementation


            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("affiliation", affiliation);
                params.put("phone", phone);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

//    private void sendVerificationCode(String phoneNumber) {
//        try {
//            Random rand = new Random();
//        otpForSms = rand.nextInt(900000) + 100000;
//
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 100);
//        } else {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phoneNumber, null, "You may be asked to enter this confirmation code: " + otpForSms + " Regards, Android Learning.", null, null);
//            sendOtpToServer();
//        }
//    }catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error sending verification code", Toast.LENGTH_SHORT).show();
//        }
//    }

//    private void sendOtpToServer() {
//            try {
//                String tag_string_req = "req_otp";
//
//                StringRequest strReq = new StringRequest(Request.Method.POST, Functions.OTP_URL, response -> {
//                    try {
//                        JSONObject jObj = new JSONObject(response);
//                        boolean success = jObj.getBoolean("success");
//                        if (success) {
//                            Toast.makeText(this, "Verification code sent via SMS and Email.", Toast.LENGTH_SHORT).show();
//                            String emailsend = editTextEmail.getText().toString();
//                            String emailsubject = "Android Learning Email Verification";
//                            String emailbody = "Hello " + editTextName.getText().toString() + " Verify that you own " + emailsend + ". You may be asked to enter this confirmation code: " + otpForSms + " Regards, Android Learning.";
//                            new EmailSenderTask(emailsend, emailsubject, emailbody).execute();
//                            createUserWithEmailAndPassword(email, password, editTextName.getText().toString(), phone, username, affiliation, emailsend, otpForSms);
//                        } else {
//                            Toast.makeText(this, "Failed to send verification code.", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                    hideDialog();
//                }, error -> {
//                    Log.e(TAG, "OTP Error: " + error.getMessage());
//                    hideDialog();
//                    Toast.makeText(this, "Network error. Retrying...", Toast.LENGTH_SHORT).show();
//                    retryRequest(); // Call retryRequest() method in case of error
//                }) {
//                    @Override
//                    protected Map<String, String> getParams() {
//                        Map<String, String> params = new HashMap<>();
//                        params.put("email", editTextEmail.getText().toString());
//                        params.put("otp", String.valueOf(otpForSms));
//                        return params;
//                    }
//                };
//
//                // Set the retry policy for the request
//                strReq.setRetryPolicy(new DefaultRetryPolicy(INITIAL_BACKOFF_DELAY_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//                // Add the request to the request queue
//                addRequestToQueue(strReq, tag_string_req);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Error sending OTP to server", Toast.LENGTH_SHORT).show();
//                retryRequest(); // Call retryRequest() method in case of exception
//            }
//        }
//    private void retryRequest() {
//        if (currentRetry < MAX_RETRIES) {
//            currentRetry++;
//            int backoffDelay = (int) (INITIAL_BACKOFF_DELAY_MS * Math.pow(2, currentRetry - 1));
//            new Handler().postDelayed(this::sendOtpToServer, backoffDelay);
//        } else {
//            Toast.makeText(this, "Maximum retry attempts reached. Please try again later.", Toast.LENGTH_SHORT).show();
//        }
//    }




    private void createUserWithEmailAndPassword(String email, String password, String name, String phone, String username, String affiliation, String sendemail, int sendotp) {
        ContentValues userValues = new ContentValues();
        userValues.put(DatabaseHandler.KEY_EMAIL, email);
        userValues.put(DatabaseHandler.KEY_PASSWORD, password);
        userValues.put(DatabaseHandler.KEY_NAME, name);
        userValues.put(DatabaseHandler.KEY_PHONE, phone);
        userValues.put(DatabaseHandler.KEY_USERNAME, username);
        userValues.put("affiliation", affiliation);
        userValues.put(DatabaseHandler.KEY_IS_APPROVED, false);
        userValues.put(DatabaseHandler.KEY_SEND_EMAIL, sendemail);
        userValues.put(DatabaseHandler.KEY_SEND_OTP, sendotp);

        db.insert("user_requests", null, userValues);
        db.close();
    }

    private void registerUserInDatabase() {
        // Implement database registration logic if needed
    }

    private void showDialog(String title) {
        Functions.showProgressDialog(PermissionActivity.this, title);
    }
    //
    private void hideDialog() {
        Functions.hideProgressDialog(PermissionActivity.this);
    }
    //
    public void addRequestToQueue(StringRequest strReq, String tag_string_req) {
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public static class OtpDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because it's going in the dialog layout
            builder.setView(inflater.inflate(R.layout.otp_dialog_box, null))
                    // Add action buttons
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Get the EditText from the dialog layout
                            EditText editTextOtp = getDialog().findViewById(R.id.editTextOtp);
                            String enteredOtp = editTextOtp.getText().toString().trim();
                            // Verify the OTP here
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            OtpDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    private static class EmailSenderTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "EmailSenderTask";
        private final String recipientEmail;
        private final String subject;
        private final String body;

        public EmailSenderTask(String recipientEmail, String subject, String body) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.body = body;
        }



        @Override
        protected Void doInBackground(Void... voids) {
            final String username = "nidhivranjith@gmail.com";
            final String password = "nidhivranjith";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);
                Log.e(TAG, "Email sent successfully");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     CODE TO SEND SMS FOR PHONE
//      Generate OTPs for SMS
//        Random rand = new Random();
//        int otpForSms = rand.nextInt(900000) + 100000;
//
//        // Insert the user's information into the SQLite database
//        if (db != null) {
//            long newRowId = db.insert("User_Requests", null, userValues);
//            if (newRowId != -1) {
//                Toast.makeText(this, "New user added with ID: " + newRowId, Toast.LENGTH_SHORT).show();
////                Log.d(TAG, "New user added with ID: " + newRowId);
//
//                // Check if the SEND_SMS permission has been granted
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    // If not, request the permission
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
//                } else {
//                    // Permission has already been granted, you can send the SMS
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phone, null, "Your OTP is: " + otpForSms, null, null);
//                    showOtpDialog(otpForSms); // Show the OTP dialog after sending the SMS
//                }
//            } else {
//                Log.w(TAG, "Error adding user");
//            }
//        } else {
//            // Log an error or throw an exception
//            Log.e(TAG, "Database is null. Cannot insert user.");
//        }
//    }
//
// Method to show the OTP dialog
//    private void showOtpDialog(final int otpForSms) {
//
//        // Create a dialog builder
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        // Inflate the dialog view
//        View otpDialogView = getLayoutInflater().inflate(R.layout.otp_dialog_box, null);
//
//        // Get the views from the dialog
//        EditText otpField = otpDialogView.findViewById(R.id.otpField);
//        TextView timerTextView = otpDialogView.findViewById(R.id.timer);
//        Button resendOtpButton = otpDialogView.findViewById(R.id.resendOtpButton);
//        Button submitOtpButton = otpDialogView.findViewById(R.id.submitOtpButton);
//
//        // Initially hide the resend OTP button
//        resendOtpButton.setVisibility(View.INVISIBLE);
//
//        // Create timers for the OTP
//        CountDownTimer fiveMinuteTimer = new CountDownTimer(300000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                // Update the timer text view
//                timerTextView.setText("Time remaining: " + millisUntilFinished / 1000);
//            }
//
//            @Override
//            public void onFinish() {
//                resendOtpButton.setVisibility(View.VISIBLE);
//            }
//
//        }.start();
//
//        resendOtpButton.setOnClickListener(v -> {
//            fiveMinuteTimer.start();
//            resendOtpButton.setVisibility(View.INVISIBLE);
//        });
//        submitOtpButton.setOnClickListener(v -> {
//            String enteredCode = otpField.getText().toString();
//            if (Integer.parseInt(enteredCode) == otpForSms) {
//                Toast.makeText(getApplicationContext(), "OTP verified successfully!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "Invalid OTP", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.setView(otpDialogView);
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//    }

