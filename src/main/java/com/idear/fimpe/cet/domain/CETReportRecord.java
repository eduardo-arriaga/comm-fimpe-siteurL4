package com.idear.fimpe.cet.domain;

import java.util.ArrayList;
import java.util.List;

public class CETReportRecord {

    private Long busId;
    private String busDescription;
    private Integer debitRecordsOk;
    private Integer debitRecordsError;
    private Integer rechargeRecordsOk;
    private Integer rechargeRecordsError;

    public CETReportRecord(Long busId, String busDescription) {
        this.busId = busId;
        this.busDescription = busDescription;
        debitRecordsOk = 0;
        debitRecordsError = 0;
        rechargeRecordsOk = 0;
        rechargeRecordsError = 0;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getBusDescription() {
        return busDescription;
    }

    public void setBusDescription(String busDescription) {
        this.busDescription = busDescription;
    }

    public Integer getDebitRecordsOk() {
        return debitRecordsOk;
    }

    public void setDebitRecordsOk(Integer debitRecordsOk) {
        this.debitRecordsOk = debitRecordsOk;
    }

    public Integer getDebitRecordsError() {
        return debitRecordsError;
    }

    public void setDebitRecordsError(Integer debitRecordsError) {
        this.debitRecordsError = debitRecordsError;
    }

    public Integer getRechargeRecordsOk() {
        return rechargeRecordsOk;
    }

    public void setRechargeRecordsOk(Integer rechargeRecordsOk) {
        this.rechargeRecordsOk = rechargeRecordsOk;
    }

    public Integer getRechargeRecordsError() {
        return rechargeRecordsError;
    }

    public void setRechargeRecordsError(Integer rechargeRecordsError) {
        this.rechargeRecordsError = rechargeRecordsError;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Id Autobus");
        headers.add("Descripcion");
        headers.add("Debito Exitoso");
        headers.add("Debito Error");
        headers.add("Recarga Exitoso");
        headers.add("Recarga Error");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> classOrderValues = new ArrayList<>();
        classOrderValues.add(String.valueOf(busId));
        classOrderValues.add(busDescription);
        classOrderValues.add(String.valueOf(debitRecordsOk));
        classOrderValues.add(String.valueOf(debitRecordsError));
        classOrderValues.add(String.valueOf(rechargeRecordsOk));
        classOrderValues.add(String.valueOf(rechargeRecordsError));
        return classOrderValues;
    }
}
