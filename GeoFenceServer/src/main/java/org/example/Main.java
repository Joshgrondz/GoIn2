package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/*
TO-DO-List
 - Review notification messages for clarity across different fence types.
 - Consider edge cases and further error handling.
*/


public class Main {
    // Time in minutes a student must be outside a boundary before student notification
    private static final long STUDENT_NOTIFICATION_THRESHOLD_MINUTES = 1;
    // Time in minutes a student/group must be outside a boundary before chaperone notification
    private static final long CHAPERONE_NOTIFICATION_THRESHOLD_MINUTES = 1;
    // Pause duration between checks in milliseconds (e.g., 30000 = 30 seconds)
    private static final long SERVER_PAUSE_MILLISECONDS = 30000;
    // Pause duration when waiting for event to start in milliseconds (e.g., 60000 = 1 minute)
    private static final long EVENT_WAIT_PAUSE_MILLISECONDS = 60000;
    // Pause duration after an error occurs in milliseconds (e.g., 60000 = 1 minute)
    private static final long ERROR_PAUSE_MILLISECONDS = 60000;
    // -----------------------------------------------------------

    public static void main(String[] args) {
        //Establish Needed variables
        if (args == null || args.length == 0) {
            System.err.println("Error: Event ID must be provided as a command-line argument.");
            System.exit(1);
        }
        int eventID = Integer.parseInt(args[0]);

        HttpClient client = HttpClient.newHttpClient();

        System.out.println("Event id being tracked: " + eventID + "\n --------------- \n");

        //Create Geofence Controller
        EventGeofenceController eventController = null;
        try {
            eventController = EventGeofenceController.CreateGeoFenceController(eventID, client);
            if(eventController != null){
                System.out.println("Event Controller Made");
                System.out.println(eventController);
            } else {
                System.err.println("Failed to create Event Controller. Exiting.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error creating Event Geofence Controller: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        //Waits till event status is TRUE
        while(!eventController.EventInformation.getBoolean("status")){
            System.out.println("Event Not Active Yet waiting to track");
            pauseServer(EVENT_WAIT_PAUSE_MILLISECONDS); // Use configured wait pause
            eventController.updateEventInformation(client);
        }
        System.out.println("Event now Active, tracking students\n");


        // --- Maps for Main Geofence ---
        Map<Integer, Instant> studentsOutsideMainSince = new HashMap<>();
        Map<Integer, Set<String>> mainNotificationsSent = new HashMap<>();
        // --- Maps for GoIn2Groups ---
        Map<String, Instant> groupsApartSince = new HashMap<>();
        Map<String, Set<String>> groupNotificationsSent = new HashMap<>();
        // --- Maps for Chaperone Geofence ---
        Map<Integer, Instant> studentsOutsideChaperoneSince = new HashMap<>();
        Map<Integer, Set<String>> chaperoneNotificationsSent = new HashMap<>();


        // Initial Data Load
        System.out.println("Getting initial chaperone Data");
        eventController.updateChaperone(client);
        System.out.println("\nGetting initial students locations");
        eventController.updateStudentGroup(client);
        System.out.println("\nGetting initial students attending list");
        eventController.updateStudentAttandingList(client);
        System.out.println("\nChecking who isnt being tracked initially");
        eventController.checkWhoIsntBeingTracked();
        System.out.println("\nGetting initial GoIn2Groups");
        eventController.updateGoIn2Groups(client);


        System.out.println("\n--- Starting Tracking Loop ---");
        while(eventController.EventInformation.getBoolean("status")){
            Instant checkTime = Instant.now();

            try {
                System.out.println("\n[" + checkTime + "] Updating locations and checking status...");
                eventController.updateStudentGroup(client);
                eventController.updateChaperone(client);
                eventController.updateGoIn2Groups(client);

                if(!eventController.allStudentsTracked()){
                    eventController.checkWhoIsntBeingTracked();
                }

                // --- 1. Main Geofence Check ---
                boolean allInsideMainGeofence = eventController.checkGeofence();
                List<User> currentOutsideMainStudents = eventController.getStudentsOutsideFence();
                Set<Integer> currentOutsideMainIds = currentOutsideMainStudents.stream()
                        .map(User::getID)
                        .collect(Collectors.toSet());
                // Pass the configured thresholds to the helper method
                processGeofenceViolations(
                        currentOutsideMainStudents, currentOutsideMainIds,
                        studentsOutsideMainSince, mainNotificationsSent,
                        eventController, client, checkTime,
                        "main event area", "MAIN",
                        STUDENT_NOTIFICATION_THRESHOLD_MINUTES, CHAPERONE_NOTIFICATION_THRESHOLD_MINUTES // Pass thresholds
                );


                // --- 2. GoIn2Groups Check ---
                boolean allGroupsOk = eventController.checkGoIn2Groups();
                List<GoIn2Group> currentGroupsApart = eventController.getGoIn2GroupsOutsideFence();
                Set<String> currentGroupsApartKeys = currentGroupsApart.stream()
                        .map(Main::getGroupKey)
                        .collect(Collectors.toSet());
                // Pass the configured thresholds to the helper method
                processGroupViolations(
                        currentGroupsApart, currentGroupsApartKeys,
                        groupsApartSince, groupNotificationsSent,
                        eventController, client, checkTime,
                        STUDENT_NOTIFICATION_THRESHOLD_MINUTES, CHAPERONE_NOTIFICATION_THRESHOLD_MINUTES // Pass thresholds
                );

                // --- 3. Chaperone Geofence Check ---
                boolean allNearChaperone = eventController.checkChaperoneGeofence();
                List<User> currentOutsideChaperoneStudents = eventController.getStudentsOutsideChaperone();
                Set<Integer> currentOutsideChaperoneIds = currentOutsideChaperoneStudents.stream()
                        .map(User::getID)
                        .collect(Collectors.toSet());
                // Pass the configured thresholds to the helper method
                processGeofenceViolations(
                        currentOutsideChaperoneStudents, currentOutsideChaperoneIds,
                        studentsOutsideChaperoneSince, chaperoneNotificationsSent,
                        eventController, client, checkTime,
                        "chaperone radius", "CHAPERONE",
                        STUDENT_NOTIFICATION_THRESHOLD_MINUTES, CHAPERONE_NOTIFICATION_THRESHOLD_MINUTES // Pass thresholds
                );

                // --- 4. Stale Location Check ---
                System.out.println("\nChecking for stale locations");
                eventController.checkStaleLocations();

                // --- Pause and Update Event Status ---
                pauseServer(SERVER_PAUSE_MILLISECONDS); // Use configured server pause
                eventController.updateEventInformation(client);

            } catch (Exception e) {
                System.err.println("[" + Instant.now() + "] ERROR during tracking loop: " + e.getMessage());
                e.printStackTrace();
                pauseServer(ERROR_PAUSE_MILLISECONDS); // Use configured error pause
            }

        } // End while loop

        System.out.println("\n--- Event Status is now FALSE. Stopping tracking. ---");
        client.close();

    } // End main method

    /**
     * Processes geofence violations for individual students (Main or Chaperone).
     * Now accepts threshold parameters.
     */
    private static void processGeofenceViolations(
            List<User> currentViolatingStudents, Set<Integer> currentViolatingIds,
            Map<Integer, Instant> violationSinceMap, Map<Integer, Set<String>> notificationSentMap,
            EventGeofenceController eventController, HttpClient client, Instant checkTime,
            String areaDescription, String logPrefix,
            long studentThresholdMinutes, long chaperoneThresholdMinutes) { // Added threshold parameters

        // 1. Process students currently violating
        for (User student : currentViolatingStudents) {
            int studentId = student.getID();
            violationSinceMap.putIfAbsent(studentId, checkTime);
            notificationSentMap.putIfAbsent(studentId, new HashSet<>());

            Instant timeFirstViolation = violationSinceMap.get(studentId);
            Duration durationViolation = Duration.between(timeFirstViolation, checkTime);

            String studentNotifKey = "student_" + studentThresholdMinutes + "min_" + logPrefix;
            String chaperoneNotifKey = "chaperone_" + chaperoneThresholdMinutes + "min_" + logPrefix;

            // Use the passed threshold parameters in the conditions
            if (durationViolation.toMinutes() >= studentThresholdMinutes && !notificationSentMap.get(studentId).contains(studentNotifKey)) {
                System.out.printf("ALERT: [%s] Student %s outside %s for > %d min. Notifying student.%n",
                        logPrefix, student.getFirstName(), areaDescription, studentThresholdMinutes);
                try {
                    String studentMessage = "You have been outside the " + areaDescription + " for over " + studentThresholdMinutes + " minute(s).";
                    APICalls.sendNotification(student, eventController.EventInformation.getInt("id"), studentMessage, client);
                    notificationSentMap.get(studentId).add(studentNotifKey);
                } catch (Exception e) {
                    System.err.printf("Failed to send student notification (%s - %d min) for student %d: %s%n", logPrefix, studentThresholdMinutes, studentId, e.getMessage());
                }
            }

            // Use the passed threshold parameters in the conditions
            if (durationViolation.toMinutes() >= chaperoneThresholdMinutes && !notificationSentMap.get(studentId).contains(chaperoneNotifKey)) {
                System.out.printf("ALERT: [%s] Student %s outside %s for > %d min. Notifying chaperone.%n",
                        logPrefix, student.getFirstName(), areaDescription, chaperoneThresholdMinutes);
                try {
                    String chaperoneMessage = student.getFirstName() + " " + student.getLastName() + " has been outside the " + areaDescription + " for over " + chaperoneThresholdMinutes + " minute(s).";
                    APICalls.sendNotification(eventController.Chaperone, eventController.EventInformation.getInt("id"), chaperoneMessage, client);
                    notificationSentMap.get(studentId).add(chaperoneNotifKey);
                } catch (Exception e) {
                    System.err.printf("Failed to send chaperone notification (%s - %d min) for student %d: %s%n", logPrefix, chaperoneThresholdMinutes, studentId, e.getMessage());
                }
            }
        }

        // 2. Process students who were violating but are now back within limits
        Set<Integer> previouslyViolatingIds = new HashSet<>(violationSinceMap.keySet());
        for (int studentId : previouslyViolatingIds) {
            if (!currentViolatingIds.contains(studentId)) {
                System.out.printf("INFO: [%s] Student ID %d is back within the %s.%n", logPrefix, studentId, areaDescription);
                violationSinceMap.remove(studentId);
                notificationSentMap.remove(studentId);
            }
        }
    }

    /**
     * Processes GoIn2Group distance violations.
     * Now accepts threshold parameters.
     */
    private static void processGroupViolations(
            List<GoIn2Group> currentGroupsApart, Set<String> currentGroupsApartKeys,
            Map<String, Instant> groupsApartSinceMap, Map<String, Set<String>> groupNotificationSentMap,
            EventGeofenceController eventController, HttpClient client, Instant checkTime,
            long studentThresholdMinutes, long chaperoneThresholdMinutes) { // Added threshold parameters

        String logPrefix = "GROUP";

        // 1. Process groups currently too far apart
        for (GoIn2Group group : currentGroupsApart) {
            String groupKey = getGroupKey(group);
            if (groupKey == null) continue;

            groupsApartSinceMap.putIfAbsent(groupKey, checkTime);
            groupNotificationSentMap.putIfAbsent(groupKey, new HashSet<>());

            Instant timeFirstApart = groupsApartSinceMap.get(groupKey);
            Duration durationApart = Duration.between(timeFirstApart, checkTime);

            String studentNotifKey = "student_" + studentThresholdMinutes + "min_" + logPrefix;
            String chaperoneNotifKey = "chaperone_" + chaperoneThresholdMinutes + "min_" + logPrefix;

            // Use the passed threshold parameters in the conditions
            if (durationApart.toMinutes() >= studentThresholdMinutes && !groupNotificationSentMap.get(groupKey).contains(studentNotifKey)) {
                System.out.printf("ALERT: [%s] Group %s apart for > %d min. Notifying students.%n", logPrefix, groupKey, studentThresholdMinutes);
                try {
                    String studentMessage = "Your GoIn2 group partner is too far away (over " + studentThresholdMinutes + " min). Please regroup.";
                    if (group.Student1 != null) {
                        APICalls.sendNotification(group.Student1, eventController.EventInformation.getInt("id"), studentMessage, client);
                    }
                    if (group.Student2 != null) {
                        APICalls.sendNotification(group.Student2, eventController.EventInformation.getInt("id"), studentMessage, client);
                    }
                    groupNotificationSentMap.get(groupKey).add(studentNotifKey);
                } catch (Exception e) {
                    System.err.printf("Failed to send group student notification (%s - %d min) for group %s: %s%n", logPrefix, studentThresholdMinutes, groupKey, e.getMessage());
                }
            }

            // Use the passed threshold parameters in the conditions
            if (durationApart.toMinutes() >= chaperoneThresholdMinutes && !groupNotificationSentMap.get(groupKey).contains(chaperoneNotifKey)) {
                System.out.printf("ALERT: [%s] Group %s apart for > %d min. Notifying chaperone.%n", logPrefix, groupKey, chaperoneThresholdMinutes);
                try {
                    String student1Name = group.Student1 != null ? group.Student1.getFirstName() + " " + group.Student1.getLastName() : "ID:" + (group.Student1 != null ? group.Student1.getID() : "null");
                    String student2Name = group.Student2 != null ? group.Student2.getFirstName() + " " + group.Student2.getLastName() : "ID:" + (group.Student2 != null ? group.Student2.getID() : "null");
                    String chaperoneMessage = "GoIn2 Group (" + student1Name + " and " + student2Name + ") have been too far apart for over " + chaperoneThresholdMinutes + " minute(s).";
                    APICalls.sendNotification(eventController.Chaperone, eventController.EventInformation.getInt("id"), chaperoneMessage, client);
                    groupNotificationSentMap.get(groupKey).add(chaperoneNotifKey);
                } catch (Exception e) {
                    System.err.printf("Failed to send group chaperone notification (%s - %d min) for group %s: %s%n", logPrefix, chaperoneThresholdMinutes, groupKey, e.getMessage());
                }
            }
        }

        // 2. Process groups that were apart but are now back within limits
        Set<String> previouslyApartKeys = new HashSet<>(groupsApartSinceMap.keySet());
        for (String groupKey : previouslyApartKeys) {
            if (!currentGroupsApartKeys.contains(groupKey)) {
                System.out.printf("INFO: [%s] Group %s is back within distance limits.%n", logPrefix, groupKey);
                groupsApartSinceMap.remove(groupKey);
                groupNotificationSentMap.remove(groupKey);
            }
        }
    }


    /**
     * Creates a consistent key for a GoIn2Group based on sorted student IDs.
     * @param group The GoIn2Group.
     * @return A string key like "ID1-ID2" or null if students are missing.
     */
    private static String getGroupKey(GoIn2Group group) {
        if (group == null || group.Student1 == null || group.Student2 == null) {
            return null;
        }
        int id1 = group.Student1.getID();
        int id2 = group.Student2.getID();
        return (id1 < id2) ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    /**
     * Pauses the execution for a specified duration.
     * @param milliseconds The duration to pause in milliseconds.
     */
    public static void pauseServer(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.err.println("Server pause interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
} // End Main class