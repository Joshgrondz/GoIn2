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


        //Waits till event status is TRUE
        while(!eventController.EventInformation.getBoolean("status")){
            System.out.println("Event Not Active Yet waiting to track");
            pauseForOneMinute();
            eventController.updateEventInformation(client);
        }
        System.out.println("Event now Active, tracking students\n");


        //Get Chaperone Data
        System.out.println("Getting chaperone Data");
        eventController.updateChaperone(client);

        //get students locations
        System.out.println("\nGet Students locations");
        eventController.updateStudentGroup(client);

        //get students who should be part of the class trip
        System.out.println("\nGet Students attending");
        eventController.updateStudentAttandingList(client);

        //Whose location has not been tracked yet?
        System.out.println("\nSee who isnt being tracked");
        eventController.checkWhoIsntBeingTracked();


        System.out.println("\nTracking Students locations with while loop");
        while(eventController.EventInformation.getBoolean("status")){

            System.out.println("\nUpdating student locations");
            //update Student Locations
            eventController.updateStudentGroup(client);
            eventController.updateStudentAttandingList(client);
            //update chaperone location
            eventController.updateChaperone(client);

            if(!eventController.allStudentsTracked()){ //Figure
                eventController.checkWhoIsntBeingTracked(); //Test
            }


            if(!eventController.checkGeofence()){
                System.out.println("Student outside of geofence");
                List<User> student = eventController.getStudentsOutsideFence();
                System.out.println(student);
            }
            if(!eventController.checkGoIn2Groups()){ //test
                System.out.println("Students outside of GoIn2Groups");
                List<GoIn2Group> student = eventController.getGoIn2GroupsOutsideFence();
                System.out.println(student); //Format this better please
            }
            if(!eventController.checkChaperoneGeofence()){
                System.out.println("Students outside of Chaperone Geofence");
                List<User> student = eventController.getStudentsOutsideChaperone();
                System.out.println(student);
            }

            System.out.println("\nChecking for stale locations");
            eventController.checkStaleLocations();
            pauseForOneMinute();
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
