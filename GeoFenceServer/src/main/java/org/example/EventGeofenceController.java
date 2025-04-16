package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventGeofenceController {
    public User Chaperone;
    public MainGeofence Geofence;
    public ChaperoneGeofence ChaperoneGeofence;
    // Make these thread-safe!
    public List<User> StudentGroup;
    public List<GoIn2Group> GoIn2Groups;
    public List<User> StudentsOutsideFence;
    public List<GoIn2Group> GoIn2GroupsOutsideFence;
    public List<User> StudentsOutsideChaperone;

    // Establish Event's Geofence
    public EventGeofenceController(User Chaperone, Float EventCenterLatitude, Float EventCenterLongitude, Float EventRadiusFeet, float ChaperoneDistance, float GoIn2Distance) {
        this.Chaperone = Chaperone;
        Geofence = new MainGeofence(EventCenterLatitude, EventCenterLongitude, EventRadiusFeet);
        ChaperoneGeofence = new ChaperoneGeofence(Chaperone, ChaperoneDistance);
        this.StudentGroup = Collections.synchronizedList(new ArrayList<User>());
        this.GoIn2Groups = Collections.synchronizedList(new ArrayList<GoIn2Group>());
        this.StudentsOutsideFence = Collections.synchronizedList(new ArrayList<User>());
        this.GoIn2GroupsOutsideFence = Collections.synchronizedList(new ArrayList<GoIn2Group>());
        this.StudentsOutsideChaperone = Collections.synchronizedList(new ArrayList<User>());
    }

    // Add a singular student (Not sure if needed )
    public void addStudent(User Student) {
        synchronized (StudentGroup) { // Explicit synchronization
            this.StudentGroup.add(Student);
        }
    }

    // Add/update studentGroup list (Probably use it for updating locations)
    public void setStudentGroup(List<User> group) {
        synchronized (StudentGroup) {
            this.StudentGroup.clear();
            this.StudentGroup.addAll(group);
        }
    }

    public void updateChaperone(User newChaperone){
        synchronized (Chaperone) {
            Chaperone = newChaperone;
            ChaperoneGeofence.updateChaperone(Chaperone);
        }
    }

    // Check Main Geofence
    public boolean checkGeofence() {
        boolean flag = true;
        synchronized (StudentGroup) { // Iterate safely
            StudentsOutsideFence.clear(); // Clear before checking
            for (User student : StudentGroup) {
                if (!Geofence.WithinGeofence(student)) {
                    flag = false;
                    StudentsOutsideFence.add(student);
                }
            }
        }
        return flag;
    }

    // Return Users/Students who are outside geofence
    public List<User> getStudentsOutsideFence() {
        return StudentsOutsideFence; // Returning the list is okay, just don't modify it outside
    }

    // Check all GoIn2 groups and see if they are too far apart
    public boolean checkGoIn2Groups() {
        boolean flag = true;
        synchronized (GoIn2Groups) {
            GoIn2GroupsOutsideFence.clear();
            for (GoIn2Group group : GoIn2Groups) {
                if (!group.checkGroup()) {
                    flag = false;
                    GoIn2GroupsOutsideFence.add(group);
                }
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
        synchronized (StudentGroup) {
            StudentsOutsideChaperone.clear();
            for (User student : StudentGroup) {
                if (!ChaperoneGeofence.WithinChaperoneGeofence(student)) {
                    flag = false;
                    StudentsOutsideChaperone.add(student);
                }
            }
        }
        return flag;
    }

    public List<User> getStudentsOutsideChaperone() {
        return StudentsOutsideChaperone;
    }
}