package com.example.oscar.finalapp;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Klass som används hantera notifikationer när en användare passerar ett geofence
 * Har följt Android officiella guiden för geofences enligt https://developer.android.com/training/location/geofencing
 * Har dock modifierat kod som finns i guiden för att anpassa efter mina krav för denna klassen
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG  = "GeofenceIntent";

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    /**
     * Metod som ta reda på vilka geofences användaren befinner sig i.
     * Metoden delegerar till andra metoder i klassen att en notifikering ska skickas ut till användaren, som ska innehålla
     * kort information om vilket geofence (affär) användaren är i närheten av
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = String.valueOf(geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geofenceTransitionDetails = getGeofenceTransitionDetails(triggeringGeofences
            );

            sendNotification(geofenceTransitionDetails, this);
        }
    }


    /**
     * Metod som tar emot en lista på alla geofences som har blivit aktiverade dvs de geofences som användaren befinner sig i.
     * Metoden retunrerar en String som ska innehålla namnet på det geofence användaren befinner sig i (Den affär användaren är i närhetene av)
     * @param triggeringGeofences
     * @return
     */
    private String getGeofenceTransitionDetails(List<Geofence> triggeringGeofences) {
        ArrayList<String> locationNames = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            locationNames.add(geofence.getRequestId());
        }
        String triggeringLocationsString = TextUtils.join("", locationNames);

        return triggeringLocationsString;
    }

    /**
     * Metod som skickar ut en notifikation när användaren befinner sig inne i ett geofence.
     * Notifikationen ska innehålla information om att användaren befinner sig inne i ett geofence och vilket geofence det är (Vilken butik det är).
     * Om användaren klickar på notifikationen ska appen starts upp.
     * @param geofenceTransitionDetails
     * @param geofenceTransitionsIntentService
     */
    private void sendNotification(String geofenceTransitionDetails, GeofenceTransitionsIntentService geofenceTransitionsIntentService) {
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


        String CHANNEL_ID = "Foodify";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Foodify")
                        .setContentText("You have entered " + geofenceTransitionDetails)
                        .setVibrate(new long[] {0,200,400,600,800,1000})
                        .setContentIntent(notificationPendingIntent);
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }

}