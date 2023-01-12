package com.shasthosheba.doctor.model;

import java.util.List;
import java.util.Objects;

public class Intermediary extends BaseModel{
    private String name;
    private String id;
    private List<String> patients;

    public Intermediary(String name, String id, List<String> patients) {
        this.name = name;
        this.id = id;
        this.patients = patients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Intermediary that = (Intermediary) o;

        if (!Objects.equals(name, that.name)) return false;
        if (!id.equals(that.id)) return false;
        return Objects.equals(patients, that.patients); //No problem as all the elements are strings and strings have their equals() method implemented
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + id.hashCode();
        result = 31 * result + (patients != null ? patients.hashCode() : 0);
        return result;
    }

    public Intermediary() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPatients() {
        return patients;
    }

    public void setPatients(List<String> patients) {
        this.patients = patients;
    }
}
