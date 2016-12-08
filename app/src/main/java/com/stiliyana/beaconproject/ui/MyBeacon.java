package com.stiliyana.beaconproject.ui;


import org.altbeacon.beacon.Beacon;

public class MyBeacon {
    private Beacon beacon;
    private String dateTime;

    public MyBeacon() {
    }

    public MyBeacon(Beacon beacon, String dateTime) {
        this.beacon = beacon;
        this.dateTime = dateTime;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
