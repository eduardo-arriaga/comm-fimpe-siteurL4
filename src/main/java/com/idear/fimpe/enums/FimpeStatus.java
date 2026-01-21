package com.idear.fimpe.enums;

public enum FimpeStatus {

    NOT_SENT(0),
    SENT_AND_PENDIENT(1),
    SENT_OK(2),
    SENT_WITH_ERROR(3);

    private int value;

    FimpeStatus(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
