package com.washmywhip.wmwvendor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
    private String vendorID;
    private String deviceID;
    SharedPreferences mSharedPreferences;
    private String transactionID;
    private boolean isListening;
    private Context mContext;
    //transaction ID???

    public ConnectionManager(Context context) {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        // SocketIO socket =new SocketIO();
        deviceID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        vendorID = mSharedPreferences.getString("VendorID", "-1");
        Log.d("server connection", "response?: " + deviceID);
        mContext = context;

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
            mSocket.on("addVendorConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "addVendorConfirmation");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "addVendorConfirmation staus: " + args[0].toString());
                        Intent intent = new Intent();
                        intent.putExtra("state", args[0].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                }
            }).on("startListeningConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "startListeningConfirmation");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "startListeningConfirmation staus: " + args[0].toString());
                    }
                }
            }).on("stopListeningConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "stopListeningConfirmation");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "stopListeningConfirmation staus: " + args[0].toString());
                    }
                }
            }).on("requestCanceled", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "requestCanceled");
                    Intent intent = new Intent();
                    intent.putExtra("requestCancel","true");
                    intent.setAction("com.android.activity.SEND_DATA");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }).on("requestExpired", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "requestExpired");
                    Intent intent = new Intent();
                    intent.putExtra("requestExpired","true");
                    intent.setAction("com.android.activity.SEND_DATA");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }).on("washRequested", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "washRequested");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "washRequested staus: userID=" + args[0].toString()+", location="+ args[1].toString()+", carID="+ args[2].toString()+", washType="+ args[3].toString());
                        //init requesting
                        Intent intent = new Intent();
                        intent.putExtra("userInfo", args[0].toString()+", "+ args[1].toString()+", "+ args[2].toString()+", "+ args[3].toString());
                        intent.setAction("com.android.activity.SEND_DATA");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                }
            }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onConnect");
                    if (args != null) {
                        Log.d("server connection", "onConnect: " + args.length);
                        addVendor();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String response = (String)args[0];
                    Log.d("server connection", "onDisconnect: "+ response);
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    Log.d("server connection", "onConnectionError: ");
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // String response = (String) args[0];
                    Log.d("server connection", "onError: ");
                }
            });
            Log.d("server connection", "attempting to connect...");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void addVendor(){
        Log.d("server connection", "addVEndor server: "+ mSocket.connected());
        //empty string place holder for device
        String[] data = {vendorID,""};
        if(mSocket.connected()){
            mSocket.emit("addVendor", data);
        }
    }
    //how is data sent??
    public void startListening(LatLng mLocation){
        Log.d("server connection", "startListening server: "+ mSocket.connected());
        String location = mLocation.latitude + ", "+mLocation.longitude;
        if(mSocket.connected()){
            mSocket.emit("startListening", location);
            isListening = true;
        }
    }
    public void stopListening(){
        Log.d("server connection", "stopListening server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("stopListening", "");
            isListening = false;
        }
    }
    public void acceptRequest(LatLng mLocation){
        Log.d("server connection", "acceptRequest server: "+ mSocket.connected());
        String location = mLocation.latitude + ", "+mLocation.longitude;
        String[] data = {vendorID,location};
        if(mSocket.connected()){
            mSocket.emit("acceptRequest", data);
        }
    }
    public void updateETA(LatLng mLocation){
        Log.d("server connection", "updateETA server: "+ mSocket.connected());
        String location = mLocation.latitude + ", "+mLocation.longitude;
        Log.d("server connection", "updateETA coordinates: "+ location);
        if(mSocket.connected()){
            mSocket.emit("updateETA", location);
        }
    }
    public void vendorHasArrived(){
        Log.d("server connection", "vendorHasArrived server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("vendorHasArrived", "");
        }
    }
    public void vendorHasInitiatedWash(String transactionID){
        Log.d("server connection", "vendorHasInitiatedWash server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("vendorHasInitiatedWash", transactionID);
        }
    }
    public void vendorHasCompletedWash(String transactionID){
        Log.d("server connection", "vendorHasCompletedWash server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("vendorHasCompletedWash", transactionID);
        }
    }
    public void vendorHasFinalized(){
        Log.d("server connection", "vendorHadFinalized server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("vendorHadFinalized", "");
        }
    }

    public void disconnect(){
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
        mSocket.off(Socket.EVENT_ERROR);
        mSocket.off(Socket.EVENT_CONNECT_ERROR);
        mSocket.off("addVendorConfirmation");
        mSocket.off("requestWash");

    }

    public boolean isConnected(){
        return mSocket.connected();
    }
    public boolean isListening() {return isListening;}
}
