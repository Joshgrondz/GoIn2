import java.util.ArrayList;
import java.util.List;

public class EventGroup {
    public User Chaperone;
    public List<User> StudentGroup;
    public MainGeofence Geofence;
    public ChaperoneGeofence ChaperoneGeofence;
    public List<GoIn2Group> GoIn2Groups;


    public EventGroup(User Chaperone, Float EventCenterLatitude, Float EventCenterLongitude, Float EventRadiusFeet, float ChaperoneDistance, float GoIn2Distance) {
        this.Chaperone = Chaperone;
        this.StudentGroup = new ArrayList<User>();
        Geofence = new MainGeofence(EventCenterLatitude, EventCenterLongitude, EventRadiusFeet);
        ChaperoneGeofence = new ChaperoneGeofence(Chaperone, ChaperoneDistance);
        GoIn2Groups = new ArrayList<GoIn2Group>();
    }

}
