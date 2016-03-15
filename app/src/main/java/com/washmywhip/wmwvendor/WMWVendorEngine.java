package com.washmywhip.wmwvendor;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by Ross on 3/14/2016.
 */
public class WMWVendorEngine {
    private WMWVendorService mService;

    public WMWVendorEngine(){
        OkHttpClient okhttpclient = new OkHttpClient();

        OkClient okclient = new OkClient(okhttpclient);

        RestAdapter.Builder builder =  new RestAdapter.Builder()
                .setEndpoint("http://www.ryanserkes.com/WashMyWhip/")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(okclient);

        RestAdapter adapter = builder.build();
        mService = adapter.create(WMWVendorService.class);
    }

    public void updateVendorInfo(int vendorID, String email, String firstName, String lastName, String phoneNumber, Callback<Object> callback) {
        mService.updateVendorInfo(vendorID, email, firstName, lastName, phoneNumber, callback);
    }
    public void getVendorWithID(int vendorID, Callback<Object> callback) {
        mService.getVendorWithID(vendorID, callback);
    }
    public void requestTemporaryPasswordForVendor(String email, Callback<JSONObject> callback) {
        mService.requestTemporaryPasswordForVendor(email, callback);
    }
    public void updateVendorPassword(String username,String password, Callback<JSONObject> callback) {
        mService.updateVendorPassword(username, password, callback);
    }
    public void requestVendorLogin(String username,String password, Callback<JSONObject> callback) {
        mService.requestVendorLogin(username, password, callback);
    }
    public void createTransaction(int userID,int vendorID,int carID, int washType,int cost,Callback<JSONObject> callback) {
        mService.createTransaction(userID, vendorID, carID, washType, cost, callback);
    }
    public void completeTransaction(int transactionID,int duration,Callback<JSONObject> callback) {
        mService.completeTransaction(transactionID, duration, callback);
    }
    public void rateUser(int transactionID,int rating,String comments,Callback<JSONObject> callback) {
        mService.rateUser(transactionID, rating,comments, callback);
    }

}
