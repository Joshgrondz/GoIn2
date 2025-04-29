package org.example;

public class TempUser {
    public int ID;
    public String FirstName;
    public String LastName;
    public String UserType;

    public TempUser(int ID, String FirstName, String LastName, String UserType) {
        this.ID = ID;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.UserType = UserType;
    }

    @Override
    public String toString() {
        return "TempUser{" +
                "ID=" + ID +
                ", FirstName='" + FirstName + '\'' +
                ", LastName='" + LastName + '\'' +
                ", UserType='" + UserType + '\'' +
                '}';
    }
}
