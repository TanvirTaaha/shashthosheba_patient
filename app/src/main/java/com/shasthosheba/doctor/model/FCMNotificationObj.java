package com.shasthosheba.doctor.model;

public class FCMNotificationObj extends BaseModel {
    private String to;
    private Notification notification;
    private Data data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Call call;

        public Call getCall() {
            return call;
        }

        public void setCall(Call call) {
            this.call = call;
        }
    }

    public static class Notification {
        private String body;
        private String title;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
