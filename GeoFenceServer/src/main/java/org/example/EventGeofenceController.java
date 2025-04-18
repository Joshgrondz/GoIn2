package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;

//Remove syncronized from methods

public class EventGeofenceController {
    public User Chaperone;
    public MainGeofence Geofence;
    public ChaperoneGeofence ChaperoneGeofence;
    public JSONObject EventInformation;
    public List<User> StudentGroup;
    public List<User> StudentsInEvent;
    public List<GoIn2Group> GoIn2Groups;
    public List<User> StudentsOutsideFence;
    public List<GoIn2Group> GoIn2GroupsOutsideFence;
    public List<User> StudentsOutsideChaperone;

    // Establish Event's Geofence
    public EventGeofenceController(User Chaperone, float EventCenterLatitude, float EventCenterLongitude, float EventRadiusFeet, float ChaperoneDistance, float GoIn2Distance) {
        this.Chaperone = Chaperone;
        Geofence = new MainGeofence(EventCenterLatitude, EventCenterLongitude, EventRadiusFeet);
        ChaperoneGeofence = new ChaperoneGeofence(Chaperone, ChaperoneDistance);
        this.StudentGroup = new ArrayList<User>();
        this.GoIn2Groups = new ArrayList<GoIn2Group>();
        this.StudentsOutsideFence = new ArrayList<User>();
        this.GoIn2GroupsOutsideFence = new ArrayList<GoIn2Group>();
        this.StudentsOutsideChaperone = new ArrayList<User>();
        this.StudentsInEvent = new ArrayList<User>();
    }

    public void addEventData(JSONObject eventInformation){
        EventInformation = eventInformation;
    }

    // Add a singular student (Not sure if needed )
    public void addStudent(User Student) {
        this.StudentGroup.add(Student);
    }

    // Add/update studentGroup list (Probably use it for updating locations)
    public void setStudentGroup(List<User> group) {
        this.StudentGroup.clear();
        this.StudentGroup.addAll(group);
    }

    public boolean allStudentsTracked(){
        return StudentGroup.size() == StudentsInEvent.size();
    }

