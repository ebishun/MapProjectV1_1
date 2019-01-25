package com.promosys.mapprojectv1_1;

/**
 * Created by Fimrware 2 on 4/6/2017.
 */

public class LocationObject {

    String locGroup,locName,locLatitude,locLongitude;
    String isClicked;

    public LocationObject(){}

    public LocationObject(String locGroup, String locName,String locLatitude, String locLongitude, String isClicked){

        this.locGroup = locGroup;
        this.locName = locName;
        this.locLatitude = locLatitude;
        this.locLongitude = locLongitude;
        this.isClicked = isClicked;

    }

    public String getLocGroup() {
        return locGroup;
    }

    public void setLocGroup(String locGroup) {
        this.locGroup = locGroup;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public String getLocLatitude() {
        return locLatitude;
    }

    public void setLocLatitude(String locLatitude) {
        this.locLatitude = locLatitude;
    }

    public String getLocLongitude() {
        return locLongitude;
    }

    public void setLocLongitude(String locLongitude) {
        this.locLongitude = locLongitude;
    }

    public String getIsClicked() {
        return isClicked;
    }

    public void setIsClicked(String isClicked) {
        this.isClicked = isClicked;
    }
}
