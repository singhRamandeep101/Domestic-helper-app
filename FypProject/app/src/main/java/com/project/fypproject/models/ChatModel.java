package com.project.fypproject.models;

public class ChatModel {
    private String lastName,firstName,email,userType;

    public ChatModel() {
    }

    public ChatModel(String lastName, String firstName, String email,String userType) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.userType = userType;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
