package com.idear.fimpe.cet.domain;

import com.idear.fimpe.properties.PropertiesHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.Product.BPD1;
import static com.idear.fimpe.enums.Product.BPD2;

public class CETNumberControl {
    private Long cutId;
    private LocalDateTime cutDate;
    private LocalDateTime initialCutDate;
    private LocalDateTime finalCutDate;
    private Long routeId;
    private Long busId;
    private String eurId;
    private String routeIdDescription;
    private String deviceId;
    private String vehicleId;
    private Integer totalRecords;
    private Float totalAmmountRecords;
    private Integer totalCardSales;
    private Float totalAmmountCardSales;
    private Integer totalRecharges;
    private Float totalAmmountRecharges;
    private String technologicProviderId;
    private Integer totalBPDs;
    private List<CETTransaction> cetTransactions;

    public CETNumberControl(Long busId, Long routeId, String deviceId, String routeDescription, String eurId ) {
        this.busId = busId;
        this.routeId = routeId;
        this.deviceId = deviceId;
        this.routeIdDescription = routeDescription;
        this.eurId = eurId;
        this.totalAmmountRecords = 0F;
        this.totalBPDs = 0;
    }

    public void calculateNumberControlDebit(){
        totalRecords = cetTransactions.size();
        technologicProviderId = PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;
        vehicleId = routeIdDescription + "-U" + deviceId;//TL04-A01-U11099

        for (CETTransaction transaction: cetTransactions) {
            if(transaction.getProductId().equals(BPD1) || transaction.getProductId().equals(BPD2)){
                totalBPDs++;
            }else{
                totalAmmountRecords += transaction.getTransactionAmmount();
            }
        }
    }

    public void calculateNumberControlRecharge(){
        totalRecords = cetTransactions.size();
        technologicProviderId = PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;
        vehicleId = routeIdDescription + "-U" + deviceId;//TL04-A01-U11099

        for (CETTransaction transaction: cetTransactions) {
            totalAmmountRecords += transaction.getTransactionAmmount();

        }

        totalRecharges = totalRecords;
        totalAmmountRecharges = totalAmmountRecords;
        totalCardSales = 0;
        totalAmmountCardSales = 0F;
    }

    public void setCetTransactions(List<CETTransaction> cetTransactions) {
        this.cetTransactions = cetTransactions;
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

    public String getEurId() {
        return eurId;
    }

    public void setEurId(String eurId) {
        this.eurId = eurId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = removeCharacter(deviceId, '-');
    }
    public String getVehicleId() {
        return vehicleId;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public Float getTotalAmmountRecords() {
        return totalAmmountRecords;
    }

    public Integer getTotalCardSales() {
        return totalCardSales;
    }

    public Float getTotalAmmountCardSales() {
        return totalAmmountCardSales;
    }

    public Integer getTotalRecharges() {
        return totalRecharges;
    }

    public Float getTotalAmmountRecharges() {
        return totalAmmountRecharges;
    }

    public String getTechnologicProviderId() {
        return technologicProviderId;
    }

    public Integer getTotalBPDs() {
        return totalBPDs;
    }

    public List<CETTransaction> getCetTransactions() {
        return cetTransactions;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getRouteIdDescription() {
        return routeIdDescription;
    }

    public void setRouteIdDescription(String routeIdDescription) {
        this.routeIdDescription = routeIdDescription;
    }

    private String removeCharacter(String word, char characterToRemove){
        List<Character> wordCharacterList = new ArrayList<>();
        String wordProceced  = "";
        for (char character: word.toCharArray()) {
            if(character != characterToRemove){
                wordCharacterList.add(character);
            }
        }
        for (Character character: wordCharacterList) {
            wordProceced += character;
        }
        return wordProceced;
    }

    public List<CETNumberControl> getCETNumberControlList(List<CETTransaction> cetTransactions, int maxTransactions){
        List<CETNumberControl> cetNumberControlList = new ArrayList<>();

        if(cetTransactions.size() > maxTransactions){

            int totalPackages = cetTransactions.size() / maxTransactions;
            int residuo = cetTransactions.size() % maxTransactions;

            if(residuo > 0)
                totalPackages++;

            int indexLow = 0;
            int indexHigh = maxTransactions;

            for (int i = 0; i < totalPackages; i++) {
                if(i == totalPackages - 1) //Si esta en el ultimo paquete
                    indexHigh =  indexLow + residuo; //Si son 186 y debe obetener las ultimas 6 que quedan

                CETNumberControl cetNumberControlAux = new CETNumberControl(busId, routeId, deviceId, routeIdDescription, eurId);
                List<CETTransaction> cetTransactionsAux = cetTransactions.subList(indexLow, indexHigh);
                cetNumberControlAux.setCetTransactions(cetTransactionsAux);
                cetNumberControlList.add(cetNumberControlAux);
                indexLow = indexHigh;
                indexHigh += maxTransactions;
            }
        }else{
            CETNumberControl cetNumberControl = new CETNumberControl(busId, routeId, deviceId, routeIdDescription, eurId);
            cetNumberControl.setCetTransactions(cetTransactions);
            cetNumberControlList.add(cetNumberControl);
        }
        return cetNumberControlList;
    }
}
