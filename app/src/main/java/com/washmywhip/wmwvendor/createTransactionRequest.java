package com.washmywhip.wmwvendor;

import java.io.File;

import retrofit.mime.TypedFile;

/**
 * Created by Ross on 3/23/2016.
 */
public class createTransactionRequest {

    final String userID;
    final String carID;
    final String vendorID;
    final String washType;
    final String cost;
    final TypedFile HasImage;

    createTransactionRequest(String userID, String carID,String vendorID,String washType,String cost, TypedFile image){
        this.userID = userID;
        this.carID = carID;
        this.vendorID = vendorID;
        this.washType = washType;
        this.cost = cost;
        this.HasImage = image;

    }

}
