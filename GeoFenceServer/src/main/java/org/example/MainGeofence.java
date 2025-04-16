public class MainGeofence extends Geofence {
    public float CenterLatitude;
    public float CenterLongitude;
    public float RadiusFeet;

    public MainGeofence(float CenterLatitude, float CenterLongitude, float RadiusFeet){
        this.CenterLatitude = CenterLatitude;
        this.CenterLongitude = CenterLongitude;
        this.RadiusFeet = RadiusFeet;
    }

    public boolean WithinGeofence(User User){
        float userLat = User.Latitude;
        float userLong = User.Longitude;

        return calculateDistance(CenterLatitude, CenterLongitude, userLat, userLong) < RadiusFeet;
    }
}
