package org.example;

import java.time.Instant;

public class User {
    public int ID;
    public String FirstName;
    public String LastName;
    public float Latitude;
    public float Longitude;
    public Instant timestamp;

    public User(int ID, String FirstName, String LastName, float Latitude, float Longitude, long timestamp) {
        this.ID = ID;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.timestamp = Instant.ofEpochMilli(timestamp);
    }

    public int getID(){
        return ID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "ID=" + ID +
                ", FirstName='" + FirstName + '\'' +
                ", LastName='" + LastName + '\'' +
                '}';
    }
}
