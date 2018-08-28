package com.example.oscar.finalapp;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Adapter för att göra det möjligt att skräddarsy hur de rutor som dyker upp ovanför markörer när man klickar på en markör i kartan ska se ut.
 * @author Oscar
 */

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
    private Context mContext;


    public InfoWindowAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * Metod som intiterar komponenter till InfoWindow
     * @param marker - Det marker objekt som infowindow ska hantera
     * @return - view
     */
    @Override
    public View getInfoContents(Marker marker) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_infowindow,null);
        TextView tvStoreName = view.findViewById(R.id.tvInforStoreName);
        TextView tvNote = view.findViewById(R.id.tvInfoNote);
        tvStoreName.setText(marker.getTitle());
        tvNote.setText(marker.getSnippet());
        return view;
    }
}
