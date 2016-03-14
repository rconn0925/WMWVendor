package com.washmywhip.wmwvendor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mDrawerLayout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.mListView)
    ListView navDrawerList;


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
        vendorState = VendorState.INACTIVE;
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

        // Add a marker in Sydney and move the camera

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

}
