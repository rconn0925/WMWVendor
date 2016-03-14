package com.washmywhip.wmwvendor;

import com.squareup.okhttp.OkHttpClient;

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
}
