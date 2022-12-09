package com.shasthosheba.doctor.model;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public class Prescription extends BaseModel {
    @NonNull
    private String id;
    private String prescriptionTitle;
    private String doctorName;
    private String doctorId;
    private String patientName;
    private String patientId;
    private String intermediaryName;
    private String intermediaryId;
    private String illnessDescription;
    private String advice;
    private long dateUnix;
    private List<String> medicines;
    private List<String> tests;

    public Prescription(String id, String prescriptionTitle, String doctorName, String doctorId,
                        String patientName, String patientId,
                        String intermediaryName, String intermediaryId,
                        String illnessDescription, String advice,
                        long dateUnix, List<String> medicines, List<String> tests) {
        this.id = id;
        this.prescriptionTitle = prescriptionTitle;
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.patientId = patientId;
        this.intermediaryName = intermediaryName;
        this.intermediaryId = intermediaryId;
        this.illnessDescription = illnessDescription;
        this.advice = advice;
        this.dateUnix = dateUnix;
        this.medicines = medicines;
        this.tests = tests;
    }

    public Prescription() {
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getIntermediaryName() {
        return intermediaryName;
    }

    public void setIntermediaryName(String intermediaryName) {
        this.intermediaryName = intermediaryName;
    }

    public String getIntermediaryId() {
        return intermediaryId;
    }

    public void setIntermediaryId(String intermediaryId) {
        this.intermediaryId = intermediaryId;
    }

    public String getIllnessDescription() {
        return illnessDescription;
    }

    public void setIllnessDescription(String illnessDescription) {
        this.illnessDescription = illnessDescription;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public List<String> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<String> medicines) {
        this.medicines = medicines;
    }

    public List<String> getTests() {
        return tests;
    }

    public void setTests(List<String> tests) {
        this.tests = tests;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrescriptionTitle() {
        return prescriptionTitle;
    }

    public void setPrescriptionTitle(String prescriptionTitle) {
        this.prescriptionTitle = prescriptionTitle;
    }

    public long getDateUnix() {
        return dateUnix;
    }

    public void setDateUnix(long dateUnix) {
        this.dateUnix = dateUnix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prescription that = (Prescription) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
