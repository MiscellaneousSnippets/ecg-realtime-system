package com.example.abdulsalam.ecgandroidclient.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Doctor implements Parcelable {

    public static final Creator<Doctor> CREATOR = new Creator<Doctor>() {
        @Override
        public Doctor createFromParcel(Parcel in) {
            return new Doctor(in);
        }

        @Override
        public Doctor[] newArray(int size) {
            return new Doctor[size];
        }
    };
    private String name;
    private String phone;
    private String address;
    private String email;
    private String hospital;
    private String type;

    public Doctor(String name, String phone, String address, String email, String hospital, String type) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.hospital = hospital;
        this.type = type;
    }

    public Doctor() {
    }

    protected Doctor(Parcel in) {
        name = in.readString();
        phone = in.readString();
        address = in.readString();
        email = in.readString();
        hospital = in.readString();
        type = in.readString();
    }

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

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        parcel.writeString(hospital);
        parcel.writeString(type);
    }
}
