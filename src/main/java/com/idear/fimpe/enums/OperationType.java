package com.idear.fimpe.enums;

public enum OperationType {

    DEBIT_OK_BPD_CET(2),
    DEBIT_OK_CET(3),
    RECHARGE_OK_CET(136),
    SELL_OK_VRT(66),
    RECHARGE_OK_VRT(60),
    REMOTE_RECHARGE_OK_VRT(65),
    RECHARGE_QR_OK_VRT(67),
    RECHARGE_QR_OK_COMMISSION_VRT(68),
    DEBIT_OK_TORNIQUETE(6),
    DEBIT_TB_OK_TORNIQUETE(10),
    DEBIT_QR_OK_TORNIQUETE(11),
    DEBIT_OK_GARITA(7),
    UNKNOWN(-1);

    private int value;

    OperationType(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static OperationType getOperationType(int value){
        for(OperationType op : OperationType.values()){
            if(op.getValue() == value){
                return op;
            }
        }
        return UNKNOWN;
    }
}
