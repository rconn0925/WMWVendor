package com.washmywhip.wmwvendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PROFILE_REQUEST = 3820;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private SharedPreferences mSharedPreferences;
    private String username, email, phone;
    private  int vendorID;
    private String encodedProfile;


    @InjectView(R.id.pictureProfile)
    CircleImageView profilePicture;

    @InjectView(R.id.usernameProfile)
    EditText usernameProfile;
    @InjectView(R.id.emailProfile)
    EditText emailEditText;
    @InjectView(R.id.phoneProfile)
    EditText phoneEditText;

    TextView editButton;



    KeyListener defaultKeyListener;

    private WMWVendorEngine mWMWVendorEngine;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            // mCars = (ArrayList<Car>) getArguments().getSerializable("cars");
        }
        //get car list from server
    }

    public void initEditable(){

        editButton.setText("Save");
        profilePicture.setOnClickListener(this);
        usernameProfile.setKeyListener(defaultKeyListener);
        usernameProfile.setEnabled(true);
        emailEditText.setKeyListener(defaultKeyListener);
        emailEditText.setEnabled(true);
        phoneEditText.setKeyListener(defaultKeyListener);
        phoneEditText.setEnabled(true);

        EditText[] fields = {usernameProfile,emailEditText,phoneEditText};

        for(EditText field:fields){
            if(field.hasFocus()){
                hideKeyboard(field);
            }
        }



    }

    public void initNotEditable() {

        editButton.setText("Edit");
        usernameProfile.setActivated(false);
        usernameProfile.setKeyListener(null);
        usernameProfile.setEnabled(false);
        emailEditText.setKeyListener(null);
        emailEditText.setEnabled(false);
        phoneEditText.setKeyListener(null);
        phoneEditText.setEnabled(false);
        profilePicture.setOnClickListener(null);

        username = usernameProfile.getText().toString();
        email = emailEditText.getText().toString();
        phone = phoneEditText.getText().toString();

        Log.d("updateVendor", username + " " + " " + email + " " + phone);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Hold on!");
        builder.setMessage("You are requesting to update your profile information. Is all the information correct?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int vendorID = Integer.parseInt(mSharedPreferences.getString("VendorID", "-1"));
                Log.d("updateVendor", "id: " + vendorID);
                mWMWVendorEngine.updateVendorInfo(vendorID, email, username, phone, new Callback<Object>() {
                    @Override
                    public void success(Object o, Response response) {
                        Log.d("updateUser", "success " + o.toString());
                        mSharedPreferences.edit().putString("Username", username).commit();
                        mSharedPreferences.edit().putString("Email", email).commit();
                        mSharedPreferences.edit().putString("Phone", phone).commit();


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("updateVendor", "failz " + error.toString());
                    }
                });
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("PROFILE", "onCreateView");
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, v);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mWMWVendorEngine = new WMWVendorEngine();
        editButton = (TextView) getActivity().findViewById(R.id.cancelToolbarButton);
        editButton.setOnClickListener(this);

        profilePicture = (CircleImageView)v.findViewById(R.id.pictureProfile);
         profilePicture.setOnClickListener(null);

        //should be getting info from server, not shared prefs
        username = mSharedPreferences.getString("Username", "");
        email = mSharedPreferences.getString("Email", "");
        phone = mSharedPreferences.getString("Phone", "");
        vendorID = Integer.parseInt(mSharedPreferences.getString("VendorID", "-1"));

        usernameProfile.setText(username);
        emailEditText.setText(email);
        phoneEditText.setText(phone);




        defaultKeyListener = usernameProfile.getKeyListener();

        editButton.setText("Edit");
        editButton.setVisibility(View.VISIBLE);
        usernameProfile.setActivated(false);
        usernameProfile.setKeyListener(null);
        usernameProfile.setEnabled(false);
        emailEditText.setKeyListener(null);
        emailEditText.setEnabled(false);
        phoneEditText.setKeyListener(null);
        phoneEditText.setEnabled(false);
        Picasso.with(getActivity())
                .load("http://www.WashMyWhip.us/wmwapp/VendorAvatarImages/vendor" + vendorID + "avatar.jpg")
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(100, 100)
                .centerCrop()
                .into(profilePicture);

        return v;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("PROFILE", "Attaching");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d("PROFILE", "Detatching");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == editButton.getId()) {
            if(editButton.getText().toString().equals("Save")){
                Log.d("FAPMENU TEXTVIEW", "SAVE CLICK");
                initNotEditable();

            } else if (editButton.getText().toString().equals("Edit")){
                Log.d("FAPMENU TEXTVIEW", "EDIT CLICK");
                initEditable();
            }
        }   else if(v.getId()==profilePicture.getId()){
            selectImage(PROFILE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data!=null) {
            Log.d("BLAHBLAH", "requestCode: " + requestCode + " ResultCode: " + requestCode + " Data: " + data.getDataString());
            super.onActivityResult(requestCode, resultCode, data);

            Uri photoUri = data.getData();
            String selectedImagePath = null;
            Log.d("photoResult", "uri: " + photoUri.toString());


            Cursor cursor = getActivity().getContentResolver().query(
                    photoUri, null, null, null, null);
            if (cursor == null) {
                selectedImagePath = photoUri.getPath();
                Log.d("photoResult", "(null)path: " + selectedImagePath);
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                selectedImagePath = cursor.getString(idx);
                Log.d("photoResult", "path: " + selectedImagePath);
            }

            Bitmap selectedImage = null;
            byte[] byteArray = null;
            try {
                selectedImage =Bitmap.createBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 60, stream);

                byteArray = stream.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (requestCode == 0||requestCode==1) {

                Log.d("BLAHBLAH", "uri 0 or 1: " + photoUri.toString());

            } else if (requestCode == PROFILE_REQUEST){
                Log.d("photoResult", "PROFILE REQUEST: " + photoUri.toString());
                encodedProfile = Base64.encodeToString(byteArray, Base64.DEFAULT);
                profilePicture.setImageBitmap(selectedImage);
                mWMWVendorEngine.uploadVendorAvatarImageAndroid(vendorID, encodedProfile, new Callback<Object>() {
                    @Override
                    public void success(Object s, Response response) {
                        Log.d("vendorAvatarUpload", "Success " +s.toString());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                      //  String json =  new String(((TypedByteArray)error.getResponse().getBody()).getBytes());

                        Log.d("vendorAvatarUpload", "error: "+ error.getMessage());
                    }
                });



            }
        }
    }

    private void selectImage(final int requestCode) {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //0 is request code
                    startActivityForResult(intent, requestCode);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), requestCode);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
