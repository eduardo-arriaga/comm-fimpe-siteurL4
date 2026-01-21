package com.idear.fimpe.enums;

public enum PrefixFile {
    DEBIT("D", "DEBITO"),
    RECHARGE("R", "RECARGA"),
    ACTION_LIST("LN", "LISTA DE ACCION"),
    CASH("E", "EFECTIVO"),
    COUNTERS("C", "CONTADORES"),
    KILOMETERS("K", "KILOMETROS"),
    REMOTE_RECHARGE("LR", "RECARGA REMOTA");

    private String prefix;
    private String fullName;

    PrefixFile(String prefix, String fullName){
        this.prefix = prefix;
        this.fullName = fullName;
    }

    public String getPrefix(){
        return prefix;
    }

    public String getFullName() {
        return fullName;
    }
}
