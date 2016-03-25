package com.washmywhip.wmwvendor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.content.ContentBody;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.CoreProtocolPNames;
import cz.msebera.android.httpclient.util.EntityUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] navigationOptions;
    private Fragment currentFragment;
    private SharedPreferences mSharedPreferences;
    private View currentView;
    private VendorState vendorState;
    private LatLng currentLocation;
    private Geocoder mGeocoder;
    private int isLoaded;
    private boolean hasAccepted;
    private CountDownTimer mCountDownTimer;
    private CountDownTimer updateETAtimer;
    private Polyline mRoute;
    private WMWVendorEngine mWMWVendorEngine;
    private ConnectionManager mConnectionManager;
    private Marker end;
    private Marker start;
    private static final int BEFORE_REQUEST = 1647;
    private static final int AFTER_REQUEST = 2469;
    private static final int PROFILE_REQUEST = 3821;


    private Button startAccepting;
    private Button stopAccepting;
    private Button acceptRequest;
    private Button contactNavigation;
    private Button beginNavigation;
    private Button takeBeforePictureArrived;
    private Button beginWashArrived;
    private Button takeAfterPictureWashing;
    private Button completeWashWashing;
    private Button finalizingSubmit;
    private TextView callContact;
    private TextView textContact;
    private TextView doneContact;
    private View contactView;
    private RatingBar ratingBar;
    private EditText finalizingComments;
    private TextView userFullName;
    private Context mContext;
    private CircleImageView userCarImageArrived;
    private CircleImageView userCarImageWashing;
    private CircleImageView userImageFinalizing;
    private RelativeLayout washcontact;
    private RelativeLayout arrivedcontact;

    private BroadcastReceiver mMessageReceiver;
    private LatLng userLocation;
    private int transactionCost;
    private String transactionID;
    private String encodedBefore;
    private String encodedAfter;
    private String encodedProfile;
    private TypedFile beforeImage;
    private File beforeFile;
    private TypedFile afterImage;
    private TypedFile profileImage;
    private String carData;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mDrawerLayout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.mListView)
    ListView navDrawerList;
    @InjectView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    @InjectView(R.id.cancelToolbarButton)
    TextView editButton;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Log.d("LocationListener", "got this location: " + location.toString());
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            if (currentLocation != null && mMap != null) {
                //Only force a camera update if user has traveled 0.1 miles from last location
                if (distance(currentLocation.latitude, currentLocation.longitude, loc.latitude, loc.longitude) > 0.1) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                    currentLocation = loc;
                }

            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
            currentLocation = loc;
            //makes custome icon for markers... might be useful to mark vendors
            //IconGenerator factory = new IconGenerator(getApplicationContext());
            //Bitmap icon = factory.makeIcon("Set Location");
            // mMarker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        }
    };
    private GoogleMap.OnCameraChangeListener myCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            LatLng cameraLocation = cameraPosition.target;
            try {
                List<Address> addressList = mGeocoder.getFromLocation(cameraLocation.latitude, cameraLocation.longitude, 1);
                if (addressList.size() > 0) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private GoogleMap.OnMapClickListener myMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            Log.d("MAIN", "clearing focus on finalizingComments");
            if (finalizingComments != null && finalizingComments.hasFocus()) {
                Log.d("MAIN", "finalizingComments has focus");
                hideKeyboard(finalizingComments);
            }
        }
    };
    private LocationManager mLocationManager;


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Extract data included in the Intent
                if (intent.hasExtra("state")) {
                    String state = intent.getStringExtra("state");
                    Log.d("server connection", "RECEIVER: Got state: " + state);
                    if (state.equals("inactive")) {
                        initInactive();
                    } else if (state.equals("active")) {
                        initActive();
                    } else if (state.equals("navigating")) {
                        initNavigating();
                    } else if (state.equals("arrived")) {
                        initArrived();
                    } else if (state.equals("washing")) {
                        initWashing();
                    } else if (state.equals("finalizing")) {
                        initFinalizing();
                    } else {
                        //??
                    }
                } else if (intent.hasExtra("userInfo")) {
                    String userInfo = intent.getStringExtra("userInfo");
                    Log.d("server connection", "RECEIVER: Got userInfo: " + userInfo);
                    String[] info = userInfo.split(", ");
                    int userID = Integer.parseInt(info[0]);
                    double lat = Double.parseDouble(info[1]);
                    ;
                    double lng = Double.parseDouble(info[2]);
                    int carID = Integer.parseInt(info[3]);
                    final int washType = Integer.parseInt(info[4]);
                    mSharedPreferences.edit().putInt("userID", userID).apply();
                    mSharedPreferences.edit().putInt("carID", carID).apply();
                    mSharedPreferences.edit().putInt("washType", washType).apply();


                    initRequesting();
                    mWMWVendorEngine.getCarWithID(carID, new Callback<Object>() {
                        @Override
                        public void success(Object jsonObject, Response response) {
                            Log.d("getCar", "success");
                            String responseStr = new String(((TypedByteArray) response.getBody()).getBytes());
                            Log.d("getCar", "success " + responseStr);
                            responseStr = responseStr.replace("[", "").replace("]", "");

                            carData = responseStr;
                            Map<String, String> data = new HashMap<String, String>();
                            data = parseResponseComma(responseStr);

                            //mSharedPreferences.edit().putString("carRawData",carData).apply();
                            String color = data.get("Color");
                            String make = data.get("Model");
                            String model = data.get("Make");
                            String plate = data.get("Plate");
                            Log.d("getCar", "success " + color + " " + make);

                            mSharedPreferences.edit().putString("CarColor", color).apply();
                            mSharedPreferences.edit().putString("CarMake", make).apply();
                            mSharedPreferences.edit().putString("CarModel", model).apply();
                            mSharedPreferences.edit().putString("CarPlate", plate).apply();


                        }

                        @Override
                        public void failure(RetrofitError error) {
                            //   String responseString = new String(((TypedByteArray)error.getBody()).getBytes());
                            Log.d("getCar", "failz: " + error.toString());
                            //   Log.d("getCar", "failz: " +responseString);
                            Log.d("getCar", "failz: " + error.getCause());

                        }
                    });


                    mWMWVendorEngine.getUserWithID(userID, new Callback<JSONObject>() {
                        @Override
                        public void success(JSONObject obj, Response response) {
                            String responseString = new String(((TypedByteArray) response.getBody()).getBytes());
                            Map<String, String> userInfo = new HashMap<String, String>();
                            userInfo = parseResponse(responseString);
                            String name = userInfo.get("FirstName") + " " + userInfo.get("LastName");
                            String phoneNum = userInfo.get("Phone");
                            // Log.d("getUser", "success: "+responseString);
                            Log.d("getUser", "success: " + name);
                            mSharedPreferences.edit().putString("userFullName", name).apply();
                            mSharedPreferences.edit().putString("userPhoneNumber", phoneNum).apply();

                            String displayText = name + " has requested a wash!";
                            userFullName.setText(displayText);

                            getWashPrice(washType);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("getUser", "failz: " + error.toString());
                        }
                    });


                    userLocation = new LatLng(lat, lng);
                } else if (intent.hasExtra("requestCancel")) {
                    Log.d("server connection", "RECEIVER: Got cancel request");
                    initActive();
                    if (mConnectionManager.isConnected()) {
                        mConnectionManager.startListening(currentLocation);
                        Log.d("server connection", "RECEIVER: Got cancel request success");
                    } else {
                        Log.d("server connection", "RECEIVER: Got cancel request");
                    }
                } else if (intent.hasExtra("requestCancel")) {
                    Log.d("server connection", "RECEIVER: Got cancel request failz");
                    initActive();
                    if (mConnectionManager.isConnected()) {
                        mConnectionManager.startListening(currentLocation);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("com.android.activity.SEND_DATA"));

        navigationOptions = new String[]{"WMW Vendor", "Profile", "About", "Sign out"};
        initDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        isLoaded = -1;
        mGeocoder = new Geocoder(this);
        vendorState = VendorState.INACTIVE;
        currentView = loadingLayout;
        int view = R.layout.loading_layout;
        swapView(view);
        mWMWVendorEngine = new WMWVendorEngine();
        /*
        mWMWVendorEngine.createVendor("Rconn", "pass", "ross@connacher.com", "2039215412", new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Log.d("createVendor", "success: "+ s);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("createVendor", "failz: "+ error.toString());
            }
        });
        */
        //setup javaScript connection
        mConnectionManager = null;
        final Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectionManager = new ConnectionManager(getApplicationContext());
                //Log.d("server connection", "isConnectedAfterAdd: " + mConnectionManager.isConnected());
            }
        });
        networkThread.run();

    }

    private Map<String, String> parseResponse(String s) {
        HashMap userData = new HashMap();
        s = s.substring(1, s.length() - 1);
        s = s.replace(" ", "").replace("\t", "").replace(",", "").replace("\"", "");
        String[] dataItem = s.split("\n");
        for (int i = 1; i < dataItem.length; i++) {
            if (dataItem[i].endsWith(":")) {
                dataItem[i] = dataItem[i] + " ";
            }
            String[] info = dataItem[i].split(":");
            String key = info[0];
            String value = info[1];
            userData.put(key, value);
        }
        return userData;
    }

    private Map<String, String> parseResponseComma(String s) {
        HashMap userData = new HashMap();
        s = s.substring(1, s.length() - 1);
        s = s.replace(" ", "").replace("\t", "").replace("\"", "");
        String[] dataItem = s.split(",");
        for (int i = 1; i < dataItem.length; i++) {
            if (dataItem[i].endsWith(":")) {
                dataItem[i] = dataItem[i] + " ";
            }
            String[] info = dataItem[i].split(":");
            String key = info[0];
            String value = info[1];
            userData.put(key, value);
        }
        return userData;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {


        //  mLocationManager.req
        mMap = googleMap;
        allowLocationServices(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        mMap.setOnMapClickListener(myMapClickListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, mLocationListener);
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation!=null){
            Log.d("LocationTAG", "got last know location");
            currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
        }


    }

    private void initDrawer() {
        Log.d("TEST", "initDrawer");
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // getSupportActionBar().setTitle("Closed");
                invalidateOptionsMenu();

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //  getSupportActionBar().setTitle("Open");
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        navDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navigationOptions));
        navDrawerList.setOnItemClickListener(this);

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        Log.d("initDrawer Method", "isfiring");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position == 0) {

            addCurrentStateView();
            editButton = (TextView) findViewById(R.id.cancelToolbarButton);
            editButton.setVisibility(View.GONE);
            currentFragment = new Fragment();
            mapFragment.getView().setVisibility(View.VISIBLE);

        } else if (position == 1) {
            Log.d("TEST", "PROFILE");
            currentFragment = ProfileFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
        } else if (position == 2) {
            Log.d("TEST", "PAYMENT");
            //currentFragment = About.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
        } else if (position == 3) {
            Log.d("TEST", "LogOut");
            //log out
            attemptLogout();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        if (currentFragment != null) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, currentFragment).commit();
        }
        // Highlight the selected item, update the title, and close the drawer
        navDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(navDrawerList);
    }

    private void removeCurrentStateView() {
        currentView.setVisibility(View.GONE);
    }

    private void attemptLogout() {


        if (vendorState == VendorState.ACTIVE || vendorState == VendorState.INACTIVE) {
            mSharedPreferences.edit().clear().commit();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error logging out");
            builder.setMessage("You cannot log out while you are active. Finish your wash before you log out!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }

    private void addCurrentStateView() {
        if (vendorState == VendorState.INACTIVE) {
            initInactive();
        } else if (vendorState == VendorState.ACTIVE) {
            initActive();
        } else if (vendorState == VendorState.REQUESTING) {
            initRequesting();
        } else if (vendorState == VendorState.NAVIGATING) {
            initNavigating();
        } else if (vendorState == VendorState.ARRIVED) {
            initArrived();
        } else if (vendorState == VendorState.WASHING) {
            initWashing();
        } else if (vendorState == VendorState.FINALIZING) {
            initFinalizing();
        }
        //current view is set within the init state view
        currentView.setVisibility(View.VISIBLE);
    }

    private void initActive() {
        if (mRoute != null) {
            mRoute.remove();
        }
        if (end != null) {
            end.remove();
        }
        if (start != null) {
            start.remove();
        }
        if (currentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
        }
        if (updateETAtimer != null) {
            updateETAtimer.cancel();
        }
        //currentLocation = mMap.getCameraPosition().target;
        int view = R.layout.active_layout;
        swapView(view);
        vendorState = VendorState.ACTIVE;
        stopAccepting = (Button) findViewById(R.id.stopAccepting);
        stopAccepting.setOnClickListener(this);
    }

    private void initInactive() {
        if (mRoute != null) {
            mRoute.remove();
        }
        if (end != null) {
            end.remove();
        }
        if (start != null) {
            start.remove();
        }
        if (currentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16.0f));
        }
        if (updateETAtimer != null) {
            updateETAtimer.cancel();
        }
        vendorState = VendorState.INACTIVE;
        int view = R.layout.inactive_layout;
        swapView(view);
        startAccepting = (Button) findViewById(R.id.startAccepting);
        startAccepting.setOnClickListener(this);


    }

    private void initRequesting() {
        int view = R.layout.requesting_layout;
        swapView(view);
        vendorState = VendorState.REQUESTING;
        acceptRequest = (Button) findViewById(R.id.acceptRequest);
        acceptRequest.setOnClickListener(this);

        userFullName = (TextView) findViewById(R.id.requestingUserName);

        final TextView timer = (TextView) findViewById(R.id.progressText);
        hasAccepted = false;
        mCountDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText("" + millisUntilFinished / 1000);

            }

            @Override
            public void onFinish() {
                if (!hasAccepted) {
                    initInactive();
                }

            }
        };
        mCountDownTimer.start();
    }

    private void initNavigating() {
        int view = R.layout.navigation_layout;
        swapView(view);
        vendorState = VendorState.NAVIGATING;
        beginNavigation = (Button) findViewById(R.id.beginNavigation);
        beginNavigation.setOnClickListener(this);
        contactNavigation = (Button) findViewById(R.id.contactNavigation);
        contactNavigation.setOnClickListener(this);
        LatLng destination = userLocation;
        if (distance(userLocation.latitude, userLocation.longitude, currentLocation.latitude, currentLocation.longitude) < .25) {
            mConnectionManager.vendorHasArrived();
            initArrived();
            return;

        }
        // createRoute(destination);
        if (currentLocation != null) {
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            start = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .draggable(false).visible(false));
            end = mMap.addMarker(new MarkerOptions()
                    .position(destination)
                    .draggable(false).visible(true));
            Marker[] markers = {start, end};
            for (Marker m : markers) {
                b.include(m.getPosition());
            }
            LatLngBounds bounds = b.build();
//Change the padding as per needed
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 244, 244, 0);
            mMap.animateCamera(cu);

            updateETAtimer = new CountDownTimer(1000 * 60 * 60, 10000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    if (mConnectionManager.isConnected()) {
                        if (distance(userLocation.latitude, userLocation.longitude, currentLocation.latitude, currentLocation.longitude) < .25) {
                            mConnectionManager.vendorHasArrived();
                            initArrived();
                            updateETAtimer.cancel();

                        } else {
                            mConnectionManager.updateETA(currentLocation);
                        }
                    }
                }

                @Override
                public void onFinish() {

                }
            };
            updateETAtimer.start();
        }
    }

    private void initArrived() {
        if (mRoute != null) {
            mRoute.remove();
        }
        if (updateETAtimer != null) {
            updateETAtimer.cancel();
        }
        int view = R.layout.arrived_layout;
        swapView(view);
        vendorState = VendorState.ARRIVED;
        takeBeforePictureArrived = (Button) findViewById(R.id.arrivedBeforePicture);
        takeBeforePictureArrived.setOnClickListener(this);
        arrivedcontact = (RelativeLayout) findViewById(R.id.arrivedContact);
        arrivedcontact.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initContact();
                    arrivedcontact.setOnClickListener(null);
                    Log.d("contact", "test");
                    return true;
                }
                return false;
            }
        });

        beginWashArrived = (Button) findViewById(R.id.arrivedBeginWash);
        beginWashArrived.setOnClickListener(null);

        TextView carColorArrived = (TextView) findViewById(R.id.arrivedCarColor);
        TextView carMakeAndModlArrived = (TextView) findViewById(R.id.arrivedCarMakeAndModel);
        TextView carPlateArrived = (TextView) findViewById(R.id.arrivedCarPlate);
        TextView userNameArrived = (TextView) findViewById(R.id.arrivedUserName);

        String color = mSharedPreferences.getString("CarColor", "null");
        String make = mSharedPreferences.getString("CarMake", "null");
        String model = mSharedPreferences.getString("CarModel", "null");
        String plate = mSharedPreferences.getString("CarPlate", "null");
        String name = mSharedPreferences.getString("userFullName", "null");
        String makeAndModel = make + " " + model;

        carColorArrived.setText(color);
        carMakeAndModlArrived.setText(makeAndModel);
        carPlateArrived.setText(plate);
        userNameArrived.setText(name);


        int carID = mSharedPreferences.getInt("carID", -1);
        userCarImageArrived = (CircleImageView) findViewById(R.id.arrivedUserPicture);
        if (carID > 0) {
            Picasso.with(this)
                    .load("http://www.WashMyWhip.us/wmwapp/CarImages/car" + carID + "image.jpg")
                    .resize(60, 60)
                    .centerCrop()
                    .into(userCarImageArrived);
        }


    }

    private void initWashing() {
        int view = R.layout.washing_layout;
        swapView(view);
        vendorState = VendorState.WASHING;
        takeAfterPictureWashing = (Button) findViewById(R.id.washingAfterPicture);
        takeAfterPictureWashing.setOnClickListener(this);

        completeWashWashing = (Button) findViewById(R.id.washingCompleteWash);
        completeWashWashing.setOnClickListener(this);

        washcontact = (RelativeLayout) findViewById(R.id.washingContact);
        washcontact.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initContact();
                    washcontact.setOnClickListener(null);
                    Log.d("contact", "test");
                    return true;
                }
                return false;
            }
        });

        TextView carColorWashing = (TextView) findViewById(R.id.washingCarColor);
        TextView carMakeAndModlWashing = (TextView) findViewById(R.id.washingCarMakeAndModel);
        TextView carPlateWashing = (TextView) findViewById(R.id.washingCarPlate);
        TextView userNameWashing = (TextView) findViewById(R.id.washingUserName);

        String color = mSharedPreferences.getString("CarColor", "null");
        String make = mSharedPreferences.getString("CarMake", "null");
        String model = mSharedPreferences.getString("CarModel", "null");
        String plate = mSharedPreferences.getString("CarPlate", "null");
        String name = mSharedPreferences.getString("userFullName", "null");
        String makeAndModel = make + " " + model;

        carColorWashing.setText(color);
        carMakeAndModlWashing.setText(makeAndModel);
        carPlateWashing.setText(plate);
        userNameWashing.setText(name);

        int carID = mSharedPreferences.getInt("carID", -1);
        userCarImageWashing = (CircleImageView) findViewById(R.id.washingUserPicture);
        if (carID > 0) {
            Picasso.with(this)
                    .load("http://www.WashMyWhip.us/wmwapp/CarImages/car" + carID + "image.jpg")
                    .resize(60, 60)
                    .centerCrop()
                    .into(userCarImageWashing);
        }
    }

    private void initFinalizing() {
        int view = R.layout.finalizing_layout;
        swapView(view);
        vendorState = VendorState.FINALIZING;
        finalizingSubmit = (Button) findViewById(R.id.finalizingSubmitButton);
        finalizingSubmit.setOnClickListener(this);

        ratingBar = (RatingBar) findViewById(R.id.finalizingRating);
        finalizingComments = (EditText) findViewById(R.id.finalizingComments);

        int userID = mSharedPreferences.getInt("userID", -1);
        userImageFinalizing = (CircleImageView) findViewById(R.id.finalizingUserImage);
        if (userID > 0) {
            Picasso.with(this)
                    .load("http://www.WashMyWhip.us/wmwapp/ClientAvatarImages/client" + userID + "avatar.jpg")
                    .resize(60, 60)
                    .centerCrop()
                    .into(userImageFinalizing);
        }

    }

    private void initContact() {
        int view = R.layout.contact_layout;
        ViewGroup parent = (ViewGroup) currentView.getParent();
        contactView = getLayoutInflater().inflate(view, parent, false);
        parent.addView(contactView);
        textContact = (TextView) findViewById(R.id.contactText);
        textContact.setOnClickListener(this);
        callContact = (TextView) findViewById(R.id.contactCall);
        callContact.setOnClickListener(this);
        doneContact = (TextView) findViewById(R.id.contactDone);
        doneContact.setOnClickListener(this);


        //background cant be clicked
        if (takeAfterPictureWashing != null && completeWashWashing != null && washcontact != null) {
            takeAfterPictureWashing.setOnClickListener(null);
            completeWashWashing.setOnClickListener(null);
        }
        if (takeBeforePictureArrived != null && beginWashArrived != null && arrivedcontact != null) {
            takeBeforePictureArrived.setOnClickListener(null);
            beginWashArrived.setOnClickListener(null);
        }


    }

    public void removeContact() {
        //int view = R.layout.contact_layout;
        ViewGroup parent = (ViewGroup) currentView.getParent();
        // int index = parent.indexOfChild(contactView);
        parent.removeView(contactView);

        //background can be clicked
        if (takeAfterPictureWashing != null && completeWashWashing != null && washcontact != null) {
            takeAfterPictureWashing.setOnClickListener(this);
            completeWashWashing.setOnClickListener(this);
        }
        if (takeBeforePictureArrived != null && beginWashArrived != null && arrivedcontact != null) {
            takeBeforePictureArrived.setOnClickListener(this);
            beginWashArrived.setOnClickListener(this);
        }
        if (beginNavigation != null && contactNavigation != null) {
            beginNavigation.setOnClickListener(this);
            contactNavigation.setOnClickListener(this);
        }
    }

    public void contactCallUser() {

        String userNumber = mSharedPreferences.getString("userPhoneNumber", "2039215412");
        Log.d("contactCallUser", "call user");
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userNumber));
        startActivity(intent);


    }
    public void contactTextUser(){
        String userNumber = mSharedPreferences.getString("userPhoneNumber","2039215412");  // The number on which you want to send SMS
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", userNumber, null)));

    }

    public void launchGPS(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<" + currentLocation.latitude  + ">,<" + currentLocation.longitude + ">?q=<" + userLocation.latitude  + ">,<" + userLocation.longitude + ">(" + "User Location" + ")"));
        startActivity(intent);
    }

    public void swapView(int v) {
        Log.d("TEST", "swapView");
        ViewGroup parent = (ViewGroup) currentView.getParent();
        int index = parent.indexOfChild(currentView);
        parent.removeView(currentView);
        int view = v;
        //updates currentView
        currentView = getLayoutInflater().inflate(view, parent, false);
        parent.addView(currentView, index);
    }

    public void createRoute(LatLng destination){
        mRoute = mMap.addPolyline(new PolylineOptions()
                .add(currentLocation, destination)
                .width(10)
                .color(R.color.blue));

    }

    public void getWashPrice(int washType){
        mWMWVendorEngine.findCostOfTransactionType(washType, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Log.d("getWashPrice", "success: "+ s);
                int price = Integer.parseInt(s);
                mSharedPreferences.edit().putInt("washPrice", price).apply();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("getWashPrice", "error");
            }
        });
    }




    public void takeBeforePicture(){

    }
    public void uploadBefore(){

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data!=null) {
            Log.d("photoResult", "requestCode: " + requestCode + " ResultCode: " + requestCode + " Data: " + data.getDataString());
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 0||requestCode==1) {
                Uri photoUri = data.getData();
                Log.d("photoResult", "uri: " + photoUri.toString());

                //DO SERVER STUFF?
                //SCALE IMAGE DOWN
                // profilePicture.setImageBitmap(selectedImage);
            } else if(requestCode == BEFORE_REQUEST){
                Log.d("photoResult", "BEFORE_REQUEST:");
                Uri photoUri = data.getData();
                String selectedImagePath = null;
                Log.d("photoResult", "uri: " + photoUri.toString());


                Cursor cursor = this.getContentResolver().query(
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

                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap selectedImage = null;
                String filename = "HasImage";

                File sd = this.getCacheDir();

                Log.d("photoResult", "getExternalStorageDirectory: name=" + sd.getName()+ " path= "+ sd.getPath());
                File dest = new File(selectedImagePath);
                dest.setWritable(true);
                dest.setReadable(true);
                //File newDest = new File(sd,"HasImage");


              //  File newDest = new File(dest.getParent()+File.separator+filename);
                try {
                    dest.createNewFile();
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);

                    FileOutputStream fos = new FileOutputStream(dest);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos);
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                    fos.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }

                beforeImage = new TypedFile("image/jpeg",dest);
                beforeFile = dest;
                beginWashArrived.setOnClickListener(this);
                beginWashArrived.setBackgroundResource(R.drawable.rounded_corner_blue);


            } else if(requestCode == AFTER_REQUEST){
                Log.d("photoResult", "AFTER_REQUEST: ");
                completeWashWashing.setOnClickListener(this);
                completeWashWashing.setBackgroundResource(R.drawable.rounded_corner_blue);


            } else if(requestCode == PROFILE_REQUEST){
                Log.d("photoResult", "PROFILE_REQUEST: ");

            }
        }
    }

    private void selectImage(final int requestCode) {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    /** calculates the distance between two locations in MILES */
    // http://stackoverflow.com/questions/18170131/comparing-two-locations-using-their-longitude-and-latitude
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return dist; // output distance, in MILES
    }
    public void allowLocationServices(boolean allow){

        if(allow) {
            //enable
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
           // Location myLocation = mMap.get
        } else {
            //disable
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(false);
        }
    }


    @Override
    public void onClick(View v) {
        if(startAccepting!=null &&v.getId() == startAccepting.getId()){
            if(currentLocation!=null){
                startAccepting.setOnClickListener(null);
                initActive();
                if(mConnectionManager.isConnected()){
                    mConnectionManager.startListening(currentLocation);
                }
            }

        } else if (stopAccepting!=null &&v.getId() == stopAccepting.getId()) {
            stopAccepting.setOnClickListener(null);
            initInactive();
            if(mConnectionManager.isListening()){
                mConnectionManager.stopListening();
            }
           // initRequesting();
        } else if (acceptRequest!=null &&v.getId() == acceptRequest.getId()) {
            acceptRequest.setOnClickListener(null);
            if(mCountDownTimer!=null){
                mCountDownTimer.cancel();
            }
            hasAccepted = true;
            if(mConnectionManager.isConnected()){
                mConnectionManager.acceptRequest(currentLocation);
                mConnectionManager.updateETA(currentLocation);
            }
            initNavigating();

        } else if (beginNavigation!=null &&v.getId() == beginNavigation.getId()) {
            beginNavigation.setOnClickListener(null);
            // LAUNCH GOOGLE MAPS with current location and user location (sent from server)

            launchGPS();

        } else if (contactNavigation!=null &&v.getId() == contactNavigation.getId()) {
            contactNavigation.setOnClickListener(null);
            //CALL/TEXT USER6

           initContact();
        } else if (beginWashArrived!=null &&v.getId() == beginWashArrived.getId()) {
            beginWashArrived.setOnClickListener(null);
            if(mConnectionManager.isConnected()){
                //transactionID
                int userID = mSharedPreferences.getInt("userID",-1);
                int carID = mSharedPreferences.getInt("carID", -1);
                int washType = mSharedPreferences.getInt("washType",-1);
                int vendorID = Integer.parseInt(mSharedPreferences.getString("VendorID", "-1"));
                int cost = mSharedPreferences.getInt("cost",20);
                final boolean[] hasError = new boolean[1];
                Log.d("createTransaction","BEFORE: "+ userID+" "+carID+" "+ washType+" "+ vendorID);
                if(userID>=0 &&carID>=0 &&vendorID>=0 &&washType>=0){

                    /*
                    PictureUploader uploader = new PictureUploader(0);
                    RequestParams params = new RequestParams();
                    try {
                        Log.d("createTransaction", "BEFORE: " + HasImage.isFile() + "name:  " + HasImage.getName()+ "path:  "+ HasImage.getPath());
                        params.put("HasImage",HasImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    params.put("userID",userID);
                    params.put("carID",carID);
                    params.put("type",washType);
                    params.put("cost",cost);
                    params.put("vendorID",vendorID);

                    uploader.post("http://www.WashMyWhip.us/wmwapp/createTransaction.php", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String responseString = null;
                            try {
                                responseString = new String(responseBody,"UTF8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Log.d("createTransaction", "success(new):" + responseString);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d("createTransaction", "fail(new): " + error.toString());
                        }
                    });

                    String user =  "" + userID;
                    String car =  "" + carID;
                    String vendor =  "" + vendorID;
                    String type =  "" + washType;
                    String price =  "" + cost;
                    Ion.with(this)
                            .load("http://www.WashMyWhip.us/wmwapp/createTransaction.php")
                            .setMultipartParameter("userID", user)
                            .setMultipartParameter("carID", car)
                            .setMultipartParameter("vendorID", vendor)
                            .setMultipartParameter("type", type)
                            .setMultipartParameter("cost", price)
                            .setMultipartFile("HasImage", "image/jpeg", beforeFile)
                            .asString()
                            .setCallback(new FutureCallback<String>() {
                                @Override
                                public void onCompleted(Exception e, String result) {
                                    Log.d("createTransaction", "???");
                                    if(result!=null){
                                        Log.d("createTransaction", "" + result);
                                    }
                                    if(e!=null){
                                        Log.d("createTransaction", ""+ e.getMessage());
                                    }
                                }

                            });



                    */
                    /*
                    String user =  "" + userID;
                    String car =  "" + carID;
                    String vendor =  "" + vendorID;
                    String type =  "" + washType;
                    String price =  "" + cost;
                    createTransactionRequest request = new createTransactionRequest(user,car,vendor,type,price,beforeImage);
                    mWMWVendorEngine.createTransaction(request, new Callback<String>() {
                        @Override
                        public void success(String str, Response response) {
                            Log.d("createTransaction","success: "+ str);
                            //save transactionID to sharedPrefs
                            transactionID = str;
                            mSharedPreferences.edit().putString("transactionID",transactionID).apply();
                            if(mConnectionManager.isConnected()){
                               // mConnectionManager.vendorHasInitiatedWash(transactionID);
                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("createTransaction","error: "+ error.toString());

                            //vendor should stay in arrived state
                            hasError[0] = true;
                        }
                    });
                    */
                }

                //mConnectionManager.vendorHasInitiatedWash();
            }
            initWashing();

        } else if (takeBeforePictureArrived!=null &&v.getId() == takeBeforePictureArrived.getId()) {

            selectImage(BEFORE_REQUEST);

        } else if (completeWashWashing!=null &&v.getId() == completeWashWashing.getId()) {
            completeWashWashing.setOnClickListener(null);
            initFinalizing();

        } else if (takeAfterPictureWashing!=null &&v.getId() == takeAfterPictureWashing.getId()) {
            selectImage(AFTER_REQUEST);

        } else if (finalizingSubmit!=null &&v.getId() == finalizingSubmit.getId()) {
            finalizingSubmit.setOnClickListener(null);
            int rating = ratingBar.getProgress();
            String comments = finalizingComments.getText().toString();
            Log.d("finalizingSubmit","comments: "+ comments+ ", numstars: "+ rating);
            hideKeyboard(finalizingComments);
            initInactive();

        } else if (callContact!=null &&v.getId() == callContact.getId()) {
            callContact.setOnClickListener(null);
            removeContact();
            contactCallUser();
            Log.d("MAIN ACTIVITY", "call pressed");
            //call user

        } else if (textContact!=null &&v.getId() == textContact.getId()) {
            textContact.setOnClickListener(null);
            Log.d("MAIN ACTIVITY", "text pressed");
            removeContact();
            contactTextUser();
            //text user

        } else if (doneContact!=null &&v.getId() == doneContact.getId()) {
            doneContact.setOnClickListener(null);
            removeContact();
        }
    }
}
