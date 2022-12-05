package com.example.patient.model;

public class Call {
    private String wants;
    private boolean video;
    private String room;
    private String patient;

    public Call() {
    }

    public Call(String wants, boolean video, String room, String patient) {
        this.wants = wants;
        this.video = video;
        this.room = room;
        this.patient = patient;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public String getWants() {
        return wants;
    }

    public void setWants(String wants) {
        this.wants = wants;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }
}
