package com.shasthosheba.doctor.model;

import java.util.Objects;

public class ChamberMember extends BaseModel {
    private String intermediaryId;
    private String name;
    private boolean withPayment;
    private String transactionId;
    private String memberBKashNo;
    private int amount;
    private String callDeviceToken;
    private long timestamp;

    public ChamberMember() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChamberMember that = (ChamberMember) o;

        if (!intermediaryId.equals(that.intermediaryId)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = intermediaryId.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String getIntermediaryId() {
        return intermediaryId;
    }

    public void setIntermediaryId(String intermediaryId) {
        this.intermediaryId = intermediaryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMemberBKashNo() {
        return memberBKashNo;
    }

    public void setMemberBKashNo(String memberBKashNo) {
        this.memberBKashNo = memberBKashNo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isWithPayment() {
        return withPayment;
    }

    public void setWithPayment(boolean withPayment) {
        this.withPayment = withPayment;
    }

    public String getCallDeviceToken() {
        return callDeviceToken;
    }

    public void setCallDeviceToken(String callDeviceToken) {
        this.callDeviceToken = callDeviceToken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
