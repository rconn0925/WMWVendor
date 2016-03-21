package com.washmywhip.wmwvendor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
                    if (isLoaded == -1) {
                        isLoaded = 1;
                        initInactive();
                    }
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
            if(finalizingComments!=null&&finalizingComments.hasFocus()){
                Log.d("MAIN", "finalizingComments has focus");
                hideKeyboard(finalizingComments);
            }
        }
    };

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        mMap = googleMap;
        allowLocationServices(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        mMap.setOnMapClickListener(myMapClickListener);


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
        mSharedPreferences.edit().clear().commit();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
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
        int view = R.layout.active_layout;
        swapView(view);
        stopAccepting = (Button) findViewById(R.id.stopAccepting);
        stopAccepting.setOnClickListener(this);
    }

    private void initInactive() {
        int view = R.layout.inactive_layout;
        swapView(view);
        startAccepting = (Button) findViewById(R.id.startAccepting);
        startAccepting.setOnClickListener(this);

    }

    private void initRequesting() {
        int view = R.layout.requesting_layout;
        swapView(view);
        acceptRequest = (Button) findViewById(R.id.acceptRequest);
        acceptRequest.setOnClickListener(this);

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
        beginNavigation = (Button) findViewById(R.id.beginNavigation);
        beginNavigation.setOnClickListener(this);
        contactNavigation = (Button) findViewById(R.id.contactNavigation);
        contactNavigation.setOnClickListener(this);
        LatLng destination = null;
        //createRoute(destination);
    }

    private void initArrived() {
        int view = R.layout.arrived_layout;
        swapView(view);
        takeBeforePictureArrived = (Button) findViewById(R.id.arrivedBeforePicture);
        takeBeforePictureArrived.setOnClickListener(this);

        beginWashArrived = (Button) findViewById(R.id.arrivedBeginWash);
        beginWashArrived.setOnClickListener(this);


    }

    private void initWashing() {
        int view = R.layout.washing_layout;
        swapView(view);
        takeAfterPictureWashing = (Button) findViewById(R.id.washingAfterPicture);
        takeAfterPictureWashing.setOnClickListener(this);

        completeWashWashing = (Button) findViewById(R.id.washingCompleteWash);
        completeWashWashing.setOnClickListener(this);
    }

    private void initFinalizing() {
        int view = R.layout.finalizing_layout;
        swapView(view);
        finalizingSubmit = (Button) findViewById(R.id.finalizingSubmitButton);
        finalizingSubmit.setOnClickListener(this);

        ratingBar = (RatingBar) findViewById(R.id.finalizingRating);
        finalizingComments = (EditText) findViewById(R.id.finalizingComments);

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
        beginNavigation.setOnClickListener(null);
        contactNavigation.setOnClickListener(null);


    }

    public void removeContact() {
        //int view = R.layout.contact_layout;
        ViewGroup parent = (ViewGroup) currentView.getParent();
        // int index = parent.indexOfChild(contactView);
        parent.removeView(contactView);

        //background can be clicked
        beginNavigation.setOnClickListener(this);
        contactNavigation.setOnClickListener(this);
    }

    public void contactCallUser() {
        String userNumber = "2039215412";
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + userNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);


    }
    public void contactTextUser(){
        String number = "2039215412";  // The number on which you want to send SMS
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));

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
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(currentLocation, destination)
                .width(5)
                .color(R.color.blue));

    }

    public void takeBeforePicture(){

    }
    public void takeAfterPicture(){

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data!=null) {
            Log.d("BLAHBLAH", "requestCode: " + requestCode + " ResultCode: " + requestCode + " Data: " + data.getDataString());
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 0||requestCode==1) {
                Uri photoUri = data.getData();
                Log.d("BLAHBLAH", "uri: " + photoUri.toString());
                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //DO SERVER STUFF?
                //SCALE IMAGE DOWN
                // profilePicture.setImageBitmap(selectedImage);
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //0 is request code
                    startActivityForResult(intent, 0);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), 1);
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
            startAccepting.setOnClickListener(null);
            initActive();

        } else if (stopAccepting!=null &&v.getId() == stopAccepting.getId()) {
            stopAccepting.setOnClickListener(null);
           // initInactive();
            initRequesting();
        } else if (acceptRequest!=null &&v.getId() == acceptRequest.getId()) {
            stopAccepting.setOnClickListener(null);
            if(mCountDownTimer!=null){
                mCountDownTimer.cancel();
            }
            hasAccepted = true;
            initNavigating();
        } else if (beginNavigation!=null &&v.getId() == beginNavigation.getId()) {
            beginNavigation.setOnClickListener(null);
            // LAUNCH GOOGLE MAPS with current location and user location (sent from server)




            initArrived();

        } else if (contactNavigation!=null &&v.getId() == contactNavigation.getId()) {
            contactNavigation.setOnClickListener(null);
            //CALL/TEXT USER
           initContact();
        } else if (beginWashArrived!=null &&v.getId() == beginWashArrived.getId()) {
            beginWashArrived.setOnClickListener(null);
            initWashing();

        } else if (takeBeforePictureArrived!=null &&v.getId() == takeBeforePictureArrived.getId()) {
            takeBeforePictureArrived.setOnClickListener(null);

        } else if (completeWashWashing!=null &&v.getId() == completeWashWashing.getId()) {
            completeWashWashing.setOnClickListener(null);
            initFinalizing();

        } else if (takeAfterPictureWashing!=null &&v.getId() == takeAfterPictureWashing.getId()) {
            takeAfterPictureWashing.setOnClickListener(null);

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
            Log.d("MAIN ACTIVITY","done pressed");
            removeContact();

        }
    }
}
