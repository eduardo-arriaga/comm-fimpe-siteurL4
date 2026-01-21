package com.idear.fimpe.counters.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CountersNumberControl {

    private Long cutId;
    private LocalDateTime cutDate;
    private LocalDateTime initialCutDate;
    private LocalDateTime finalCutDate;
    private String idSIR;
    private String routeIdDescription;
    private Integer totalRecords;
    private Integer totalGetOn;
    private Integer totalGetOff;
    private String technologicalProvider;
    private List<CountersTransaction> countersTransactions;

    public CountersNumberControl(){

    }
    public CountersNumberControl(String routeIdDescription, String idSIR){
        this.routeIdDescription = routeIdDescription;
        this.idSIR = idSIR;
        totalGetOn = 0;
        totalGetOff = 0;
        countersTransactions = new ArrayList<>();
    }

    public List<CountersTransaction> getCountersTransactions() {
        return countersTransactions;
    }

    public void setCountersTransactions(List<CountersTransaction> countersTransactions) {
        this.countersTransactions = countersTransactions;
    }

    public Long getCutId() {
        return cutId;
    }

    public void setCutId(Long cutId) {
        this.cutId = cutId;
    }

    public LocalDateTime getCutDate() {
        return cutDate;
    }

    public void setCutDate(LocalDateTime cutDate) {
        this.cutDate = cutDate;
    }

    public LocalDateTime getInitialCutDate() {
        return initialCutDate;
    }

    public void setInitialCutDate(LocalDateTime initialCutDate) {
        this.initialCutDate = initialCutDate;
    }

    public LocalDateTime getFinalCutDate() {
        return finalCutDate;
    }

    public void setFinalCutDate(LocalDateTime finalCutDate) {
        this.finalCutDate = finalCutDate;
    }

    public String getIdSIR() {
        return idSIR;
    }

    public String getRouteIdDescription() {
        return routeIdDescription;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public Integer getTotalGetOn() {
        return totalGetOn;
    }

    public String getTechnologicalProvider() {
        return technologicalProvider;
    }

    public void setTechnologicalProvider(String technologicalProvider) {
        this.technologicalProvider = technologicalProvider;
    }

    public Integer getTotalGetOff() {
        return totalGetOff;
    }

    public void calculate(){
        totalRecords = countersTransactions.size();
        for (CountersTransaction cashTransaction: countersTransactions) {
            totalGetOff += cashTransaction.getGetOffNumbers();
            totalGetOn += cashTransaction.getGetOnNumbers();
        }
    }
}
