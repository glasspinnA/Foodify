package com.example.oscar.finalapp;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom Adapter för lisview som visar alla locations i EditFragment
 */
public class CustomListViewAdapter extends ArrayAdapter<MarkerLocation> {

    private ArrayList<MarkerLocation> mMarkerArray;
    private Context mContext;
    private int mResource;
    private static final String TAG = "CustomListViewAdapter";
    private AdapterListener mListener;


    /**
     * Konstruktor för adaptern
     * @param mContext - Context
     * @param mResource - Resource
     * @param mMarkerArray - Array innehållande alla markörer
     */
    public CustomListViewAdapter(@NonNull Activity mContext, int mResource, ArrayList<MarkerLocation> mMarkerArray) {
        super(mContext, mResource, mMarkerArray);
        this.mContext = mContext;
        this.mResource = mResource;
        this.mMarkerArray = mMarkerArray;
    }

    /***
     * Metod som initierar alla komponenter så som knappar och textviews samt
     * har en knapplyssnare på "ta bort en plats" knappen och "redigera en plats" knappen i listviewn
     * @param position - Positionen på det element som blir tryckt på av användaren i listviewn
     * @param convertView -
     * @param parent -
     * @return - View
     */
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(mResource,parent,false);

        TextView tvStoreName = view.findViewById(R.id.tvStoreName);
        TextView tvAdress = view.findViewById(R.id.tvAdress);
        TextView tvNote = view.findViewById(R.id.tvNote);



        Button btnDelete = view.findViewById(R.id.btnDelete);
        MarkerLocation markerLocation = mMarkerArray.get(position);


        Button btnEdit = view.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.editMarker(position);
            }
        });

        tvStoreName.setText(markerLocation.getStoreName());
        tvAdress.setText(markerLocation.getAdress());
        tvNote.setText(markerLocation.getNote());

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.dialog_delete_marker)
                        .setPositiveButton(R.string.yes_delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onClick(getItem(position));
                                CustomListViewAdapter.this.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(TAG,"Cancel");
                            }
                        }).show();

            }
        });

        return view;
    }


    /**
     * Metod för att sätta lyssnare
     * @param listener - knapplyssnare
     */
    public void setListener(AdapterListener listener) {
        this.mListener = listener;
    }


}