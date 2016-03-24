package com.washmywhip.wmwvendor;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Ross on 3/22/2016.
 */
public class PictureUploader {

    private static final String BASE_URL = "http://www.WashMyWhip.us/wmwapp/BeforeImages";
    private static final String BEFORE_URL = "/BeforeImages";
    private static final String AFTER_URL = "/AfterImages";
    private static final String PROFILE_URL = "/VendorAvatarImages";


    private AsyncHttpClient client;

    public PictureUploader(int request){
        client = new AsyncHttpClient();
        client.setLoggingEnabled(true);
        if(request == 0){
            String url = BASE_URL + BEFORE_URL;

        } else if(request == 1){
            String url = BASE_URL + AFTER_URL;
        } else if(request == 2){
            String url = BASE_URL + PROFILE_URL;
        } else {
            Log.d("PictureUploader","InvalidRequest");
        }

    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

}
