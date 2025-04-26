package org.example;

public class GoIn2Group {
    public User Student1;
    public User Student2;
    public GoIn2GroupGeofence Geofence;

    public GoIn2Group(User Student1, User Student2, float maxDistance) {
        this.Student1 = Student1;
        this.Student2 = Student2;
        Geofence = new GoIn2GroupGeofence(Student1, Student2, maxDistance);
    }

    public boolean checkGroup(){
        return Geofence.DistanceCheck();
    }

    @Override
    public String toString() {
        return "GoIn2Group{" +
                "Student2=" + Student2 +
                ", Student1=" + Student1 +
                '}';
    }
}
