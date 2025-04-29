package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages geofences, student locations, and event status for a specific event.
 * Includes checks for main geofence, GoIn2 groups, chaperone proximity,
 * stale locations, and untracked students, sending notifications as needed.
 */
public class EventGeofenceController {
    public User Chaperone;
    public MainGeofence Geofence;
    public ChaperoneGeofence ChaperoneGeofence;
    public JSONObject EventInformation;
    public List<User> StudentGroup; // Students currently tracked with locations
    public List<User> StudentsInEvent; // All students registered for the event
    public List<GoIn2Group> GoIn2Groups;
    public List<User> StudentsOutsideFence;
    public List<GoIn2Group> GoIn2GroupsOutsideFence;
    public List<User> StudentsOutsideChaperone;

    private long stalenessThresholdSeconds = 300; // 5 minutes (Seconds)
    public float goIn2Distance;
    private HttpClient client; // HttpClient for making API calls

    // State tracking for notifications
    private Map<Integer, Instant> staleNotificationSentTime = new HashMap<>();
    private Set<Integer> untrackedNotificationSent = new HashSet<>();
    private static final Duration STALE_NOTIFICATION_COOLDOWN = Duration.ofMinutes(10); // Notify every 10 mins for ongoing staleness

    /**
     * Constructor for EventGeofenceController.
     * Initializes geofences and lists based on provided parameters.
     *
     * @param Chaperone             The chaperone user object.
     * @param EventCenterLatitude   Latitude of the main event geofence center.
     * @param EventCenterLongitude  Longitude of the main event geofence center.
     * @param EventRadiusMeters     Radius of the main event geofence in meters.
     * @param ChaperoneDistance     Radius of the chaperone geofence in meters.
     * @param GoIn2Distance         Maximum allowed distance between GoIn2 group partners in meters.
     * @param client                HttpClient instance for API calls.
     */
    public EventGeofenceController(User Chaperone, float EventCenterLatitude, float EventCenterLongitude, float EventRadiusMeters, float ChaperoneDistance, float GoIn2Distance, HttpClient client) {
        this.Chaperone = Chaperone;
        this.Geofence = new MainGeofence(EventCenterLatitude, EventCenterLongitude, EventRadiusMeters);
        this.ChaperoneGeofence = new ChaperoneGeofence(Chaperone, ChaperoneDistance);
        this.client = client; // Assign the client
        this.StudentGroup = new ArrayList<>();
        this.GoIn2Groups = new ArrayList<>();
        this.StudentsOutsideFence = new ArrayList<>();
        this.GoIn2GroupsOutsideFence = new ArrayList<>();
        this.StudentsOutsideChaperone = new ArrayList<>();
        this.StudentsInEvent = new ArrayList<>();
        this.goIn2Distance = GoIn2Distance;
    }

    /**
     * Sets the event information JSON object.
     * @param eventInformation JSONObject containing event details.
     */
    public void addEventData(JSONObject eventInformation){
        EventInformation = eventInformation;
    }

    /**
     * Sets the HttpClient instance.
     * @param client HttpClient instance.
     */
    public void setHttpClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Adds a single student to the tracked group (potentially unused).
     * @param Student The student user object to add.
     */
    public void addStudent(User Student) {
        this.StudentGroup.add(Student);
    }

    /**
     * Replaces the current student group with a new list (potentially unused).
     * @param group The new list of student user objects.
     */
    public void setStudentGroup(List<User> group) {
        this.StudentGroup.clear();
        this.StudentGroup.addAll(group);
    }

    /**
     * Checks if all students registered for the event are currently being tracked.
     * @return true if the number of tracked students equals the number of students in the event, false otherwise.
     */
    public boolean allStudentsTracked(){
        if (StudentsInEvent == null || StudentGroup == null) {
            return false; // Or handle as appropriate if lists aren't initialized
        }
        return StudentGroup.size() == StudentsInEvent.size();
    }

