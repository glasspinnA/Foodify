package com.example.oscar.finalapp;

/**
 * Interface för lyssna efter om användaren klickar på ta bort knappen
 * i listview över alla locations
 */
public interface AdapterListener {
    void onClick(MarkerLocation name);
    void editMarker(int position);
}