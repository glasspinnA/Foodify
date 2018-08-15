package com.example.oscar.finalapp;



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
 * Fragment för den sida i applikationen där användaren kan ta bort platser och se
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


    @Override
    public void onResume() {
        super.onResume();
        ((MapsActivity)getActivity()).supportInvalidateOptionsMenu();
        mArray = getArguments().getParcelableArrayList("arrayWithMarkers");
        CustomListViewAdapter mAdapter = new CustomListViewAdapter(getActivity(), R.layout.custom_listview, mArray);
        mAdapter.setListener(this);
        mListView.setAdapter(mAdapter);
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
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        Log.d(TAG,"ONCE");

        ((MapsActivity) getActivity()).setActionBarTitle("Edit Location");
        setHasOptionsMenu(true);

        mArray = new ArrayList<>();
        mArray = getArguments().getParcelableArrayList("arrayWithMarkers");
        arrCopy = new ArrayList<>(mArray);
        Log.d(TAG,"Size before removal:" + String.valueOf(mArray.size()));

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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDataTransfer = (DataTransfer) context;
    }

    @Override
    public void onClick(MarkerLocation name) {
        Log.d(TAG,name.getId());
        for(MarkerLocation i: mArray){
            if(i.getId().equals(name.getId())){
                mArray.remove(i);
                mDataTransfer.removeMarker(mArray,arrCopy);
                break;
            }
        }
        Log.d(TAG,"Size after removal:" + String.valueOf(mArray.size()));
        Log.d(TAG,"Size of copy before removal:" + String.valueOf(arrCopy.size()));
    }

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
