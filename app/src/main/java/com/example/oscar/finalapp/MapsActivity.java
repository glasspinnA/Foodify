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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity klass som representerar den vy i appen där man ser Google Maps kartan m.m
 * @author Oscar
 */

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


    /**
     * Metod som initierar komponenter som hör till denna activity så som så som actionbar.
     * Metoden kontrollerar även ifall det är första gången applikationen startas, då om det är fallet så ska TutorialActivity
     * startas för att ge användaren, guiden till hur man använder applikationen
     * @param savedInstanceState -
     */
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

        SharedPreferences sharedPreferences = getSharedPreferences("startUp", Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("isFirstStart",true)){
            sharedPreferences.edit().putBoolean("isFirstStart",false).commit();
            Intent intent = new Intent(getApplicationContext(),TutorialActivity.class);
            startActivity(intent);
            finish();
        }

    }

    /**
     * Metod för att ändra titeln på actionbaren
     * @param title - Den titel man vill sätta på actionbaren
     */
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    /**
     * Metod som körs när Google Maps kartan initieras.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(infoWindowAdapter);
        enableMyLocation();
    }


    /**
     * Metod som kontrollerar ifall applikationen har tillåtelse för att använda mobilens GPS
     * Om tillåtelse finns, gör metoden så att det är möjligt att se sin egna position på Google maps kartan
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        else if (mMap != null) {
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
     * Metod som lägger till en geofence för en mataffär/inköpslista
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
                        if(isEdit == false){
                            removeMarkersFromMap();
                        }else{
                            removeMarkersFromMapEdit();
                        }
                        Toast.makeText(getApplicationContext(),R.string.markerRemovedSucess, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),R.string.markerRemovedFail, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Metod som tar bort markörer från kartan när man redigerar en inköpslista objekt
     */
    private void removeMarkersFromMapEdit() {
        mMap.clear();
        for(MarkerLocation m : mArray){
            mMap.addMarker(new MarkerOptions().position(m.getLatLng()).title(m.getStoreName()).snippet(m.getNote())).showInfoWindow();
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(m.getLatLng())
                    .radius(100)
                    .strokeColor(R.color.colorPrimaryDark)
                    .fillColor(R.color.secondaryDarkColor));
        }
    }

    /**
     * Metod som tar bort markörer från kartan när användaren väljer att radera en inköpslista
     */
    private void removeMarkersFromMap() {
        mMap.clear();
        for(MarkerLocation m : mCopyMarkerArray){
            mMap.addMarker(new MarkerOptions().position(m.getLatLng()).title(m.getStoreName()).snippet(m.getNote())).showInfoWindow();
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(m.getLatLng())
                    .radius(100)
                    .strokeColor(R.color.colorPrimaryDark)
                    .fillColor(R.color.secondaryDarkColor));
        }

        mArray = mCopyMarkerArray;
    }


    /**
     * Metod som används för att specefiera på vilket sätt geofences för inköpslistorna ska fungera
     * I detta fallet ska geofences triggas endast när användaren går in i ett geofence
     * Metod hämtad från https://developer.android.com/training/location/geofencing#java
     * @return -
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    /**
     * Metod som triggar igång så att notifikationer kan skickas ut till användaren när användaren går in i ett geofence
     * Metod hämtad från https://developer.android.com/training/location/geofencing#java
     * @return-
     */
    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    /**
     * Metod som hämtar data efter att telefon har blivit roterad
     * @param savedInstanceState -
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


    /**
     * Metod för att hantera actionbaren / app baren
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * Metod för att sätta rätt titel på actionbaren / app baren när användaren trycker på bakåtknappen på telefonen
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mTopToolbar.setTitle("Map");
    }


    /**
     * Metod som startar Fragments beroende vilken knapp i app baren / actionbaren som användaren trycker på
     * @param item -
     * @return -
     */
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


    /**
     * Metod som kontrollerar ifall ett permisison är tillåtet eller ej, och därmed utför olika funktioner beroende på
     * om permissionet är godkänt eller ej
     * @param requestCode - Kod för request
     * @param permissions - String som innehåller det akutella permissionet
     * @param grantResults - Visar ifall permission är tillåtet eller ej
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }


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
                    Toast.makeText(getApplication(),"THE LOCATION ALREADY EXIST",Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * Metod för att ta bort markörer
     * @param copyArray - Array som innehåller alla MarkerLocation objekt innan bortagning
     * @param orginalMarkerArray - Array som innehåller alla MarkerLocation objekt efter bortagning av specefik MarkerLocation objekt
     */
    @Override
    public void removeMarker(ArrayList<MarkerLocation> copyArray, ArrayList<MarkerLocation> orginalMarkerArray) {
        this.mArray = orginalMarkerArray;
        this.mCopyMarkerArray = copyArray;
        splitArray(copyArray);
    }

    /**
     * Metod används för att tar reda på vilket MarkerLocation objekt som har blivit bortaget, när användaren har tagit bort en inköpslista
     * från Listview i EditFragment. Detta behövs ta reda på då man måste avregistrerar det geofencet för det bortagna inköpslista objektet
     * man har nyss tagit bort. (Så att det inte ligger kvar ett geofence runt en mataffär där ingen inköpslista existerar längre)
     *
     * Genom att man har en array som innehåller alla MarkerLocation objekt innan bortagning av det aktuella objektet/inköpslistan
     * och en annan array som innehåller alla MarkerLocatio objekt efter bortagning av det aktuella objektet, så kan man ta reda på vilket
     * objekt som saknas i arreyen som innnehåller alla MarkerLocation objekt innan borttagning genom att jämföra arrayerna med varandra.
     * De objekt som saknas i är de som har blivit bortagna av användaren och ska sådelse avregistreras från geofence
     * @param copyArray
     */
    private void splitArray(ArrayList<MarkerLocation> copyArray) {
        ArrayList<String> orginalMarkerArr = new ArrayList<>();
        for (MarkerLocation i : mArray) {
            orginalMarkerArr.add(i.getId());
        }
        ArrayList<String> copyMarkerArr = new ArrayList<>();
        for (MarkerLocation j : copyArray) {
            copyMarkerArr.add(j.getId());
        }

        mMakersToRemoveArray = new ArrayList<>(orginalMarkerArr);
        mMakersToRemoveArray.addAll(copyMarkerArr);

        ArrayList<String> overFlowMarkerArray = new ArrayList<>(orginalMarkerArr);
        overFlowMarkerArray.retainAll(copyMarkerArr);
        mMakersToRemoveArray.removeAll(overFlowMarkerArray);

        if (mMakersToRemoveArray != null && mMakersToRemoveArray.size() > 0) {
            removeMarkerForGeofence(mMakersToRemoveArray);
        }
    }

    /**
     * @param isEdit - Boolean som är True om det är redigering eller bortagning av inköpslista som sker.
     *               Detta behövs veta för dessa två funktionerna triggar olika metoder och kodrader i koden.
     */
    @Override
    public void isEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    /**
     * Metod som retunerar array som innehåller MarkerLocation objekt
     * @return - Array innehållandes MarkerLocation objekt
     */
    public ArrayList<MarkerLocation> getArray() {
        return mArray;
    }
}


