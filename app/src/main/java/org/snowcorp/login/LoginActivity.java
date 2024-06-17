package org.snowcorp.login;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.helper.DatabaseHandler;
import org.snowcorp.login.helper.Functions;
import org.snowcorp.login.helper.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private MaterialButton btnLogin, btnLinkToRegister, btnForgotPass;
    private TextInputLayout inputEmail, inputPassword;
    private DatabaseHandler dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        dbHelper = new DatabaseHandler(this);
        inputEmail = findViewById(R.id.edit_email);
        inputPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.button_login);
        btnLinkToRegister = findViewById(R.id.button_register);
        btnForgotPass = findViewById(R.id.button_reset);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        init();
    }

    private SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }

    private void init() {
        btnLogin.setOnClickListener(view -> {
            String email = Objects.requireNonNull(inputEmail.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(inputPassword.getEditText()).getText().toString().trim();
            Log.e(TAG, "email: " + email);
            Log.e(TAG, "password: " + password);
            if (!email.isEmpty() && !password.isEmpty()) {
                if (Functions.isValidEmailAddress(email)) {
                    loginProcess(email, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Email is not valid!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enter the credentials!", Toast.LENGTH_LONG).show();
                Functions.hideSoftKeyboard(LoginActivity.this, view);
            }
        });

        btnLinkToRegister.setOnClickListener(view -> {
            Intent i = new Intent(LoginActivity.this, PermissionActivity.class);
            startActivity(i);
        });

        btnForgotPass.setOnClickListener(v -> forgotPasswordDialog());
    }

    private void forgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.reset_password, null);
        TextInputLayout mEditEmail = dialogView.findViewById(R.id.edit_email);

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).setTitle("Forgot Password").setCancelable(false).setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "onClick------------------: " + mEditEmail.getEditText().getText().toString());
                //resetPassword(mEditEmail.getEditText().getText().toString());
                String tag_string_req = "req_reset_pass";
                showDialog("Please wait...");

                StringRequest strReq = new StringRequest(Request.Method.POST, Functions.RESET_PASS_URL, response -> {
                    Log.e(TAG, "Login Response----------: " + response);
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        Log.e(TAG, "Login Response----------: " + response);

                        boolean error = jObj.getBoolean("error");
                        Log.e(TAG, "error----------: " + error);

                        if (!error) {
//                            JSONObject json_user = jObj.getJSONObject("users");
//                            Functions logout = new Functions(this);
                            String errorMsg = jObj.getString("message");
                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            String errorMsg = jObj.getString("message");
                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    hideDialog();
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("tag", "forgot_pass");
                        params.put("email", mEditEmail.getEditText().getText().toString());
                        return params;
                    }
                };

                addRequestToQueue(strReq, tag_string_req);

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "onClick------------------: " + mEditEmail.getEditText().getText().toString());
            }
        }).create();

        alertDialog.show();
    }

    private void showResetPasswordDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String newPassword = input.getText().toString();
            if (newPassword.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                //updatePassword(email, newPassword);
                // resetPassword(email);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loginProcess(final String email, final String password) {

        if (email.equals("nidhivranjith@gmail.com") && password.equals("nidhivranjith")) {
            Log.e(TAG, "loginProcess: " + "admin loginn....");
            SharedPreferences sharedPref = getSharedPreferences("login_response", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("login_data", email);
            editor.apply();
            startActivity(new Intent(LoginActivity.this,AdminPanelActivity.class));
            finish();
        } else {
            String tag_string_req = "req_login";
            showDialog("Logging in ...");
            StringRequest strReq = new StringRequest(Request.Method.POST, Functions.LOGIN_URL, response -> {
                Log.e(TAG, "Login Response----------: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    Log.e(TAG, "Login Response----------: " + response);
                    boolean error = jObj.getBoolean("error");
                    Log.e(TAG, "error----------: " + error);

                    if (!error) {
                        JSONObject json_user = jObj.getJSONObject("user");
                        SharedPreferences sharedPref = getSharedPreferences("login_response", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("login_data", email);
                        editor.apply();
                        if (json_user.optString("verified", "0").equals("1")) {
                            dbHelper.createUserWithEmailAndPassword(json_user.getString("uid"),
                                    json_user.getString("name"),
                                    json_user.getString("name"),
                                    json_user.getString("email"),
                                    json_user.optString("password", ""),
                                    json_user.optString("affiliation", ""),
                                    json_user.optString("isApproved", ""),
                                    json_user.optString("sendemail", ""),
                                    json_user.optInt("sendotp")
                            );
                            Log.e(TAG, "User logged in successfully with email: " + json_user.getString("email"));
                            String errorMsg = jObj.getString("message");
                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                            session.setLogin(true);
                            if (session.isLoggedIn()) {
                                startActivity(new Intent(LoginActivity.this, DrawerNavigationActivity.class));
                                finish();
                            }
                        } else {
                            Bundle b = new Bundle();
                            b.putString("email", json_user.getString("email"));
                            Functions.EMAIL = inputEmail.getEditText().getText().toString();
                            Intent upanel = new Intent(LoginActivity.this, EmailVerify.class);
                            upanel.putExtras(b);
                            Functions.EMAIL = inputEmail.getEditText().getText().toString();
                            upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(upanel);
                            finish();
                        }
                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, error -> {
                Log.e(TAG, "Login Error: " + error.getMessage());
                hideDialog();
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }
            };

            addRequestToQueue(strReq, tag_string_req);
        }
    }


    public void addRequestToQueue(StringRequest strReq, String tag_string_req) {
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void showDialog(String title) {
        Functions.showProgressDialog(LoginActivity.this, title);
    }

    private void hideDialog() {
        Functions.hideProgressDialog(LoginActivity.this);
    }


    private void resetPassword(final String email) {
        String tag_string_req = "req_reset_pass";
        showDialog("Please wait...");

        StringRequest strReq = new StringRequest(Request.Method.POST, Functions.RESET_PASS_URL, response -> {
            Log.e(TAG, "Login Response----------: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                Log.e(TAG, "Login Response----------: " + response);

                boolean error = jObj.getBoolean("error");
                Log.e(TAG, "error----------: " + error);

                if (!error) {
//                            JSONObject json_user = jObj.getJSONObject("users");
                    Functions logout = new Functions(this);
                    String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

//                    startActivity(new Intent(LoginActivity.this, LoginActivity.class));

                } else {
                    String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Log.e(TAG, "Login Error: " + error.getMessage());
            hideDialog();
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "forgot_pass");
                params.put("email", email);
                return params;
            }
        };

        addRequestToQueue(strReq, tag_string_req);

       /* StringRequest strReq = new StringRequest(Request.Method.POST, Functions.RESET_PASS_URL, response -> {
            Log.e(TAG, "Reset Password Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                Log.e(TAG, "resetPassword----------: " + jObj.getString("message") );
                Toast.makeText(getApplicationContext(), jObj.getString("message"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Log.e(TAG, "Reset Password Error: " + error.getMessage());
//            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "forgot_pass");
                params.put("email", email);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);*/
    }

}


