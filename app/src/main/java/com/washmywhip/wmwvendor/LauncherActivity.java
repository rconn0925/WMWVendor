package com.washmywhip.wmwvendor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Ross on 4/6/2016.
 */
public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("isLoggedIn", false)){
            //Login activity
            Log.d("WASHMYWHIP", "login from past session");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            //Main Activity
            Log.d("WASHMYWHIP","new login session");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
