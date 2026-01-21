package com.idear.fimpe.enums;

public enum ExtentionFile {
    NUMBER_CONTROL(".CC"),
    DATA(".DAT"),
    SUCCESSFUL_ACK(".OK"),
    ERROR_ACK(".ERR");

    private String extention;

    ExtentionFile(String extention){
        this.extention = extention;
    }

    public String getExtention(){
        return extention;
    }
}
