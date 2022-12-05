package com.example.patient.model;

public class Patient {
    private String name;
    private String status;
    private String uId;

    public Patient(String uId, String name, String status) {
        this.uId = uId;
        this.name = name;
        this.status = status;
    }

    public Patient() {
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

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}