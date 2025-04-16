package org.example;

public class GoIn2GroupGeofence extends Geofence {
    public User student1;
    public User student2;
    public float maxDistance;

    public GoIn2GroupGeofence(User student1, User student2, float maxDistance) {
        this.student1 = student1;
        this.student2 = student2;
        this.maxDistance = maxDistance;
    }

    public boolean DistanceCheck(){
        return calculateDistance(student1.Latitude, student1.Longitude, student2.Latitude, student2.Longitude) < maxDistance;
    }
}
