package org.example;

public class MainGeofence extends Geofence {
    public float CenterLatitude;
    public float CenterLongitude;
    public float RadiusMeters;

    public MainGeofence(float CenterLatitude, float CenterLongitude, float RadiusMeters){
        this.CenterLatitude = CenterLatitude;
        this.CenterLongitude = CenterLongitude;
        this.RadiusMeters = RadiusMeters;
    }

    public boolean WithinGeofence(User User){
        float userLat = User.Latitude;
        float userLong = User.Longitude;

        return calculateDistance(CenterLatitude, CenterLongitude, userLat, userLong) < RadiusMeters;
    }
}
