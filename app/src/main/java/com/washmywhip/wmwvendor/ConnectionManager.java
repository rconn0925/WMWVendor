package com.washmywhip.wmwvendor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by Ross on 3/18/2016.
 */
public class ConnectionManager {

    private String mAddress;
    private Socket mSocket;
    private int userID;
    private String deviceID;
    SharedPreferences mSharedPreferences;

    public ConnectionManager(Context context) {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        // SocketIO socket =new SocketIO();
        deviceID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        userID = Integer.parseInt(mSharedPreferences.getString("UserID", "-1"));
        Log.d("server connection", "response?: " + deviceID);

        try {
            // mAddress = "http://192.168.0.18:3000";
            mAddress = "http://54.191.214.16:3000";
            //  IO.Options opts = new IO.Options();
            //  opts.forceNew = true;
            //  opts.reconnection = false;
            if (mSocket != null && mSocket.connected()) {
                mSocket.close();
            }
            mSocket = IO.socket(mAddress);

            //change the on listeners
            mSocket.on("addUserConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onAddUser");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "onAddUser staus: " + args[0].toString());
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
        mSocket.off(Socket.EVENT_ERROR);
        mSocket.off(Socket.EVENT_CONNECT_ERROR);
        mSocket.off("addUser");
        mSocket.off("requestWash");

    }

    public boolean isConnected(){
        return mSocket.connected();
    }
}
