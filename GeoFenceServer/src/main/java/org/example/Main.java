package org.example;

import org.json.JSONArray;
import org.json.JSONObject;


import java.net.http.HttpClient;


/*
TO-DO-List
 - Remove Syncronized from needed Classes
 - 
*/


public class Main {
    public static void main(String[] args) {
        //Establish Needed variables

        //Client to access web
        HttpClient client = HttpClient.newHttpClient();


        //Print out what event we are doing
        System.out.println("Event id being tracked: " + args[0] + "\n --------------- \n");

        //Create Geofence
        EventGeofenceController eventController = EventGeofenceController.CreateGeoFenceController(Integer.valueOf(args[0]), client);

        //get students locations

        //get students who should be part of the class trip

        //Check who has not gotten a location

        //Create users from the students with locations


        //Check Locations
        //Every minute
        //Only students who are tracked
        //Notify if outside or broken rule


        //Check to see if started location tracking




    }
}
