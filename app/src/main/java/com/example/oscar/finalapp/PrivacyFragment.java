package com.example.oscar.finalapp;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


/**
 * Fragment som hanterar den sida där användaren kan läsa om privacy policy
 * */
public class PrivacyFragment extends Fragment {


    public PrivacyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MapsActivity)getActivity()).supportInvalidateOptionsMenu();
    }

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MapsActivity) getActivity()).setActionBarTitle("Privacy Information");
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_privacy, container, false);
    }

}
