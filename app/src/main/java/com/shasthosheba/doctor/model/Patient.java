package com.shasthosheba.doctor.model;

import java.io.Serializable;
import java.util.List;

public class Patient extends BaseModel {
    private String name;
    private int birthYear;
    private List<String> prescriptionIds;
    private String id;

    public Patient(String name, int birthYear, List<String> prescriptionIds, String id) {
        this.name = name;
        this.birthYear = birthYear;
        this.prescriptionIds = prescriptionIds;
        this.id = id;
    }

    public Patient() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public List<String> getPrescriptionIds() {
        return prescriptionIds;
    }

    public void setPrescriptionIds(List<String> prescriptionIds) {
        this.prescriptionIds = prescriptionIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
