package com.idear.fimpe.enums;

public enum LAMAction {
    REACTIVATION(1),
    DESACTIVATION(2),
    BLOCKING(3);

    private int key;

    LAMAction(int key){
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
