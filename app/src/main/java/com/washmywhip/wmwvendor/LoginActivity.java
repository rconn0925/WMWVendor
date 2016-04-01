package com.washmywhip.wmwvendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // UI references.
    private RelativeLayout loginLayout;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private TextView forgotPassword;
    private Button logIn;
    private SharedPreferences mSharedPreferences;
    private Context mContext = this;
    private WMWVendorEngine mWMWVendorEngine;
    private Typeface mFont;

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFont= Typeface.createFromAsset(getAssets(), "fonts/Archive.otf");
        mUsernameView = (EditText) findViewById(R.id.usernameField);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);
        forgotPassword.setTypeface(mFont);
        logIn = (Button) findViewById(R.id.loginloginButton);
        logIn.setTypeface(mFont);
        loginLayout = (RelativeLayout) findViewById(R.id.loginLayout);
        logIn.setOnClickListener(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mWMWVendorEngine = new WMWVendorEngine();

        mPasswordView = (EditText) findViewById(R.id.passwordField);
        // mPasswordView.setFocusable(true);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    mPasswordView.requestFocus();
                    showKeyboard(mPasswordView);
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPasswordView.setOnFocusChangeListener(focusChangeListener);
        mUsernameView.setOnFocusChangeListener(focusChangeListener);
    }


    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)

                )

        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        else if (TextUtils.isEmpty(email))

        {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel)

        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else

        {

            if (mPasswordView.hasFocus()) {
                hideKeyboard(mPasswordView);
            } else if (mUsernameView.hasFocus()) {
                hideKeyboard(mUsernameView);
            }

            Log.d("lolz", mUsernameView.getText().toString() + " " + mPasswordView.getText().toString());


            final Intent i = new Intent(this, MainActivity.class);
            final Intent redo = new Intent(this, LoginActivity.class);
            mWMWVendorEngine.requestVendorLogin(mUsernameView.getText().toString(), mPasswordView.getText().toString(), new Callback<JSONObject>() {
                @Override
                public void success(JSONObject jsonObject, Response response) {
                    Log.d("THUGLYFE", "successful VENDOR login");
                    SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();
                    prefsEditor.putString("email", mUsernameView.getText().toString()).apply();
                    prefsEditor.putString("password", mPasswordView.getText().toString()).apply();
                    //Try to get response body
                    String responseString = new String(((TypedByteArray) response.getBody()).getBytes());



                    Log.d("BIG PIMPING", responseString);

                    Map<String, String> userInfo = new HashMap<String, String>();

                    userInfo = parseResponse(responseString);
                    //if response tempPass = 1, prompt user for new password
                    String resetPass = (String) userInfo.get("isTempPass");
                    //String resetPass = "1";
                    if (resetPass.equals("1")) {
                        //prompt reset
                        Log.d("RESETPASS", "YO DAWG! RESET YO PASSWORD!");
                        LayoutInflater li = LayoutInflater.from(mContext);
                        View promptsView = li.inflate(R.layout.reset_password_prompt_layout, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                        alertDialogBuilder.setView(promptsView);
                        final EditText userInput = (EditText) promptsView
                                .findViewById(R.id.editTextDialogUserInput);

                        // set dialog message
                        alertDialogBuilder
                                .setCancelable(false)
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // get user input and send new password to server

                                                //result.setText(userInput.getText());
                                                mWMWVendorEngine.updateVendorPassword(mUsernameView.getText().toString(), userInput.getText().toString(), new Callback<JSONObject>() {
                                                    @Override
                                                    public void success(JSONObject jsonObject, Response response) {
                                                        String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                                                        if (responseString.contains("0")) {
                                                            Log.d("resetPassResult", "failz?");
                                                        } else if (responseString.contains("1")) {
                                                            Log.d("resetPassResult", "success");
                                                        }
                                                    }

                                                    @Override
                                                    public void failure(RetrofitError error) {

                                                    }
                                                });
                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();

                    } else {

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        for (String s : userInfo.keySet()) {
                            Log.d("thisismytag", s + " , " + userInfo.get(s));
                            editor.putString(s, userInfo.get(s));
                        }
                        if (mPasswordView.hasFocus()) {
                            hideKeyboard(mPasswordView);
                        }
                        if (mUsernameView.hasFocus()) {
                            hideKeyboard(mUsernameView);
                        }
                        editor.commit();
                        startActivity(i);
                        finish();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("HERPDERP", "RETROFIT ERROR: " + error);
                    //DONT DO THIS!! THIS IS BAD. Just a toast and reset the username and password field.
                    //ERROR message: bad connection? bad username? bad password?
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Error logging in");
                    builder.setMessage("Please try again!");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                    mPasswordView.setText("");
                    mUsernameView.setText("");

                }
            });
        }
    }

    public HashMap<String,String> parseResponse(String s) {
        HashMap userData = new HashMap();
        s = s.substring(1, s.length()-1);
        s = s.replace(" ", "").replace("\t", "").replace(",","").replace("\"", "");
        String[] dataItem = s.split("\n");
        for(int i = 1; i < dataItem.length;i++){
            if(dataItem[i].endsWith(":")){
                dataItem[i] = dataItem[i]+" ";
            }
            String[] info  = dataItem[i].split(":");
            String key = info[0];
            String value = info[1];
            userData.put(key, value);
        }
        return userData;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == logIn.getId()) {
            attemptLogin();


        } else if(v.getId() == forgotPassword.getId()){
            Intent forgotPass = new Intent(this,ForgotPasswordActivity.class);
            startActivity(forgotPass);
            finish();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
