package com.idear.fimpe.kilometers.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KilometersNumberControl {

    private Long cutId;
    private LocalDateTime cutDate;
    private LocalDateTime initialCutDate;
    private LocalDateTime finalCutDate;
    private String idSIR;
    private String routeIdDescription;
    private Integer totalRecords;
    private Double totalKilometers;
    private Double totalLaps;
    private String technologicalProvider;
    private List<KilometersTransaction> kilometersTransactions;

    public KilometersNumberControl(){

    }
    public KilometersNumberControl(String routeIdDescription, String idSIR){
        this.routeIdDescription = routeIdDescription;
        this.idSIR = idSIR;
        totalKilometers = 0D;
        totalLaps = 0D;
        kilometersTransactions = new ArrayList<>();
    }

    public List<KilometersTransaction> getKilometersTransactions() {
        return kilometersTransactions;
    }

    public void setKilometersTransactions(List<KilometersTransaction> kilometersTransactions) {
        this.kilometersTransactions = kilometersTransactions;
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

    public Double getTotalKilometers() {
        return totalKilometers;
    }

    public String getTechnologicalProvider() {
        return technologicalProvider;
    }

    public void setTechnologicalProvider(String technologicalProvider) {
        this.technologicalProvider = technologicalProvider;
    }

    public Double getTotalLaps() {
        return totalLaps;
    }

    public void calculate(){
        totalRecords = kilometersTransactions.size();
        for (KilometersTransaction cashTransaction: kilometersTransactions) {
            totalLaps += cashTransaction.getLaps();
            totalKilometers += cashTransaction.getAccumulatedKilometers();
        }
    }
}
