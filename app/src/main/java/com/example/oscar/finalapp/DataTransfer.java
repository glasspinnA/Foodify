package com.example.oscar.finalapp;

import java.util.ArrayList;

/**
 * Interface som används för att kunna skicka data från ett fragment till activity
 * @author Oscar
 */
public interface DataTransfer {
    void addMarker(MarkerLocation data);
    void removeMarker(ArrayList<MarkerLocation> arrCopy, ArrayList<MarkerLocation> mOrignalMarkerArray);
    void isEdit(boolean isEdit);

}
