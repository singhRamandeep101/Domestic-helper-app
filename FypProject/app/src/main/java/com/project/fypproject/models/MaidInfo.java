package com.project.fypproject.models;

import java.io.ObjectStreamException;
import java.util.Map;

public class MaidInfo {
    String firstName, lastName, nationality, img_url, age, religion;
    Map<String, Object> overseasExperience;
    public MaidInfo() {
    }

    public MaidInfo(String firstName, String lastName, String nationality, Map<String, Object> overseasExperience, String img_url, String age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.overseasExperience = overseasExperience;
        this.img_url = img_url;
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


    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public Map<String, Object> getOverseasExperience() {
        return overseasExperience;
    }

    public void setOverseasExperience(Map<String, Object> overseasExperience) {
        this.overseasExperience = overseasExperience;
    }
}