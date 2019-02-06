package com.example.abdulsalam.ecgandroidclient.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Patient implements Parcelable {

    private String name;
    private String phone;
    private String address;
    private String email;
    private String age;
    private String gender;
    private String weight;
    private String height;
    private String doctor_name;
    private String type;
    private LatLng currentLocation;

    public Patient() {
    }

    public Patient(String name, String phone, String address, String email, String age, String gender, String weight, String height, String doctor_name, String type) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.doctor_name = doctor_name;
        this.type = type;
    }


    protected Patient(Parcel in) {
        name = in.readString();
        phone = in.readString();
        address = in.readString();
        email = in.readString();
        age = in.readString();
        gender = in.readString();
        weight = in.readString();
        height = in.readString();
        doctor_name = in.readString();
        type = in.readString();
        currentLocation = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(address);
        parcel.writeString(email);
        parcel.writeString(age);
        parcel.writeString(gender);
        parcel.writeString(weight);
        parcel.writeString(height);
        parcel.writeString(doctor_name);
        parcel.writeString(type);
        parcel.writeParcelable(currentLocation, i);
    }
}