    public void updateStudentGroup(HttpClient client){
        String studentLocationEventIDEndpoint = "/api/MostRecentStudentLocationsView/" + EventInformation.getInt("id");
        JSONArray studentList = null;
        try{
            studentList = APICalls.makeGetRequestMultiItem(client, studentLocationEventIDEndpoint);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        if(studentList != null){
            System.out.println("Student Locations Retrieved");
        }
        else{
            System.exit(76);
        }

        for (int i = 0; i < studentList.length(); i++){
            JSONObject s = studentList.getJSONObject(i);

            User student = new User(s.getInt("studentId"), s.getString("firstName"),
                    s.getString("lastName"), s.getFloat("latitude"),s.getFloat("longitude"));
            StudentGroup.add(student);
        }

        if(StudentGroup.size() != studentList.length()){
            System.exit(5);
        }
    }

    public void updateStudentAttandingList(HttpClient client){
        String studentsOnTripEndpoint = "/api/StudentsInEventsView/" + EventInformation.getInt("id");
        JSONArray studentsOnTripJson = null;
        try{
            studentsOnTripJson = APICalls.makeGetRequestMultiItem(client, studentsOnTripEndpoint);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        if(studentsOnTripJson != null){
            System.out.println("Retrieved all Students on trip");
        }
        else{
            System.exit(7);
        }

        for(int i = 0; i < studentsOnTripJson.length(); i++){
            JSONObject s = studentsOnTripJson.getJSONObject(i);

            User student = new User(s.getInt("studentId"),s.getString("fistName"),s.getString("s.lastName"),0,0);
            StudentsInEvent.add(student);
        }

        if(StudentsInEvent.size() != studentsOnTripJson.length()){
            System.exit(5);
        }
    }

    public void checkWhoIsntBeingTracked(){
        Set<Integer> trackedStudentIds = StudentGroup.stream()
                .map(User::getID)
                .collect(Collectors.toSet());

        //Set of total IDS being tracked
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

            //Print for now, API call for notification next time
            if (!notTrackedStudents.isEmpty()) {
                System.out.println("Details of students not tracked:");
                notTrackedStudents.forEach(student -> System.out.println("  ID: " + student.getID() + ", Name: " + student.getFirstName() + " " + student.getLastName()));
            }
        } else {
            System.out.println("\nAll students on the trip have had their location tracked.");
        }
    }

    public void updateChaperone(User newChaperone){
        Chaperone = newChaperone;
        ChaperoneGeofence.updateChaperone(Chaperone);
    }

    // Check Main Geofence
    public boolean checkGeofence() {
        boolean flag = true;
        StudentsOutsideFence.clear(); // Clear before checking
        for (User student : StudentGroup) {
            if (!Geofence.WithinGeofence(student)) {
                flag = false;
                StudentsOutsideFence.add(student);
            }
        }
        return flag;
    }

    // Return Users/Students who are outside geofence
    public List<User> getStudentsOutsideFence() {
        return StudentsOutsideFence;
    }

    // Check all GoIn2 groups and see if they are too far apart
    public boolean checkGoIn2Groups() {
        boolean flag = true;
        GoIn2GroupsOutsideFence.clear();
        for (GoIn2Group group : GoIn2Groups) {
            if (!group.checkGroup()) {
                flag = false;
                GoIn2GroupsOutsideFence.add(group);
            }
        }
        return flag;
    }

    // Return GoIn2Groups who are too far apart
    public List<GoIn2Group> getGoIn2GroupsOutsideFence() {
        return GoIn2GroupsOutsideFence;
    }

    public boolean checkChaperoneGeofence() {
        boolean flag = true;
        StudentsOutsideChaperone.clear();
        for (User student : StudentGroup) {
            if (!ChaperoneGeofence.WithinChaperoneGeofence(student)) {
                flag = false;
                StudentsOutsideChaperone.add(student);
            }
        }
        return flag;
    }

    public List<User> getStudentsOutsideChaperone() {
        return StudentsOutsideChaperone;
    }

    public void updateEventInformation(HttpClient client){
        String EventAccess = "/api/Event/" + EventInformation.getInt("id");
        JSONObject Event;
        try {
            Event = APICalls.makeGetRequestSingleItem(client, EventAccess);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (Event != null){
            EventInformation = Event;
            System.out.println("Updated");
        }
        else{
            System.exit(420);
        }
    }

    //static method to create a geofenceController with just EventID
    //uses api calls to database to be able to gather needed information
    public static EventGeofenceController CreateGeoFenceController (int EventID, HttpClient client){
        //Get Event information
        String EventAccess = "/api/Event/" + EventID;
        JSONObject Event;
        try {
            Event = APICalls.makeGetRequestSingleItem(client, EventAccess);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (Event != null){
            System.out.println("Event Successfully Retrieved");
        }
        else{
            System.exit(420);
        }

        //Get Event Geofence
        int GeofenceID = Event.getInt("geofenceid");
        String GeofenceAccess = "/api/GeoFence/" + GeofenceID;
        JSONObject geofence;
        try {
            geofence = APICalls.makeGetRequestSingleItem(client, GeofenceAccess);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (geofence != null){
            System.out.println("Geofence Successfully Retrieved");
        }
        else{
            System.exit(69);
        }


        //Get Chaperone Data
        int chaperoneID = Event.getInt("teacherid");
        String chaperoneAccess = "/api/User/" + chaperoneID;
        JSONObject chaperone;
        try{
            chaperone = APICalls.makeGetRequestSingleItem(client, chaperoneAccess);
        } catch(Exception e){
            throw new RuntimeException(e);
        }

        if(chaperone != null){
            System.out.println("Chaperone Information Retrieved");
        }
        else{
            System.exit(908);
        }
        User Chaperone = new User(chaperone.getInt("id"), chaperone.getString("firstName"),chaperone.getString("lastName"),0,0);

        //Create Geofence Controller and return
        EventGeofenceController controller = new EventGeofenceController(Chaperone, geofence.getFloat("latitude"), geofence.getFloat("longitude"), geofence.getFloat("eventRadius"), geofence.getFloat("teacherRadius"),geofence.getFloat("pairDistance"));
        controller.addEventData(Event);
        return controller;

    }
}