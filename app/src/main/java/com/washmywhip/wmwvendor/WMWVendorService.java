package com.washmywhip.wmwvendor;

/**
 * Created by Ross on 3/14/2016.
 */
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;
import org.json.JSONArray;
import org.json.JSONObject;

public interface WMWVendorService {


    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/getVendorWithID.php")
    void getVendorWithID (@Field("vendorID") int vendorID, Callback<Object> callback);

    //1 if successful, 0 if sql error, "Email Exists" if email already exists in the table
    @FormUrlEncoded
    @POST("/updateVendorInfo.php")
    void updateVendorInfo (@Field("vendorID") int vendorID,@Field("email") String email, @Field("username") String username, @Field("phone") String phoneNumber, Callback<Object> callback);

    @FormUrlEncoded
    @POST("/requestTemporaryPasswordForVendor.php")
    void requestTemporaryPasswordForVendor (@Field("email") String email, Callback<JSONObject> callback);

    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/updateVendorPassword.php")
    void updateVendorPassword (@Field("email") String email, @Field("password") String password,Callback<JSONObject> callback);

    /*
   [User info] - information about specified user in JSON format. The first JSON element is success:1 to represent a successful login. The second element is isTempPass, which again is a boolean that represents if the user has just reset their password or not.
   Unsuccessful: [integer] - if there is more than 1 user, or no users with the specified username.
    */
    @FormUrlEncoded
    @POST("/requestVendorLogin.php")
    void requestVendorLogin(@Field("email") String email,@Field("password") String password, Callback<JSONObject> callback);

    /*
    This should be called by the Vendor when beginning the wash. If successful, this will return to you a unique transactionID.
    Make sure to save it for later use. The Vendor should not be allowed to create a transaction without taking a “before” picture.
     */

    //last parameter image?
    @FormUrlEncoded
    @POST("/createTransaction.php")
    void createTransaction(@Field("userID") int userID,@Field("vendorID") int vendorID, @Field("carID") int carID,@Field("type") int washType,@Field("cost") int cost, Callback<JSONObject> callback);

    //last parameter image?
    @FormUrlEncoded
    @POST("/createTransaction.php")
    void completeTransaction(@Field("transactionID") int transactionID,@Field("duration") int duration, Callback<JSONObject> callback);

    @FormUrlEncoded
    @POST("/createTransaction.php")
    void rateUser(@Field("transactionID") int transactionID,@Field("rating") int rating,@Field("comments") String comments, Callback<JSONObject> callback);
}