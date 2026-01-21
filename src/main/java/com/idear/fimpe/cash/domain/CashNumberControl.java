package com.idear.fimpe.cash.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CashNumberControl {
    private Long cutId;
    private LocalDateTime cutDate;
    private LocalDateTime initialCutDate;
    private LocalDateTime finalCutDate;
    private String idSIR;
    private String technologicalProvider;
    private String routeIdDescription;
    private Integer totalRecords;
    private Float totalAmountRecords;
    private Integer totalPassengers;
    private List<CashTransaction> cashTransactions;

    public CashNumberControl(){

    }
    public CashNumberControl(String routeIdDescription, String idSIR){
        this.routeIdDescription = routeIdDescription;
        this.idSIR = idSIR;
        totalAmountRecords = 0F;
        totalPassengers = 0;
        cashTransactions = new ArrayList<>();
    }


    public List<CashTransaction> getCashTransactions() {
        return cashTransactions;
    }

    public void setCashTransactions(List<CashTransaction> cashTransactions) {
        this.cashTransactions = cashTransactions;
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

    public Float getTotalAmmountRecords() {
        return totalAmountRecords;
    }

    public String getTechnologicalProvider() {
        return technologicalProvider;
    }

    public void setTechnologicalProvider(String technologicalProvider) {
        this.technologicalProvider = technologicalProvider;
    }

    public Integer getTotalPassengers() {
        return totalPassengers;
    }

    public void calculate(){
        totalRecords = cashTransactions.size();
        for (CashTransaction cashTransaction: cashTransactions) {
            totalPassengers += cashTransaction.getTotalPassengers();
            totalAmountRecords += cashTransaction.getAmount();
        }
    }
}
