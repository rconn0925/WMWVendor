package com.washmywhip.wmwvendor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
    private int userID;
    private String deviceID;
    SharedPreferences mSharedPreferences;
    private String transactionID;
    //transaction ID???

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
            mSocket.on("addVendorConfirmation", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "onAddUser");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "onAddUser staus: " + args[0].toString());
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
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "requestCanceled staus: " + args[0].toString());
                    }
                }
            }).on("washRequested", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("server connection", "washRequested");
                    if (args != null && args.length > 0) {
                        Log.d("server connection", "washRequested staus: " + args[0].toString());
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void addVendor(){
        Log.d("server connection", "addVEndor server: "+ mSocket.connected());
        String vendorIDstring =Integer.toString(userID);
        //empty string place holder for device
        String[] data = {vendorIDstring,""};
        if(mSocket.connected()){
            mSocket.emit("addVendor", data);
        }
    }
    //how is data sent??
    public void startListening(LatLng mLocation){
        Log.d("server connection", "startListening server: "+ mSocket.connected());
        double[] data = {mLocation.latitude,mLocation.longitude};
        if(mSocket.connected()){
            mSocket.emit("startListening", data);
        }
    }
    public void stopListening(){
        Log.d("server connection", "stopListening server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("startListening", "");
        }
    }
    public void acceptRequest(){
        Log.d("server connection", "acceptRequest server: "+ mSocket.connected());
        if(mSocket.connected()){
            mSocket.emit("acceptRequest", "");
        }
    }
    public void updateETA(LatLng mLocation){
        Log.d("server connection", "updateETA server: "+ mSocket.connected());
        double[] data = {mLocation.latitude,mLocation.longitude};
        if(mSocket.connected()){
            mSocket.emit("updateETA", data);
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
    public void vendorHadFinalized(){
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
}
