package com.example.oscar.finalapp;

/**
 * Interface som används för att lyssna efter vilken knapp användaren trycker på i listview över alla inköpslistor
 * @author Oscar
 */
public interface AdapterListener {
    void removeMarker(MarkerLocation markerLocation);
    void editMarker(int position);
}