package com.example.oscar.finalapp;


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
 * Fragment för den sida inuti applikationen där man skapar påminelse för en plats samt skapar en inköpslista
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

    public CreateFragment() {
        // Required empty public constructor
    }


    /**
     * Metod som initierar komponenter som hör till detta fragmentet så som textViews och den widget för att kunna söka efter platser
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
        mPlaceDetailsText = view.findViewById(R.id.place_details);
        mPlaceDetailsText.setText(",");
        setFilterForWidget();

        Bundle bundle = this.getArguments();
        if(bundle != null){
            position = bundle.getInt("position");
            markerArray = bundle.getParcelableArrayList("markerArray");
            copyMarkerArray = new ArrayList<>(markerArray);
            Log.d(TAG, String.valueOf(position) + " / " + String.valueOf(markerArray.size()));
            isEdit = true;
            mEtNote.setText(markerArray.get(position).getNote().toString());
            ((MapsActivity) getActivity()).setActionBarTitle("Edit your location");

        }
        return view;
    }

    /**
     * Metod som sätter ett filter. Filltret filltrerar mellan sökreulstat så svenska butiker/matäffrer priorirteras i sökresultatet för användaren.
     */
    private void setFilterForWidget() {
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_GROCERY_OR_SUPERMARKET)
                .setCountry("SE")
                .build();
        mAutocompleteFragment.setFilter(autocompleteFilter);
    }


    /**
     * Knapplyssnare som lyssnar ifall användaren klickar på knappen för att lägga till en plats objekt
     */
    private Button.OnClickListener mButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(markerArray !=null && isEdit){
                markerArray.remove(position);
                mDataTransfer.removeMarker(copyMarkerArray, markerArray);
                isEdit = false;
                mDataTransfer.isEdit(true);
            }

            if(!TextUtils.isEmpty(mEtNote.getText()) || mPlaceDetailsText.getText().toString().matches("")) {
                getLocationValues();
                passData(new MarkerLocation(PLACE_LOCATION,
                        NOTE,
                        KEY_ID,
                        STORE_NAME,
                        ADRESS
                ));
                mEtNote.setText("");
                mAutocompleteFragment.setText("");
            }
            else{
                mEtNote.setError("Add a note is required");
            }
        }
    };

    /**
     * Metod som hämtar värden för en plats och sparar dem i respektive variabel
     */
    public void getLocationValues(){
        KEY_ID = mStoreName.toString();
        STORE_NAME = mStoreName.toString();
        ADRESS = mAdress.toString();
        NOTE = mEtNote.getText().toString();
        PLACE_LOCATION = mLatLng;
    }


    /**
     * Metod som hämtar den information som kommer från Google efter att man har sökt efter en plats i widgeten
     * @param place - Ett Place objekt från Google som innehåller all information om den plats man har sökt efter i widgeten
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());
        mLatLng = place.getLatLng();
        mId = place.getId();
        mStoreName = place.getName();
        mAdress = place.getAddress();



        // Format the returned place's details and display them in the TextView.
        mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(), place.getId(),place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));

        /*
        CharSequence attributions = place.getAttributions();
        if (!TextUtils.isEmpty(attributions)) {
            mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
        } else {
            mPlaceAttribution.setText("");
        }
        */
    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id, CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber, websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber, websiteUri));
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
     * @param menu
     * @param inflater
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
        Log.e(TAG, "onError: Status = " + status.toString());
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
     * Metod som skickar en markerLocation objekt till interfacet DataTransfer
     * Används för att kunna skicka data från fragment till MapsActivity
     * @param markerLocation - Objekt av MarkerLocation innehållande information av den plats man har skapat
     */
    public void passData(MarkerLocation markerLocation) {
        mDataTransfer.addMarker(markerLocation);
    }
}