    /**
     * Updates the list of tracked students and their locations by fetching data from the API.
     * @param client HttpClient instance.
     * @throws RuntimeException if the API call fails.
     */
    public void updateStudentGroup(HttpClient client){
        this.client = client; // Ensure client is set
        StudentGroup = new ArrayList<>(); // Clear previous data
        String studentLocationEventIDEndpoint = "/api/MostRecentStudentLocationView/by-event/" + EventInformation.getInt("id");
        JSONArray studentListJson = null;
        try{
            studentListJson = APICalls.makeGetRequestMultiItem(client, studentLocationEventIDEndpoint);
        }catch (Exception e){
            System.err.println("Error fetching student locations: " + e.getMessage());
            throw new RuntimeException("Failed to update student group locations", e);
        }

        if(studentListJson != null){
            System.out.println("Student Locations Retrieved: " + studentListJson.length() + " records.");
            for (int i = 0; i < studentListJson.length(); i++){
                JSONObject s = studentListJson.getJSONObject(i);
                try {
                    User student = new User(s.getInt("studentId"), s.getString("firstName"),
                            s.getString("lastName"), s.getFloat("latitude"),s.getFloat("longitude"),s.getLong("timestampMs"));
                    StudentGroup.add(student);
                } catch (Exception e) {
                    System.err.println("Error parsing student record: " + s.toString() + " - " + e.getMessage());
                    // Decide whether to continue or throw
                }
            }
            if (StudentGroup.size() != studentListJson.length()) {
                System.err.println("Warning: Mismatch between parsed students and received JSON records in updateStudentGroup.");
            }
        }
        else{
            System.err.println("API call for student locations returned null or failed.");
            // Potentially throw an error or handle gracefully
            // System.exit(76); // Avoid System.exit in library code if possible
        }

    }

    /**
     * Updates the list of all students registered for the event by fetching data from the API.
     * @param client HttpClient instance.
     * @throws RuntimeException if the API call fails.
     */
    public void updateStudentAttandingList(HttpClient client){
        this.client = client; // Ensure client is set
        StudentsInEvent = new ArrayList<>(); // Clear previous data
        String studentsOnTripEndpoint = "/api/StudentsInEventsView/" + EventInformation.getInt("id");
        JSONArray studentsOnTripJson = null;
        try{
            studentsOnTripJson = APICalls.makeGetRequestMultiItem(client, studentsOnTripEndpoint);
        } catch (Exception e){
            System.err.println("Error fetching students attending list: " + e.getMessage());
            throw new RuntimeException("Failed to update student attending list", e);
        }
        if(studentsOnTripJson != null){
            System.out.println("Retrieved all Students on trip: " + studentsOnTripJson.length() + " students.");
            for(int i = 0; i < studentsOnTripJson.length(); i++){
                JSONObject s = studentsOnTripJson.getJSONObject(i);
                try {
                    // Create User object, potentially with default location/timestamp as it's just the list of attendees
                    User student = new User(s.getInt("studentId"),s.getString("firstName"),s.getString("lastName"),0,0,0);
                    StudentsInEvent.add(student);
                } catch (Exception e) {
                    System.err.println("Error parsing student attending record: " + s.toString() + " - " + e.getMessage());
                    // Decide whether to continue or throw
                }
            }
            if (StudentsInEvent.size() != studentsOnTripJson.length()) {
                System.err.println("Warning: Mismatch between parsed students and received JSON records in updateStudentAttandingList.");
            }
        }
        else{
            System.err.println("API call for students attending list returned null or failed.");
            // Potentially throw an error or handle gracefully
            // System.exit(7); // Avoid System.exit
        }
    }

