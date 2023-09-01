package com.example.InPushServerDemo.model;

public class DeviceIdentifier {
    private String token;

    public DeviceIdentifier() {
    }

    public DeviceIdentifier(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
