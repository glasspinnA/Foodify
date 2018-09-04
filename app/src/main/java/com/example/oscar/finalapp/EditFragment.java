package com.example.oscar.finalapp;



import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Fragment för den sida i applikationen där användaren kan ta bort och redigera inköpslistor
 * och få en överblick vilka inköpslistor som finns sparade
 * @author Oscar
 */
public class EditFragment extends Fragment implements AdapterListener {
    private ArrayList<MarkerLocation> mArray;
    private DataTransfer mDataTransfer;
    private static final String TAG = "EditFragment";
    private ArrayList<MarkerLocation> arrCopy;
    private ListView mListView;

    public EditFragment() {
        // Required empty public constructor
    }


    /**
     * Metod som kallas när Fragmentet blir åter onResume
     * Uppdaterar listviewn i fragmentet med uppdaterad data om det finns någon sådan data
     */
    @Override
    public void onResume() {
        super.onResume();
        ((MapsActivity)getActivity()).supportInvalidateOptionsMenu();
        mArray = getArguments().getParcelableArrayList("arrayWithMarkers");
        CustomListViewAdapter mAdapter = new CustomListViewAdapter(getActivity(), R.layout.custom_listview, mArray);
        mAdapter.setListener(this);
        mListView.setAdapter(mAdapter);
    }

    /**
     * Kod för att hantera hur actionbaren i detta fragment ser ut
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
     * Metod för att intitierar komponenter som finns i detta fragmentet.
     * @param inflater -
     * @param container -
     * @param savedInstanceState -
     * @return - view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ((MapsActivity) getActivity()).setActionBarTitle("Edit Location");
        setHasOptionsMenu(true);

        mArray = new ArrayList<>();
        mArray = getArguments().getParcelableArrayList("arrayWithMarkers");
        arrCopy = new ArrayList<>(mArray);

        mListView = view.findViewById(R.id.lw);
        CustomListViewAdapter mAdapter = new CustomListViewAdapter(getActivity(), R.layout.custom_listview, mArray);
        mAdapter.setListener(this);
        mListView.setAdapter(mAdapter);

        TextView tvEmptyList = view.findViewById(R.id.tvEmptyList);

        if(mArray.size() == 0){
            tvEmptyList.setText(R.string.empty_list);
        }else{
            tvEmptyList.setVisibility(View.GONE);
        }
        return view;
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
     * Metod som blir kallad när fragmentet blir ihopbundet med dess Context. För APIer lägre än 23
     * @param activity -
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDataTransfer = (DataTransfer) activity;

    }

    /**
     * Metod som kallas när användaren klickar på ta bort knappen i listviewn
     * Metoden tar bort den rad i lisview som användaren har tryckt på (Tar bort det markerLocation objektet)
     * @param name - Det MarkerLocation objekt (Markör) som ska tas bort
     */
    @Override
    public void removeMarker(MarkerLocation name) {
        for(MarkerLocation i: mArray){
            if(i.getId().equals(name.getId())){
                mArray.remove(i);
                mDataTransfer.removeMarker(mArray,arrCopy);
                break;
            }
        }
    }

    /**
     * Metod som kallas när när användaren har tryckt på edit knappen i listviewn
     * Metoden skickar vilken position (rad i listview) som har blivit tryckt på till
     * CreateFragment.
     * @param position - Positionen i listview som har blivit tryckt på av användaren, används sedan för att
     *                 veta vilken position i arrayen med alla MarkerLocation objekt (Markörer) man ska redigera med ny data
     */
    @Override
    public void editMarker(int position) {
        Bundle bundle = new Bundle();
        CreateFragment createFragment = new CreateFragment();
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        bundle.putParcelableArrayList("markerArray",mArray);
        bundle.putInt("position",position);
        createFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.maps_container, createFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
