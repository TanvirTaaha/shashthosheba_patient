package com.example.patient.app;

public enum IntentTag {
    DOCTOR_NAME("doctor_name"),
    DOCTOR_UID("doctor_uid"),
    DOCTOR_STATUS("doctor_status"),
    DOCTOR("doctor_object");

    public final String tag;

    IntentTag(String tag) {
        this.tag = tag;
    }
}
