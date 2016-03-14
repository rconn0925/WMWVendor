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

public interface WMWVendorService {

    //1 if successful, 0 if sql error, "Email Exists" if email already exists in the table
    @FormUrlEncoded
    @POST("/updateVendorInfo.php")
    void updateVendorInfo (@Field("vendorID") int userId,@Field("email") String email, @Field("firstName") String firstName,@Field("lastName")  String lastName, @Field("phone") String phoneNumber, Callback<Object> callback);

}
