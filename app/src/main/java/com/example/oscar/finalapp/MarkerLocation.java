package com.example.oscar.finalapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Ett objekt som innehåller all nödvändig data som applikationen kommer använda sig av en plats
 */

public class MarkerLocation implements Parcelable {
    private LatLng mLatLng;

    public void setmStoreName(String mStoreName) {
        this.mStoreName = mStoreName;
    }

    private String mNote;
    private String mId;
    private String mStoreName;
    private String mAdress;

    /**
     * Konstruktor
     * @param mLatLng - GPS Positionen för platsen man skapar
     * @param mNote - Inköslistan för platsen man skapar
     * @param mId - ID för platsen man skapar
     * @param mStoreName - Namnet för platsen man skapar
     * @param mAdress - Addressen för platsen man skapar
     */
    public MarkerLocation(LatLng mLatLng, String mNote, String mId, String mStoreName, String mAdress) {
        this.mLatLng = mLatLng;
        this.mNote = mNote;
        this.mId = mId;
        this.mStoreName = mStoreName;
        this.mAdress = mAdress;
    }

    protected MarkerLocation(Parcel in) {
        mLatLng = in.readParcelable(LatLng.class.getClassLoader());
        mNote = in.readString();
        mId = in.readString();
        mStoreName = in.readString();
        mAdress = in.readString();
    }


    public static final Creator<MarkerLocation> CREATOR = new Creator<MarkerLocation>() {
        @Override
        public MarkerLocation createFromParcel(Parcel in) {
            return new MarkerLocation(in);
        }

        @Override
        public MarkerLocation[] newArray(int size) {
            return new MarkerLocation[size];
        }
    };

    public LatLng getLatLng(){
        return mLatLng;
    }

    public String getNote(){
        return mNote;
    }

    public String getId(){
        return mId;
    }

    public String getStoreName() {
        return mStoreName;
    }

    public String getAdress() {
        return mAdress;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mLatLng, i);
        parcel.writeString(mNote);
        parcel.writeString(mId);
        parcel.writeString(mStoreName);
        parcel.writeString(mAdress);
    }
}
