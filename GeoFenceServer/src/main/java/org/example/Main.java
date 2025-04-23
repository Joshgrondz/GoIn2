package org.example;

import org.json.JSONArray;
import org.json.JSONObject;


import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/*
TO-DO-List
 - add logic for if student's location hasn't been updated for more than a certain time
*/


public class Main {
    public static void main(String[] args) {
        //Establish Needed variables

        int eventID = Integer.valueOf(args[0]);

        //Client to access web
        HttpClient client = HttpClient.newHttpClient();


        //Print out what event we are doing
        System.out.println("Event id being tracked: " + args[0] + "\n --------------- \n");

        //Create Geofence
        EventGeofenceController eventController = EventGeofenceController.CreateGeoFenceController(eventID, client);
        if(eventController != null){
            System.out.println("Event Controller Made");
            System.out.println(eventController);
        }



        while(!eventController.EventInformation.getBoolean("status")){
            System.out.println("Event Not Active Yet waiting to track");
            pauseForOneMinute();
            eventController.updateEventInformation(client);
        }
        System.out.println("Event now Active, tracking students");

        //Get Chaperone Data
        eventController.updateChaperone(client);

        //get students locations
        eventController.updateStudentGroup(client);

        //get students who should be part of the class trip
        eventController.updateStudentAttandingList(client);

        //Whose location has not been tracked yet?


        eventController.checkWhoIsntBeingTracked();

        //Check Locations
        //Every minute
        //Only students who are tracked
        //Notify if outside or broken rule


        while(eventController.EventInformation.getBoolean("status")){
            if(!eventController.checkGeofence()){
                System.out.println("Student outside of geofence");
            }
            if(!eventController.checkGoIn2Groups()){
                System.out.println("Students outside of GoIn2Groups");
            }
            if(!eventController.checkChaperoneGeofence()){
                System.out.println("Students outside of Chaperone Geofence");
            }
            eventController.checkStaleLocations();
            pauseForOneMinute();

            //update Student Locations
            eventController.updateStudentGroup(client);
            eventController.updateStudentAttandingList(client);

            //update chaperone location
            eventController.updateChaperone(client);

            if(!eventController.allStudentsTracked()){
                eventController.checkWhoIsntBeingTracked();
            }
            eventController.updateEventInformation(client);
        }



    }

    public static void pauseForOneMinute() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
