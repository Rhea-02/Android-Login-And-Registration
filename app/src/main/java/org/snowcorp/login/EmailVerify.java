package org.snowcorp.login;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.helper.DatabaseHandler;
import org.snowcorp.login.helper.Functions;
import org.snowcorp.login.helper.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class EmailVerify extends AppCompatActivity {
    private static final String TAG = EmailVerify.class.getSimpleName();
    private static final String FORMAT = "%02d:%02d";
    String verificationId;
    FirebaseAuth mAuth;
    private TextInputLayout textVerifyCode;
    private MaterialButton btnVerify, btnResend;
    private TextView otpCountDown;
    private SessionManager session;
    private DatabaseHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verify);
        db = new DatabaseHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        textVerifyCode = findViewById(R.id.verify_code);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResendCode);
        otpCountDown = findViewById(R.id.otpCountDown);
        Log.e(TAG, "onCreate----------------Functions.EMAIL: " + Functions.EMAIL);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        init();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 100);
        } else {

        }

    }

    private void init() {
        btnVerify.setOnClickListener(v -> {
            String enteredOtp = Objects.requireNonNull(textVerifyCode.getEditText()).getText().toString();
            if (!enteredOtp.isEmpty()) {
                verifyCode(Functions.EMAIL, enteredOtp);
                textVerifyCode.setErrorEnabled(false);
            } else {
                textVerifyCode.setError("Please enter verification code");
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendCode(Functions.EMAIL);
            }
        });

        btnResend.setEnabled(false);
        countDown();
    }

    private void countDown() {
        new CountDownTimer(70000, 1000) { // adjust the milli seconds here
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            public void onTick(long millisUntilFinished) {
                otpCountDown.setVisibility(View.VISIBLE);
                otpCountDown.setText("" + String.format(FORMAT, TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                otpCountDown.setVisibility(View.GONE);
                btnResend.setEnabled(true);
            }
        }.start();
    }

    private void verifyCode(final String email, final String otp) {
        String tag_string_req = "req_verify_code";
        Log.e(TAG, "verifyCode-----email: " + email);
        showDialog("Checking in ...");

        StringRequest strReq = new StringRequest(Request.Method.POST, Functions.OTP_VERIFY_URL, response -> {
            Log.e(TAG, "Verification Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                hideDialog();

                if (!error) {
                    hideDialog();
                    session.setLogin(true);
                    Intent upanel = new Intent(EmailVerify.this, DrawerNavigationActivity.class);
                    startActivity(upanel);
                    finish();
                } else {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), "Invalid Verification Code", Toast.LENGTH_LONG).show();
                    textVerifyCode.setError("Invalid Verification Code");
                }
            } catch (JSONException e) {
                hideDialog();
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }, error -> {
            Log.e(TAG, "Verify Code Error: " + error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show(); // Show a toast with the error message
            hideDialog();
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "verify_code");
                params.put("email", Functions.EMAIL);
                params.put("otp", otp);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params; // Return the headers
            }
        };

        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
        SQLiteDatabase db = this.db.getReadableDatabase();
        String selection = "email = ?";
        String[] selectionArgs = {email};  // Use the email parameter directly here
        Cursor cursor = db.query("User_Requests", new String[]{"sendotp"}, selection, selectionArgs, null, null, null);

        /*if (cursor != null && cursor.moveToFirst()) {
            int savedOtp = cursor.getInt(cursor.getColumnIndexOrThrow("sendotp"));
            Log.e(TAG, "verifyCode--------------savedOtp: " + savedOtp );
            cursor.close();

            if (savedOtp == Integer.parseInt(otp)) {
//            hideDialog();
                Toast.makeText(this, "Verification successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EmailVerify.this, DrawerNavigation.class));
                finish();
            } else {
//            hideDialog();
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show();
            }
        } else {
//        hideDialog();
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
        }*/
    }

    private void resendCode(final String email) {
        sendVerificationCode(Functions.PHONE);
        Log.e(TAG, "onCreate------------dfdfdfdf----Functions.EMAIL: " + Functions.EMAIL);
    }

    private void showDialog(String title) {
        Functions.showProgressDialog(EmailVerify.this, title);
    }

    private void hideDialog() {
        Functions.hideProgressDialog(EmailVerify.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        countDown();
    }

    private void sendVerificationCode(String phoneNumber) {
        mAuth = FirebaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 100);
        } else {
            String tag_string_req = "req_resend_code";
            showDialog("Resending code ...");
            StringRequest strReq = new StringRequest(Request.Method.POST, Functions.OTP_VERIFY_URL, response -> {
                Log.e(TAG, "Resend Code Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    hideDialog();
                    if (!error) {
                        hideDialog();
                        String otp = jObj.getString("otp");
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumber, null, "You may be asked to enter this reconfirmation code: " + otp + "Regards,Android Learning.", null, null);
                        Toast.makeText(getApplicationContext(), "Code successfully sent to your email!", Toast.LENGTH_LONG).show();
                        btnResend.setEnabled(false);
                        countDown();
                    } else {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "Code sending failed!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    hideDialog();
                }

            }, error -> {
                hideDialog();
                Log.e(TAG, "Resend Code Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "resend_code");
                    params.put("email", Functions.EMAIL);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");

                    return params;
                }
            };
            MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    private void updateUserRequestWithOtp(String email, int newOtp) {
        SQLiteDatabase db = this.db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sendotp", newOtp);
        db.update("User_Requests", values, "email = ?", new String[]{email});
    }

    public static class User {
        private String email;
        private String password;

        // Constructor
        public User(String email, String password) {
            this.email = email;
            this.password = password;
        }

        // Getter for email
        public String getEmail() {
            return this.email;
        }

        // Getter for password
        public String getPassword() {
            return this.password;
        }
    }

    public class EmailSenderTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "EmailSenderTask";
        private String recipientEmail;
        private String subject;
        private String body;

        public EmailSenderTask(String recipientEmail, String subject, String body) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.body = body;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final String username = "mahardhidevs@gmail.com";
            final String password = "kvxskajxqftdhbaz";

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

