package com.shasthosheba.doctor.model;

public class DoctorProfile extends BaseModel {
    private String name;
    private String docId;
    private String speciality;
    private String isApproved;
    private String contactNo;
    private String bkash;
    private String nagad;
    private String upay;

    public DoctorProfile() {
    }

    public DoctorProfile(String name, String docId, String speciality, String isApproved, String contactNo, String bkash, String nagad, String upay) {
        this.name = name;
        this.docId = docId;
        this.speciality = speciality;
        this.isApproved = isApproved;
        this.contactNo = contactNo;
        this.bkash = bkash;
        this.nagad = nagad;
        this.upay = upay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(String isApproved) {
        this.isApproved = isApproved;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getBkash() {
        return bkash;
    }

    public void setBkash(String bkash) {
        this.bkash = bkash;
    }

    public String getNagad() {
        return nagad;
    }

    public void setNagad(String nagad) {
        this.nagad = nagad;
    }

    public String getUpay() {
        return upay;
    }

    public void setUpay(String upay) {
        this.upay = upay;
    }
}

