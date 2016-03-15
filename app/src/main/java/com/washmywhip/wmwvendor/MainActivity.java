package com.washmywhip.wmwvendor;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener {

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


    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mDrawerLayout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.mListView)
    ListView navDrawerList;
    @InjectView(R.id.loadingLayout)
    RelativeLayout loadingLayout;

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());;
            if(currentLocation!=null && mMap!=null){
                //Only force a camera update if user has traveled 0.1 miles from last location
                if(distance(currentLocation.latitude,currentLocation.longitude,loc.latitude,loc.longitude)>0.1){
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

        if(position == 0){

            addCurrentStateView();
            currentFragment = new Fragment();
            mapFragment.getView().setVisibility(View.VISIBLE);

        } else if (position==1){
            Log.d("TEST", "PROFILE");
            currentFragment = ProfileFragment.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
        } else if (position==2){
            Log.d("TEST", "PAYMENT");
            //currentFragment = About.newInstance();
            mapFragment.getView().setVisibility(View.INVISIBLE);
            removeCurrentStateView();
        } else if (position==3){
            Log.d("TEST", "LogOut");
            //log out
            attemptLogout();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        if(currentFragment!=null){
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
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void addCurrentStateView() {
        if(vendorState==VendorState.INACTIVE){
            initInactive();
        } else if(vendorState == VendorState.ACTIVE) {
            initActive();
        } else if(vendorState == VendorState.REQUESTING) {
            initRequesting();
        } else if(vendorState == VendorState.NAVIGATING) {
            initNavigating();
        } else if(vendorState == VendorState.ARRIVED) {
            initArrived();
        } else if(vendorState == VendorState.WASHING) {
            initWashing();
        } else if(vendorState == VendorState.FINALIZING) {
            initFinalizing();
        }
        //current view is set within the init state view
        currentView.setVisibility(View.VISIBLE);
    }

    private void initActive() {
    }
    private void initInactive() {
        int view = R.layout.inactive_layout;
        swapView(view);
    }
    private void initRequesting() {
    }
    private void initNavigating() {
    }
    private void initArrived() {
    }
    private void initWashing() {
    }
    private void initFinalizing() {
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
}
