package com.idear.fimpe.enums;

public enum Product {
    DEBIT("4D31", "M1"),
    CREDIT("4331", "C1"),
    BPD1("4231", "B1"),
    BPD2("4232", "B2"),
    QR("", "QR"),
    TB("", "TB"),
    DEFAULT("", "\u200B");

    private String beaValue;
    private String fimpeValue;

    Product(String beaValue, String fimpeValue){
        this.beaValue = beaValue;
        this.fimpeValue = fimpeValue;
    }

    public String getBeaValue(){
        return beaValue;
    }

    public String getFimpeValue(){
        return fimpeValue;
    }
}
