package org.example;

public class ChaperoneGeofence extends Geofence {
    public User Chaperone;
    public float RadiusFeet;

    public ChaperoneGeofence(User Chaperone, float RadiusFeet ) {
        this.Chaperone = Chaperone;
        this.RadiusFeet = RadiusFeet;
    }

    public void updateChaperone(User newChaperone){
        Chaperone = newChaperone;
    }

    public boolean WithinChaperoneGeofence(User Student){
        float userLat = Student.Latitude;
        float userLong = Student.Longitude;

        return calculateDistance(Chaperone.Latitude, Chaperone.Longitude, userLat, userLong) < RadiusFeet;
    }
}