    /**
     * Checks which students registered for the event are not currently being tracked (i.e., no recent location data).
     * Sends a notification to the chaperone for each newly detected untracked student.
     */
    public void checkWhoIsntBeingTracked(){
        if (StudentGroup == null || StudentsInEvent == null || EventInformation == null || Chaperone == null || client == null) {
            System.err.println("Cannot check for untracked students: Required objects (StudentGroup, StudentsInEvent, EventInformation, Chaperone, client) not initialized.");
            return;
        }


        Set<Integer> trackedStudentIds = StudentGroup.stream()
                .map(User::getID)
                .collect(Collectors.toSet());


        Set<Integer> allTripStudentIds = StudentsInEvent.stream()
                .map(User::getID)
                .collect(Collectors.toSet());


        // Find students on the trip whose IDs are NOT in the trackedStudentIds set
        Set<Integer> notTrackedIds = new HashSet<>(allTripStudentIds);
        notTrackedIds.removeAll(trackedStudentIds);


        if (!notTrackedIds.isEmpty()) {
            System.out.println("\nStudents whose location has not been tracked yet (IDs): " + notTrackedIds);


            List<User> notTrackedStudents = StudentsInEvent.stream()
                    .filter(student -> notTrackedIds.contains(student.getID()))
                    .collect(Collectors.toList());


            if (!notTrackedStudents.isEmpty()) {
                System.out.println("Details of students not tracked:");
                for (User student : notTrackedStudents) {
                    System.out.println("  ID: " + student.getID() + ", Name: " + student.getFirstName() + " " + student.getLastName());


                    // --- Notification Logic ---
                    // Only send notification if it hasn't been sent for this student ID yet during this run
                    if (!untrackedNotificationSent.contains(student.getID())) {
                        try {
                            String message = student.getFirstName() + " " + student.getLastName() + " is registered for the event but is not currently being tracked.";
                            System.out.println("Attempting to send untracked student notification for student ID: " + student.getID());
                            // Ensure Chaperone and EventInformation are not null and client is set
                            APICalls.sendNotification(this.Chaperone, this.EventInformation.getInt("id"), message, this.client); // Use the client field
                            untrackedNotificationSent.add(student.getID()); // Mark notification as sent for this session
                        } catch (Exception e) {
                            System.err.println("Failed to send untracked student notification for student " + student.getID() + ": " + e.getMessage());
                        }
                    }
                    // --- End Notification Logic ---
                }
            }
        } else {
            System.out.println("\nAll students on the trip have had their location tracked.");
        }


        // Clean up notification set for students who are now tracked (in case they start sending data)
        untrackedNotificationSent.removeIf(trackedStudentIds::contains);
    }

    /**
     * Updates the chaperone's current location by fetching data from the API.
     * @param client HttpClient instance.
     * @throws RuntimeException if the API call fails or chaperone data is not found.
     */
    public void updateChaperone(HttpClient client){
        this.client = client; // Ensure client is set
        if (Chaperone == null) {
            System.err.println("Cannot update chaperone location: Chaperone object is null.");
            return;
        }
        int chaperoneID = Chaperone.getID();
        String chaperoneAccess = "/api/Location/latest/" + chaperoneID;
        JSONObject chaperoneLocationJson;
        try{
            chaperoneLocationJson = APICalls.makeGetRequestSingleItem(client, chaperoneAccess);
        } catch(Exception e){
            System.err.println("Error fetching chaperone location: " + e.getMessage());
            throw new RuntimeException("Failed to update chaperone location", e);
        }


        if(chaperoneLocationJson != null){
            System.out.println("Chaperone Location Retrieved");
            try {
                Chaperone.Latitude = chaperoneLocationJson.getFloat("latitude");
                Chaperone.Longitude = chaperoneLocationJson.getFloat("longitude");
                // Optionally update chaperone timestamp if available/needed
                // Chaperone.timestamp = Instant.ofEpochMilli(chaperoneLocationJson.getLong("timestampMs"));
            } catch (Exception e) {
                System.err.println("Error parsing chaperone location data: " + chaperoneLocationJson.toString() + " - " + e.getMessage());
                throw new RuntimeException("Failed to parse chaperone location data", e);
            }
        }
        else{
            System.err.println("API call for chaperone location returned null or failed for ID: " + chaperoneID);
            // Consider how to handle this - maybe keep old location? Throw error?
            // System.exit(908); // Avoid System.exit
            throw new RuntimeException("Chaperone location data not found or API call failed.");
        }
    }

