package com.project.fypproject.models;

public class HelperInfo {
    String name, nationality, zodiac, img_url, email, age;

    public HelperInfo() {
    }

    public HelperInfo(String name, String nationality, String zodiac, String img_url, String email, String age) {
        this.name = name;
        this.nationality = nationality;
        this.zodiac = zodiac;
        this.img_url = img_url;
        this.email = email;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getZodiac() {
        return zodiac;
    }

    public void setZodiac(String zodiac) {
        this.zodiac = zodiac;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}