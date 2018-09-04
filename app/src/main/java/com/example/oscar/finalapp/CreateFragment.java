package com.example.oscar.finalapp;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * Fragment för den vy i applikationen där användaren skapar en inköpslista för en mataffär
 * @author Oscar
 */
public class CreateFragment extends Fragment implements PlaceSelectionListener {
    private EditText mEtNote;
    private LatLng mLatLng;
    private CharSequence mStoreName;
    private CharSequence mAdress;
    private String mId;
    private TextView mPlaceDetailsText;
    private PlaceAutocompleteFragment mAutocompleteFragment;
    private DataTransfer mDataTransfer;
    private static final String TAG = "CreateFragment";
    private String KEY_ID,STORE_NAME,NOTE,ADRESS;
    private LatLng PLACE_LOCATION;
    private int position;
    private ArrayList<MarkerLocation> markerArray = new ArrayList<>();
    private ArrayList<MarkerLocation> copyMarkerArray;
    private boolean isEdit = false;
    boolean isAdressFieldFilled = false;


    public CreateFragment() {
        // Required empty public constructor
    }





    /**
     * Metod som initierar komponenter som hör till detta fragmentet så som textViews och den widget för att kunna söka efter platser.
     * Tar även emot data ifrån EditFragment ifall användaren har tryckt på redigera knappen i EditFragment.
     * @param inflater -
     * @param container -
     * @param savedInstanceState -
     * @return - view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        ((MapsActivity) getActivity()).setActionBarTitle("Add Location");
        setHasOptionsMenu(true);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(mButtonListener);
        mEtNote = view.findViewById(R.id.etNote);
        mAutocompleteFragment = (PlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        mAutocompleteFragment.setOnPlaceSelectedListener(this);
        mAutocompleteFragment.setHint("Enter an address");
        mPlaceDetailsText = view.findViewById(R.id.place_details);
        mPlaceDetailsText.setText("");

        mAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(clearButtonListener);
        setFilterForWidget();

        Bundle bundle = this.getArguments();
        if(bundle != null){
            position = bundle.getInt("position");
            markerArray = bundle.getParcelableArrayList("markerArray");
            copyMarkerArray = new ArrayList<>(markerArray);
            isEdit = true;
            mEtNote.setText(markerArray.get(position).getNote().toString());
            ((MapsActivity) getActivity()).setActionBarTitle("Edit your location");
        }

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey("adressFieldState")){
                this.isAdressFieldFilled = savedInstanceState.getBoolean("adressFieldState");
                if(isAdressFieldFilled){
                    double lat = savedInstanceState.getDouble("lat");
                    double lng = savedInstanceState.getDouble("lng");
                    this.mLatLng = new LatLng(lat,lng);
                    this.mStoreName = savedInstanceState.getString("storeName");
                    this.mAdress = savedInstanceState.getString("adress");
                }
                this.mEtNote.setText(savedInstanceState.getString("note"));
            }

        }

        return view;
    }


    /**
     * Metod för att spara data vid rotation av telefon
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(isAdressFieldFilled){
            outState.putDouble("lat",mLatLng.latitude);
            outState.putDouble("lng",mLatLng.longitude);
            outState.putString("storeName",mStoreName.toString());
            outState.putString("adress",mAdress.toString());
            outState.putBoolean("adressFieldState", isAdressFieldFilled);
            outState.putString("note",mEtNote.getText().toString());
        }
    }


    /**
     * Knapplyssnare för att lyssna ifall användaren har tryckt på ta bort knappen (X:et) i adresssökfältet
     * Används för att förhindra att en användare kan lägga till en plats utan att ha en adress inskrivet
     */
    private View.OnClickListener clearButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mAutocompleteFragment.setText("");
            isAdressFieldFilled = false;
        }
    };

    /**
     * Metod som sätter ett filter.
     * Filltret filltrerar bland sökresultaten så att svenska mataffärer priorirteras när användaren söker efter en mataffär i sökfältet
     */
    private void setFilterForWidget() {
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_GROCERY_OR_SUPERMARKET)
                .setCountry("SE")
                .build();
        mAutocompleteFragment.setFilter(autocompleteFilter);
    }


    /**
     * Knapplyssnare metod
     * Metod som skapar ett MarkerLocation objekt för den inköpslista som användaren håller på att skapa.
     */
    private Button.OnClickListener mButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(markerArray !=null && isEdit && isAdressFieldFilled){
                markerArray.remove(position);
                mDataTransfer.removeMarker(copyMarkerArray, markerArray);
                isEdit = false;
                mDataTransfer.isEdit(true);
            }

            Boolean isNoteFieldFilled = !TextUtils.isEmpty(mEtNote.getText());

            if(isAdressFieldFilled && isNoteFieldFilled)  {
                getLocationValues();
                passData(new MarkerLocation(PLACE_LOCATION,
                        NOTE,
                        KEY_ID,
                        STORE_NAME,
                        ADRESS
                ));
                mEtNote.setText("");
                mAutocompleteFragment.setText("");
                mAutocompleteFragment.setHint("Search");
                isAdressFieldFilled = false;
            }else if(!isNoteFieldFilled){
                mEtNote.setError("Add a note is required");
            }else if(!isAdressFieldFilled){
                mAutocompleteFragment.setHint("ADRESS IS REQUIRED!");
            }
        }
    };

    /**
     * Metod som hämtar värden för en geografisk plats och sparar dem i respektive variabel
     */
    public void getLocationValues(){
        KEY_ID = mStoreName.toString();
        STORE_NAME = mStoreName.toString();
        ADRESS = mAdress.toString();
        NOTE = mEtNote.getText().toString();
        PLACE_LOCATION = mLatLng;
    }


    /**
     * Metod som hämtar information som finns tillgänglig för det sökresultat man har tryckt på i sökfältet när man söker efter mataffärer
     * @param place - Ett Place objekt från Google som innehåller all information om den plats man har sökt efter i widgeten
     */
    @Override
    public void onPlaceSelected(Place place) {
        mLatLng = place.getLatLng();
        mId = place.getId();
        mStoreName = place.getName();
        mAdress = place.getAddress();
        isAdressFieldFilled = true;
    }



    /**
     * Metod för att hantera layouten för actionbaren i detta fragmentent
     */
    @Override
    public void onResume() {
        super.onResume();
        ((MapsActivity)getActivity()).supportInvalidateOptionsMenu();
    }

    /**
     * Metod för att hantera layouten för actionbaren i detta fragmentent
     * @param menu -
     * @param inflater -
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem editItem = menu.findItem(R.id.editLocation);
        MenuItem createItem = menu.findItem(R.id.addLocation);
        MenuItem privacyItem = menu.findItem(R.id.privacy);
        editItem.setVisible(false);
        createItem.setVisible(false);
        privacyItem.setVisible(false);
    }


    /**
     * Metod som skickar en toast ifall något fel inträffar under tiden man skriver in plats i Google widgeten
     * @param status - Felmeddelande
     */
    @Override
    public void onError(Status status) {
        Toast.makeText(getActivity(), "Place selection failed: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }


    /**
     * Metod som blir kallad när fragmentet blir ihopbundet med dess Context
     * @param context -
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataTransfer = (DataTransfer) context;
    }

    /**
     * Metod som blir kallad när fragmentet blir ihopbundet med dess activity. Metod som kallas för APIer längre än 23
     * @param activity -
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDataTransfer = (DataTransfer) activity;
    }

    /**
     * Metod som skickar ett MarkerLocation objekt med interfacet DataTransfer till MapsActivity
     * @param markerLocation - Objekt av MarkerLocation innehållande information av den plats och inköpslista man har skapat
     */
    public void passData(MarkerLocation markerLocation) {
        mDataTransfer.addMarker(markerLocation);
    }
}
