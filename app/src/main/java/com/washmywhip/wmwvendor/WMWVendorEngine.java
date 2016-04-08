package com.washmywhip.wmwvendor;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;

/**
 * Created by Ross on 3/14/2016.
 */
public class WMWVendorEngine {
    private WMWVendorService mService;

    public WMWVendorEngine(){
        OkHttpClient okhttpclient = new OkHttpClient();

        OkClient okclient = new OkClient(okhttpclient);

        RestAdapter.Builder builder =  new RestAdapter.Builder()
                .setEndpoint("http://www.WashMyWhip.us/wmwapp")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(okclient);

        RestAdapter adapter = builder.build();
        mService = adapter.create(WMWVendorService.class);
    }
    public void createVendor(String username, String password, String email,String phoneNumber,Callback<String> callback) {
        mService.createVendor(username, password, email, phoneNumber, callback);
    }
    public void updateVendorInfo(int vendorID, String email, String username, String phoneNumber, Callback<Object> callback) {
        mService.updateVendorInfo(vendorID, email, username, phoneNumber, callback);
    }
    public void getVendorWithID(int vendorID, Callback<Object> callback) {
        mService.getVendorWithID(vendorID, callback);
    }
    public void requestTemporaryPasswordForVendor(String email, Callback<JSONObject> callback) {
        mService.requestTemporaryPasswordForVendor(email, callback);
    }
    public void updateVendorPassword(String email,String password, Callback<JSONObject> callback) {
        mService.updateVendorPassword(email, password, callback);
    }
    public void requestVendorLogin(String email,String password, Callback<JSONObject> callback) {
        mService.requestVendorLogin(email, password, callback);
    }
    public void findCostOfTransactionType(int washType,Callback<String> callback) {
        mService.findCostOfTransactionType(washType, callback);
    }
    public void createTransaction(int userID,int vendorID,int carID, int washType,int cost,String encodedImage,Callback<String> callback) {
        mService.createTransaction(userID, vendorID, carID, washType, cost, encodedImage, callback);
    }
    public void completeTransaction(int transactionID,int duration,String encodedImage,Callback<String> callback) {
        mService.completeTransaction(transactionID, duration, encodedImage, callback);
    }
    public void rateUser(int transactionID,int rating,String comments,Callback<String> callback) {
        mService.rateUser(transactionID, rating, comments, callback);
    }

    public void chargeStripeCustomer(int userID,int costInCents,Callback<String> callback) {
        mService.chargeStripeCustomer(userID, costInCents, callback);
    }
    public void getUserWithID(int userID, Callback<JSONObject> callback) {
        mService.getUserWithID(userID, callback);
    }
    public void getCarWithID(int carID, Callback<Object> callback) {
        mService.getCarWithID(carID, callback);
    }
    public void uploadVendorAvatarImageAndroid(int vendorID, String encodedImage, Callback<Object> callback) {
        mService.uploadVendorAvatarImageAndroid(vendorID, encodedImage, callback);
    }
}
