package com.example.oscar.finalapp;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DataTransfer {

    private GoogleMap mMap;
    private Toolbar mTopToolbar;
    boolean mPermissionDenied = false;
    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient;
    private final int GEOFENCE_RADIUS_IN_METER = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "MapsActivity";
    private ArrayList<MarkerLocation> mArray = new ArrayList<>();
    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();
    private ArrayList<String> mMakersToRemoveArray;
    private ArrayList<MarkerLocation> mCopyMarkerArray = new ArrayList<>();

    private boolean isEdit = false;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);

        mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGeofencingClient = LocationServices.getGeofencingClient(this);


        sharedPreferences = getSharedPreferences("startUp", Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean("isFirstStart",true)){
            sharedPreferences.edit().putBoolean("isFirstStart",false).commit();
            Intent intent = new Intent(getApplicationContext(),TutorialActivity.class);
            finish();
            startActivity(intent);
        }



    }


    /**
     * Metod för att ändra titeln på actionbaren
     * @param title - Den titel man vill sätta på actionbaren
     */
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    @Override
    protected void onStop() {
        super.onStop();
        int counter = 0;

        if(mArray != null){
            for(MarkerLocation i : mArray){
                String convertedToString = String.valueOf((i).toString());  //method 1
                sharedPreferences.edit().putString("mKey" + String.valueOf(counter), convertedToString).commit();
                counter++;
            }

            sharedPreferences.edit().putInt("mCounter",counter).commit();
        }
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

        InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(infoWindowAdapter);



        /*
        if(mArray!=null){
            for(MarkerLocation mLocation : mArray){
                addMarkerForGeofence(mLocation.getLatLng().latitude, mLocation.getLatLng().longitude, mLocation.getStoreName());
                mMap.addMarker(new MarkerOptions().position(mLocation.getLatLng()).title(mLocation.getStoreName()).snippet(mLocation.getNote()));
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(mLocation.getLatLng())
                        .radius(100)
                        .strokeColor(R.color.colorPrimaryDark)
                        .fillColor(R.color.secondaryDarkColor));
            }
        }

        */

        ArrayList<String> test = new ArrayList<>();

        if(sharedPreferences.contains("mCounter")){
            int counter = sharedPreferences.getInt("mCounter",-1);
            for(int i=0; i<counter; i++){
                String testShared = sharedPreferences.getString("mKey" +String.valueOf(i),null);
                Log.d(TAG, "TEST SHARED: " +testShared);

                test.add(testShared);

                String[] parts = null;

                for(String t : test){
                    Log.d(TAG,t);
                    parts = t.split(",");
                }

                LatLng latLng = new LatLng(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
                mArray.add(new MarkerLocation(latLng,parts[2],parts[3],parts[4],parts[5]));

                for(MarkerLocation j : mArray){
                    Log.d(TAG,"J" + j.getNote());
                    mMap.addMarker(new MarkerOptions().position(j.getLatLng()).title(j.getStoreName()).snippet(j.getNote())).showInfoWindow();

                }

            }
        }else {
            Log.d(TAG,"NO");
        }

        enableMyLocation();
    }


    /**
     * Metod som gör det möjligt att se sin egna position på kartan
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14f));
        }
    }


    /**
     * Metod som lägger till en geofence en plats
     * @param lat - Latituden för platsen som ska läggas till
     * @param lng - Longituden för platsen som ska läggas till
     * @param storeName - Namnet på den plats som ska läggas till
     */
    private void addMarkerForGeofence(double lat, double lng, String storeName) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                String key = storeName;
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(key)
                        .setCircularRegion(
                                lat,
                                lng,
                                GEOFENCE_RADIUS_IN_METER
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()
                );

                mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), R.string.GeofenceAddedSucess, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), R.string.GeofenceAddedFailed, Toast.LENGTH_SHORT).show();
                            }
                        });
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Metod som tar bort geofences för en plats eller flera platser
     * @param geofenceRequestIds - Array som innehåller alla de platser som ska tas bort från geofences
     */
    private void removeMarkerForGeofence(final List<String> geofenceRequestIds){
        mGeofencingClient.removeGeofences(geofenceRequestIds)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Removal of marker sucess!");
                        if(isEdit == false){
                            removeMarkersFromMap();
                            Log.d(TAG,"KOMMER IN HÄR");
                        }else{
                            removeMarkersFromMapEdit();
                        }
                        Toast.makeText(getApplicationContext(),R.string.markerRemovedSucess, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Removal of marker sucess!");
                        Toast.makeText(getApplicationContext(),R.string.markerRemovedFail, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeMarkersFromMapEdit() {
        mMap.clear();

        for(MarkerLocation m : mArray){
            Log.d(TAG,m.getStoreName());
            mMap.addMarker(new MarkerOptions().position(m.getLatLng()).title(m.getStoreName()).snippet(m.getNote())).showInfoWindow();
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(m.getLatLng())
                    .radius(100)
                    .strokeColor(R.color.colorPrimaryDark)
                    .fillColor(R.color.secondaryDarkColor));
        }
    }

    /**
     * Metod som tar bort markörer från kartan
     */
    private void removeMarkersFromMap() {
        mMap.clear();

        for(MarkerLocation m : mCopyMarkerArray){
            Log.d(TAG,m.getStoreName());
            mMap.addMarker(new MarkerOptions().position(m.getLatLng()).title(m.getStoreName()).snippet(m.getNote())).showInfoWindow();
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(m.getLatLng())
                    .radius(100)
                    .strokeColor(R.color.colorPrimaryDark)
                    .fillColor(R.color.secondaryDarkColor));
        }

        mArray = mCopyMarkerArray;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    /**
     * Metod som hämtar data efter att telefon har blivit roterad
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            mArray = savedInstanceState.getParcelableArrayList("markerArray");
            mCopyMarkerArray = savedInstanceState.getParcelableArrayList("copyArray");
        }
    }


    /**
     * Metod som sparar data som gör så att inget försvinner när man roterar skärmen
     * @param //outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("markerArray",mArray);
        outState.putParcelableArrayList("copyArray",mCopyMarkerArray);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mTopToolbar.setTitle("Map");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (item.getItemId() == R.id.addLocation) {
            CreateFragment mCreateFragment = new CreateFragment();
            fragmentTransaction.replace(R.id.maps_container,mCreateFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if(item.getItemId() == R.id.editLocation){
            isEdit = false;
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("arrayWithMarkers",mArray);
            EditFragment mEditFragment = new EditFragment();
            mEditFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.maps_container,mEditFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if(item.getItemId() == R.id.privacy){
            PrivacyFragment mPrivacyFragment = new PrivacyFragment();
            fragmentTransaction.replace(R.id.maps_container,mPrivacyFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void splitArray(ArrayList<MarkerLocation> copyArray) {
        Log.d(TAG,"Size of removal array " + String.valueOf(copyArray.size()));
        Log.d(TAG,"Size of orginal array " + String.valueOf(mArray.size()));
        // Make the two lists
        ArrayList<String> orginalMarkerList = new ArrayList<>();
        for (MarkerLocation i : mArray) {
            orginalMarkerList.add(i.getId());
        }
        ArrayList<String> copyMarkerList = new ArrayList<>();
        for (MarkerLocation j : copyArray) {
            copyMarkerList.add(j.getId());
        }

        // Prepare a mMakersToRemoveArray
        mMakersToRemoveArray = new ArrayList<String>(orginalMarkerList);
        mMakersToRemoveArray.addAll(copyMarkerList);

        // Prepare an intersection
        ArrayList<String> intersection = new ArrayList<String>(orginalMarkerList);
        intersection.retainAll(copyMarkerList);

        // Subtract the intersection from the mMakersToRemoveArray
        mMakersToRemoveArray.removeAll(intersection);

        // Print the result
        for (String n : mMakersToRemoveArray) {
            System.out.println("BASH " + n);
        }

        if (mMakersToRemoveArray != null && mMakersToRemoveArray.size() > 0) {
            removeMarkerForGeofence(mMakersToRemoveArray);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    private ArrayList<MarkerLocation> test = new ArrayList<>();

    /**
     * Metod som lägger till en markör på kartan
     * @param data - MarkerLocation objekt innehållande data om platsen som ska bli tillagd i kartan
     */
    @Override
    public void addMarker(MarkerLocation data) {
        if(mArray.size() == 0){
            mMap.addMarker(new MarkerOptions().position(data.getLatLng()).title(data.getStoreName()).snippet(data.getNote())).showInfoWindow();
            addMarkerForGeofence(data.getLatLng().latitude,data.getLatLng().longitude,data.getStoreName());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(data.getLatLng()));
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(data.getLatLng())
                    .radius(100)
                    .strokeColor(R.color.colorPrimaryDark)
                    .fillColor(R.color.secondaryDarkColor));
            mArray.add(data);
        }else{
            for(MarkerLocation m : mArray){
                if(!m.getId().equals(data.getId())){
                    mMap.addMarker(new MarkerOptions().position(data.getLatLng()).title(data.getStoreName()).snippet(data.getNote())).showInfoWindow();
                    addMarkerForGeofence(data.getLatLng().latitude,data.getLatLng().longitude,data.getStoreName());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(data.getLatLng()));
                    Circle circle = mMap.addCircle(new CircleOptions()
                            .center(data.getLatLng())
                            .radius(100)
                            .strokeColor(R.color.colorPrimaryDark)
                            .fillColor(R.color.secondaryDarkColor));
                    mArray.add(data);
                    break;
                }else{
                    Log.d(TAG,"LOCATION FINNS REDAN");
                }
            }
        }
    }


    @Override
    public void removeMarker(ArrayList<MarkerLocation> copyArray, ArrayList<MarkerLocation> orginalMarkerArray) {
        this.mArray = orginalMarkerArray;
        this.mCopyMarkerArray = copyArray;
        splitArray(copyArray);
    }

    @Override
    public void isEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public ArrayList<MarkerLocation> getArray() {
        Log.d(TAG,"mArray " + mArray.get(2).getNote());
        if(test.size() == 2){

            Log.d(TAG,"TEST" + test.get(1).getNote());
        }
        Log.d(TAG,"TEST" + test.get(0).getNote());
        return mArray;
    }
}


