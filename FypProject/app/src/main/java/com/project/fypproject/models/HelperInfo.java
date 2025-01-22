package com.project.fypproject.models;

public class HelperInfo {
    String firstName, lastName, nationality, totalExperienceDuration, postTime, img_url,userEmail,age;

    public HelperInfo() {
    }

    public HelperInfo(String firstName, String lastName, String nationality, String totalExperienceDuration, String postTime, String img_url, String userEmail, String age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.totalExperienceDuration = totalExperienceDuration;
        this.postTime = postTime;
        this.img_url = img_url;
        this.userEmail = userEmail;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getTotalExperienceDuration() {
        return totalExperienceDuration;
    }

    public void setTotalExperienceDuration(String totalExperienceDuration) {
        this.totalExperienceDuration = totalExperienceDuration;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}