package com.example.oscar.finalapp;

/**
 * Interface för lyssna efter om användaren klickar på ta bort knappen eller redigera en plats knappen
 * i listview över alla locations
 */
public interface AdapterListener {
    void onClick(MarkerLocation markerLocation);
    void editMarker(int position);
}