    /**
     * Updates the list of active GoIn2 groups for the event by fetching data from the API.
     * @param client HttpClient instance.
     * @throws RuntimeException if the API call fails.
     */
    public void updateGoIn2Groups(HttpClient client){
        this.client = client; // Ensure client is set
        GoIn2Groups = new ArrayList<>(); // Clear previous data
        String goIn2GroupEndpoint = "/api/Pair/Event/" + EventInformation.getInt("id") + "/Active";
        JSONArray goIn2GroupsJson = null;
        try{
            goIn2GroupsJson = APICalls.makeGetRequestMultiItem(client, goIn2GroupEndpoint);
        }catch (Exception e){
            System.err.println("Error fetching GoIn2 groups: " + e.getMessage());
            // If groups are optional, maybe just log and continue?
            // throw new RuntimeException("Failed to update GoIn2 groups", e);
            return; // Exit method if groups fail to load but it's not critical
        }
        if(goIn2GroupsJson != null){
            System.out.println("Retrieved active GoIn2 Groups: " + goIn2GroupsJson.length() + " groups.");
            for(int i = 0; i < goIn2GroupsJson.length(); i++){
                JSONObject s = goIn2GroupsJson.getJSONObject(i);
                try {
                    User student1 = findStudentById(s.getInt("student1id"));
                    User student2 = findStudentById(s.getInt("student2id"));

                    // Important: Only create group if both students were found in the *currently tracked* StudentGroup
                    if (student1 != null && student2 != null) {
                        GoIn2Group g = new GoIn2Group(student1, student2, goIn2Distance);
                        GoIn2Groups.add(g);
                    } else {
                        System.err.println("Warning: Could not create GoIn2 Group from JSON " + s.toString() + ". One or both students not found in current StudentGroup (tracked locations). Student1 found: " + (student1 != null) + ", Student2 found: " + (student2 != null));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing GoIn2 group record: " + s.toString() + " - " + e.getMessage());
                    // Decide whether to continue or throw
                }
            }
        }
        else{
            System.out.println("No active Go In 2 groups found or API call failed.");
        }
    }

    /**
     * Checks all tracked students against the main event geofence.
     * Populates the StudentsOutsideFence list.
     * @return true if all students are within the geofence, false otherwise.
     */
    public boolean checkGeofence() {
        if (Geofence == null || StudentGroup == null) {
            System.err.println("Cannot check main geofence: Geofence or StudentGroup not initialized.");
            return true; // Assume ok if cannot check? Or return false?
        }
        boolean allInside = true;
        StudentsOutsideFence.clear(); // Clear before checking
        for (User student : StudentGroup) {
            if (!Geofence.WithinGeofence(student)) {
                allInside = false;
                StudentsOutsideFence.add(student);
            }
        }
        if (!allInside) {
            System.out.println("Students found outside main geofence: " + StudentsOutsideFence.size());
        }
        return allInside;
    }

    /**
     * Returns the list of students currently identified as being outside the main event geofence.
     * @return List of User objects.
     */
    public List<User> getStudentsOutsideFence() {
        return StudentsOutsideFence == null ? Collections.emptyList() : StudentsOutsideFence;
    }

    /**
     * Checks all active GoIn2 groups to see if partners are within the allowed distance.
     * Populates the GoIn2GroupsOutsideFence list.
     * @return true if all group partners are within the allowed distance, false otherwise.
     */
    public boolean checkGoIn2Groups() {
        if (GoIn2Groups == null) {
            System.err.println("Cannot check GoIn2 groups: GoIn2Groups list not initialized.");
            return true; // Assume ok if no groups?
        }
        boolean allOk = true;
        GoIn2GroupsOutsideFence.clear();
        for (GoIn2Group group : GoIn2Groups) {
            // Ensure group members are not null before checking
            if (group.Student1 != null && group.Student2 != null) {
                if (!group.checkGroup()) {
                    allOk = false;
                    GoIn2GroupsOutsideFence.add(group);
                }
            } else {
                // This case should be minimized by the check in updateGoIn2Groups
                System.err.println("Warning: Skipping distance check for GoIn2 group due to missing student data: " + group);
            }
        }
        if (!allOk) {
            System.out.println("GoIn2 groups found outside distance limit: " + GoIn2GroupsOutsideFence.size());
        }
        return allOk;
    }

    /**
     * Returns the list of GoIn2 groups currently identified as being too far apart.
     * @return List of GoIn2Group objects.
     */
    public List<GoIn2Group> getGoIn2GroupsOutsideFence() {
        return GoIn2GroupsOutsideFence == null ? Collections.emptyList() : GoIn2GroupsOutsideFence;
    }

    /**
     * Checks students who are NOT in GoIn2 groups against the chaperone geofence.
     * Populates the StudentsOutsideChaperone list.
     * @return true if all non-group students are within the chaperone radius, false otherwise.
     */
    public boolean checkChaperoneGeofence() {
        if (ChaperoneGeofence == null || StudentGroup == null || GoIn2Groups == null) {
            System.err.println("Cannot check chaperone geofence: ChaperoneGeofence, StudentGroup, or GoIn2Groups not initialized.");
            return true; // Assume ok?
        }
        boolean allNear = true;
        StudentsOutsideChaperone.clear();

        // Create a set of all student IDs currently in an active GoIn2Group
        Set<Integer> studentsInActiveGroups = new HashSet<>();
        for (GoIn2Group group : GoIn2Groups) {
            // Ensure group members are not null before adding IDs
            if (group.Student1 != null) {
                studentsInActiveGroups.add(group.Student1.ID);
            }
            if (group.Student2 != null) {
                studentsInActiveGroups.add(group.Student2.ID);
            }
        }

        for (User student : StudentGroup) {
            // Only check the chaperone geofence if the student is NOT in an active GoIn2Group
            if (!studentsInActiveGroups.contains(student.getID())) {
                if (!ChaperoneGeofence.WithinChaperoneGeofence(student)) {
                    allNear = false;
                    StudentsOutsideChaperone.add(student);
                }
            }
        }
        if (!allNear) {
            System.out.println("Students (not in groups) found outside chaperone radius: " + StudentsOutsideChaperone.size());
        }
        return allNear;
    }

    /**
     * Returns the list of students (not in GoIn2 groups) currently identified as being outside the chaperone geofence.
     * @return List of User objects.
     */
    public List<User> getStudentsOutsideChaperone() {
        return StudentsOutsideChaperone == null ? Collections.emptyList() : StudentsOutsideChaperone;
    }

    /**
     * Updates the event information (e.g., status) by fetching data from the API.
     * @param client HttpClient instance.
     * @throws RuntimeException if the API call fails or event data is not found.
     */
    public void updateEventInformation(HttpClient client){
        this.client = client; // Ensure client is set
        if (EventInformation == null) {
            System.err.println("Cannot update event information: EventInformation object is null.");
            throw new RuntimeException("Cannot update event information: EventInformation object is null.");
        }
        String EventAccess = "/api/Event/" + EventInformation.getInt("id");
        JSONObject updatedEventJson;
        try {
            updatedEventJson = APICalls.makeGetRequestSingleItem(client, EventAccess);
        } catch (Exception e) {
            System.err.println("Error fetching updated event information: " + e.getMessage());
            throw new RuntimeException("Failed to update event information", e);
        }

        if (updatedEventJson != null){
            EventInformation = updatedEventJson; // Replace the old JSON with the updated one
            System.out.println("Event Information Updated. Status: " + EventInformation.optBoolean("status", false));
        }
        else{
            System.err.println("API call for event information returned null or failed for Event ID: " + EventInformation.getInt("id"));
            // Decide how to handle - critical failure?
            // System.exit(420); // Avoid System.exit
            throw new RuntimeException("Event information could not be updated.");
        }
    }

    /**
     * Checks all tracked students for stale location data (data older than stalenessThresholdSeconds).
     * Sends a notification to the chaperone for students whose data becomes stale or remains stale after a cooldown period.
     */
    public void checkStaleLocations() {
        if (StudentGroup == null || EventInformation == null || Chaperone == null || client == null) {
            System.err.println("Cannot check for stale locations: Required objects (StudentGroup, EventInformation, Chaperone, client) not initialized.");
            return;
        }


        Instant currentTime = Instant.now();
        List<User> currentStaleStudents = new ArrayList<>();


        for (User student : StudentGroup) {
            Instant studentTimestamp = student.timestamp; // Assumes User class has public Instant timestamp field
            if (studentTimestamp != null) {
                Duration durationSinceLastUpdate = Duration.between(studentTimestamp, currentTime);
                if (durationSinceLastUpdate.getSeconds() > stalenessThresholdSeconds) {
                    currentStaleStudents.add(student);
                }
            } else {
                // Handle cases where a student might not have a timestamp yet (e.g., first time seen)
                // Decide if this counts as "stale" immediately. Assuming yes for now.
                System.out.println("Warning: Student ID " + student.getID() + " has no timestamp. Considered stale.");
                currentStaleStudents.add(student);
            }
        }


        if (!currentStaleStudents.isEmpty()) {
            System.out.println("\nStudents with stale locations (data older than " + stalenessThresholdSeconds + " seconds):");
            for (User student : currentStaleStudents) {
                System.out.println("  ID: " + student.getID() + ", Name: " + student.getFirstName() + " " + student.getLastName() + ", Last Update: " + (student.timestamp != null ? student.timestamp.toString() : "None"));


                // --- Notification Logic Added ---
                Instant lastNotified = staleNotificationSentTime.get(student.getID());
                // Send notification if never sent before OR if cooldown period has passed
                if (lastNotified == null || Duration.between(lastNotified, currentTime).compareTo(STALE_NOTIFICATION_COOLDOWN) > 0) {
                    try {
                        String message = student.getFirstName() + " " + student.getLastName() + "'s location data is stale (older than " + stalenessThresholdSeconds / 60 + " minutes).";
                        System.out.println("Attempting to send stale location notification for student ID: " + student.getID());
                        APICalls.sendNotification(this.Chaperone, this.EventInformation.getInt("id"), message, this.client);
                        staleNotificationSentTime.put(student.getID(), currentTime); // Update last notification time
                    } catch (Exception e) {
                        System.err.println("Failed to send stale location notification for student " + student.getID() + ": " + e.getMessage());
                        // Decide if you want to update staleNotificationSentTime even if sending failed, to prevent immediate retries
                        // staleNotificationSentTime.put(student.getID(), currentTime); // Uncomment to prevent rapid retries on failure
                    }
                } else {
                    // Optional: Log that notification is skipped due to cooldown
                    // System.out.println("Stale notification for student ID " + student.getID() + " skipped due to cooldown.");
                }
                // --- End Notification Logic ---
            }
        } else {
            System.out.println("\nAll tracked student locations are up-to-date based on their data timestamps.");
        }


        // Clean up notification map for students who are no longer stale
        Set<Integer> currentStaleIds = currentStaleStudents.stream().map(User::getID).collect(Collectors.toSet());
        staleNotificationSentTime.keySet().removeIf(id -> !currentStaleIds.contains(id));
    }


    /**
     * Factory method to create an EventGeofenceController instance using an Event ID.
     * Fetches necessary event, geofence, and chaperone details via API calls.
     *
     * @param EventID The ID of the event.
     * @param client  HttpClient instance for making API calls.
     * @return A fully initialized EventGeofenceController instance.
     * @throws RuntimeException if any critical API call fails or required data is missing.
     */
    public static EventGeofenceController CreateGeoFenceController (int EventID, HttpClient client){
        JSONObject Event;
        JSONObject geofence;
        JSONObject chaperoneJson;
        User Chaperone;

        // --- 1. Get Event information ---
        String EventAccess = "/api/Event/" + EventID;
        try {
            Event = APICalls.makeGetRequestSingleItem(client, EventAccess);
            if (Event == null) {
                System.err.println("Failed to retrieve Event details for Event ID: " + EventID);
                throw new RuntimeException("Event details not found or API call failed for Event ID: " + EventID);
            }
            System.out.println("Event Successfully Retrieved");
        } catch (Exception e) {
            System.err.println("Error fetching Event details: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve Event details", e);
        }

        // --- 2. Get Event Geofence ---
        int GeofenceID = Event.getInt("geofenceid"); // Ensure 'geofenceid' key exists
        String GeofenceAccess = "/api/GeoFence/" + GeofenceID;
        try {
            geofence = APICalls.makeGetRequestSingleItem(client, GeofenceAccess);
            if (geofence == null) {
                System.err.println("Failed to retrieve Geofence details for Geofence ID: " + GeofenceID);
                throw new RuntimeException("Geofence details not found or API call failed for Geofence ID: " + GeofenceID);
            }
            System.out.println("Geofence Successfully Retrieved");
        } catch (Exception e) {
            System.err.println("Error fetching Geofence details: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve Geofence details", e);
        }


        // --- 3. Get Chaperone Data ---
        int chaperoneID = Event.getInt("teacherid"); // Ensure 'teacherid' key exists
        String chaperoneAccess = "/api/User/" + chaperoneID;
        try{
            chaperoneJson = APICalls.makeGetRequestSingleItem(client, chaperoneAccess);
            if (chaperoneJson == null) {
                System.err.println("Failed to retrieve Chaperone details for User ID: " + chaperoneID);
                throw new RuntimeException("Chaperone details not found or API call failed for User ID: " + chaperoneID);
            }
            // Create Chaperone User object - assuming 0 for location initially, will be updated later
            Chaperone = new User(chaperoneJson.getInt("id"), chaperoneJson.getString("firstName"), chaperoneJson.getString("lastName"), 0, 0, 0);
            System.out.println("Chaperone Information Retrieved");
        } catch(Exception e){
            System.err.println("Error fetching Chaperone details: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve Chaperone details", e);
        }

        // --- 4. Create and Return Controller ---
        try {
            EventGeofenceController controller = new EventGeofenceController(
                    Chaperone,
                    geofence.getFloat("latitude"),       // Ensure these keys exist
                    geofence.getFloat("longitude"),
                    geofence.getFloat("eventRadius"),    // Assuming this is meters
                    geofence.getFloat("teacherRadius"),  // Assuming this is meters
                    geofence.getFloat("pairDistance"),   // Assuming this is meters
                    client                               // Pass the client
            );
            controller.addEventData(Event);
            System.out.println("EventGeofenceController created successfully.");
            return controller;
        } catch (Exception e) {
            System.err.println("Error creating EventGeofenceController from fetched data: " + e.getMessage());
            throw new RuntimeException("Failed to instantiate EventGeofenceController", e);
        }
    }

    /**
     * Finds a student in the *currently tracked* StudentGroup list by their ID.
     * Used primarily for linking students in GoIn2 groups.
     * @param studentId The ID of the student to find.
     * @return The User object if found in the current StudentGroup, otherwise null.
     */
    public User findStudentById(int studentId) {
        if (StudentGroup == null) {
            return null;
        }
        // Iterate through the list of students currently being tracked
        for (User student : StudentGroup) {
            if (student.getID() == studentId) {
                return student;
            }
        }
        // Student not found in the list of currently tracked locations
        return null;
    }


    /**
     * Provides a string representation of the event details managed by this controller.
     * @return Formatted string with event name, date, and location.
     */
    @Override
    public String toString(){
        if (EventInformation == null) {
            return "EventGeofenceController (Event details not loaded)";
        }
        // Use optString to avoid errors if keys are missing
        return "---------\n Event Name: " + EventInformation.optString("eventName", "N/A") +
                "\nEvent Date: " + EventInformation.optString("eventDate", "N/A") +
                "\nEvent Location: " + EventInformation.optString("eventLocation", "N/A") +
                "\n---------";
    }
}