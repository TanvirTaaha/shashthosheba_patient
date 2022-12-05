package com.example.patient.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Doctor implements Serializable {
    private String name;
    private String status;
    private String uId;

    public Doctor( String uId, String name, String status) {
        this.uId = uId;
        this.name = name;
        this.status = status;
    }

    public Doctor() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
