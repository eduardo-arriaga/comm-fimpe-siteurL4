package com.idear.fimpe.enums;

public enum Device {
    CET("CET"),
    TORNIQUETE("TORNIQUETE"),
    VRT("VRT");

    private String name;

    Device(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
