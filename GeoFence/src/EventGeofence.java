import java.util.ArrayList;
import java.util.List;

public class EventGeofence {
    public User Chaperone;
    public List<User> StudentGroup;
    public MainGeofence Geofence;
    public ChaperoneGeofence ChaperoneGeofence;
    public List<GoIn2Group> GoIn2Groups;
    public List<User> StudentsOutsideFence;
    public List<GoIn2Group> GoIn2GroupsOutsideFence;


    // Establish Event's Geofence
    public EventGeofence(User Chaperone, Float EventCenterLatitude, Float EventCenterLongitude, Float EventRadiusFeet, float ChaperoneDistance, float GoIn2Distance) {
        this.Chaperone = Chaperone;
        this.StudentGroup = new ArrayList<User>();
        Geofence = new MainGeofence(EventCenterLatitude, EventCenterLongitude, EventRadiusFeet);
        ChaperoneGeofence = new ChaperoneGeofence(Chaperone, ChaperoneDistance);
        GoIn2Groups = new ArrayList<GoIn2Group>();
    }

    // Add a singular student (Not sure if needed )
    public void addStudent(User Student) {
        this.StudentGroup.add(Student);
    }

    // Add/update studentGroup list (Probably use it for updating locations)
    public void setStudentGroup(List<User> group){
        this.StudentGroup = group;
    }

    // Check Main Geofence
    public boolean checkGeofence() {
        boolean flag = true;
        for (User student : StudentGroup) {
            if(!Geofence.WithinGeofence(student)){
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

    //Check all GoIn2 groups and see if they are too far apart
    public boolean checkGoIn2Groups() {
        boolean flag = true;
        for (GoIn2Group group : GoIn2Groups) {
            if(!group.checkGroup()){
                flag = false;
                GoIn2GroupsOutsideFence.add(group);
            }
        }
        return flag;
    }

    //Return GoIn2Groups who are too far apart
    public List<GoIn2Group> getGoIn2GroupsOutsideFence() {
        return GoIn2GroupsOutsideFence;
    }

    public boolean checkChaperoneGeofence() {
        boolean flag = true
    }
    

}
