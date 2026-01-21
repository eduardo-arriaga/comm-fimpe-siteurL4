package com.idear.fimpe.vrt.domain;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.properties.PropertiesHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VRTNumberControl {

    private Long cutId;
    private LocalDateTime cutDate;
    private LocalDateTime initialCutDate;
    private LocalDateTime finalCutDate;
    private String eurId;
    private String routeId;
    private String deviceIdFimpe;
    private Long deviceId;
    private String stationId;
    private Integer totalRecords;
    private Float totalAmmountRecords;
    private Integer totalCardSales;
    private Float totalAmmountCardSales;
    private Integer totalRecharges;
    private Float totalAmmountRecharges;
    private Integer totalBPDs;
    private String technologicProviderId;
    private List<VRTTransaction> vrtTransactions;

    public VRTNumberControl(String eurId, String routeId, String deviceIdFimpe, Long deviceId, String stationId) {
        this.eurId = eurId;
        this.routeId = routeId;
        this.deviceIdFimpe = deviceIdFimpe;
        this.deviceId = deviceId;
        this.stationId = stationId;
        this.totalAmmountRecords = 0F;
        this.totalCardSales = 0;
        this.totalAmmountCardSales = 0F;
        this.totalRecharges = 0;
        this.totalAmmountRecharges = 0F;
        this.totalBPDs = 0;
        this.technologicProviderId = PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;
    }

    public void calculateNumberControl(){
        totalRecords = vrtTransactions.size();
        for (VRTTransaction vrtTransaction: vrtTransactions) {
            totalAmmountRecords += vrtTransaction.getTransactionAmmount();
            if(vrtTransaction.getOperationType().equals(OperationType.SELL_OK_VRT)){
                totalCardSales++;
                totalAmmountCardSales += vrtTransaction.getTransactionAmmount();
            }else {
                totalRecharges++;
                totalAmmountRecharges += vrtTransaction.getTransactionAmmount();
            }
        }
    }

    public void setCutId(Long cutId) {
        this.cutId = cutId;
    }

    public void setCutDate(LocalDateTime cutDate) {
        this.cutDate = cutDate;
    }

    public void setInitialCutDate(LocalDateTime initialCutDate) {
        this.initialCutDate = initialCutDate;
    }

    public void setFinalCutDate(LocalDateTime finalCutDate) {
        this.finalCutDate = finalCutDate;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public void setTotalAmmountRecords(Float totalAmmountRecords) {
        this.totalAmmountRecords = totalAmmountRecords;
    }

    public void setTotalCardSales(Integer totalCardSales) {
        this.totalCardSales = totalCardSales;
    }

    public void setTotalAmmountCardSales(Float totalAmmountCardSales) {
        this.totalAmmountCardSales = totalAmmountCardSales;
    }

    public void setTotalRecharges(Integer totalRecharges) {
        this.totalRecharges = totalRecharges;
    }

    public void setTotalAmmountRecharges(Float totalAmmountRecharges) {
        this.totalAmmountRecharges = totalAmmountRecharges;
    }

    public void setTotalBPDs(Integer totalBPDs) {
        this.totalBPDs = totalBPDs;
    }

    public void setTechnologicProviderId(String technologicProviderId) {
        this.technologicProviderId = technologicProviderId;
    }

    public void setVrtTransactions(List<VRTTransaction> vrtTransactions) {
        this.vrtTransactions = vrtTransactions;
    }

    public Long getCutId() {
        return cutId;
    }

    public LocalDateTime getCutDate() {
        return cutDate;
    }

    public LocalDateTime getInitialCutDate() {
        return initialCutDate;
    }

    public LocalDateTime getFinalCutDate() {
        return finalCutDate;
    }

    public String getEurId() {
        return eurId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getDeviceIdFimpe() {
        return deviceIdFimpe;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getStationId() {
        return stationId;
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

    public Integer getTotalBPDs() {
        return totalBPDs;
    }

    public String getTechnologicProviderId() {
        return technologicProviderId;
    }

    public List<VRTTransaction> getVrtTransactions() {
        return vrtTransactions;
    }

    public List<VRTNumberControl> getVRTNumberControlList(List<VRTTransaction> vrtTransactions, int maxTransactions){
        List<VRTNumberControl> vrtNumberControlList = new ArrayList<>();

        if(vrtTransactions.size() > maxTransactions){

            int totalPackages = vrtTransactions.size() / maxTransactions;
            int residuo = vrtTransactions.size() % maxTransactions;

            if(residuo > 0)
                totalPackages++;

            int indexLow = 0;
            int indexHigh = maxTransactions;

            for (int i = 0; i < totalPackages; i++) {
                if(i == totalPackages - 1) //Si esta en el ultimo paquete
                    indexHigh =  indexLow + residuo; //Si son 186 y debe obetener las ultimas 6 que quedan

                VRTNumberControl vrtNumberControl = new VRTNumberControl(eurId, routeId, deviceIdFimpe, deviceId, stationId);
                List<VRTTransaction> vrtTransactionsAux = vrtTransactions.subList(indexLow, indexHigh);
                vrtNumberControl.setVrtTransactions(vrtTransactionsAux);
                vrtNumberControlList.add(vrtNumberControl);
                indexLow = indexHigh;
                indexHigh += maxTransactions;
            }
        }else{
            VRTNumberControl vrtNumberControl = new VRTNumberControl(eurId, routeId, deviceIdFimpe, deviceId, stationId);
            vrtNumberControl.setVrtTransactions(vrtTransactions);
            vrtNumberControlList.add(vrtNumberControl);
        }
        return vrtNumberControlList;
    }
}
