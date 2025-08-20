package com.example.uvsensor.data;

public class State {
    public double latitude;
    public double longitude;
    public Long timeStamp;

    public State(double latitude, double longitude, Long timeStamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
    }
}
