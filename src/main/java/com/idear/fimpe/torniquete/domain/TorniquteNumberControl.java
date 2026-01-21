package com.idear.fimpe.torniquete.domain;

import com.idear.fimpe.properties.PropertiesHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.Product.BPD1;
import static com.idear.fimpe.enums.Product.BPD2;

public class TorniquteNumberControl {
    private String idCorredor;
    private String idSir;
    private String stationId;
    private String deviceId;
    private String serie;

    private long cutId;
    private LocalDateTime cutDate;//fecha y hora colecta
    private LocalDateTime initialCutDate;
    private LocalDateTime finalCutDate;
    private String eurId;
    private String routeId;
    private Integer totalRecords;
    private Float totalAmmountRecords;
    private String technologicProviderId;
    private Integer totalBPDs;


    private List<TorniqueteTransaction> torniqueteTransactions;

    public TorniquteNumberControl(String routeId, String eurId, String stationID, String deviceId, String serie){

        this.routeId = routeId;
        this.eurId = eurId;
        this.stationId = stationID;
        this.deviceId = deviceId;
        this.serie = serie.trim();
        this.totalAmmountRecords = 0F;
        this.totalBPDs = 0;
        this.technologicProviderId = PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;
    }
    //Calcula el numero de control y genera mas atributos que el constructor no tiene. Identifica si es BPD.
    public void calculateNumberControl(){

        totalRecords = torniqueteTransactions.size();
        technologicProviderId = PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;

        for (TorniqueteTransaction transacction: torniqueteTransactions){
            if(transacction.getProductId().equals(BPD1) || transacction.getProductId().equals(BPD2)){
                totalBPDs++;
            }else{
                totalAmmountRecords += transacction.getTransactionAmount();
            }
        }

    }

    public void setTorniqueteTransactions(List<TorniqueteTransaction> torniqueteTransactions) {
        this.torniqueteTransactions = torniqueteTransactions;
    }

    public void setCutDate(LocalDateTime cutDate) {
        this.cutDate = cutDate;
    }

    public void setIdCorredor(String idCorredor) {
        this.idCorredor = idCorredor;
    }

    public void setIdSir(String idSir) {
        this.idSir = idSir;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }


    public void setSerie(String serie) {
        this.serie = serie;
    }

    public void setCutId(long cutId) {
        this.cutId = cutId;
    }

    public void setInitialCutDate(LocalDateTime initialCutDate) {
        this.initialCutDate = initialCutDate;
    }

    public void setFinalCutDate(LocalDateTime finalCutDate) {
        this.finalCutDate = finalCutDate;
    }

    public void setEurId(String eurId) {
        this.eurId = eurId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public void setTechnologicProviderId(String technologicProviderId) {
        this.technologicProviderId = technologicProviderId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getDeviceId() {
        return deviceId;
    }

    public void setTotalBPDs(Integer totalBPDs) {
        this.totalBPDs = totalBPDs;
    }

    public String getIdCorredor() {
        return idCorredor;
    }

    public String getIdSir() {
        return idSir;
    }

    public String getStationId() {
        return stationId;
    }

    public String getSerie() {
        return serie;
    }

    public long getCutId() {
        return cutId;
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

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public String getTechnologicProviderId() {
        return technologicProviderId;
    }

    public Integer getTotalBPDs() {
        return totalBPDs;
    }

    public LocalDateTime getCutDate() {
        return cutDate;
    }

    public Float getTotalAmmountRecords() {
        return totalAmmountRecords;
    }

    public List<TorniqueteTransaction> getTorniqueteTransactions() {
        return torniqueteTransactions;
    }

    public List<TorniquteNumberControl> getTorniqueteNumberControlList(List<TorniqueteTransaction> torniqueteTransactions, int maxTransactions){
        List<TorniquteNumberControl> torniquteNumberControlList = new ArrayList<>();

        if(torniqueteTransactions.size() > maxTransactions){

            int totalPackages = torniqueteTransactions.size() / maxTransactions;
            int residuo = torniqueteTransactions.size() % maxTransactions;

            if(residuo > 0)
                totalPackages++;

            int indexLow = 0;
            int indexHigh = maxTransactions;

            for (int i = 0; i < totalPackages; i++) {
                if(i == totalPackages - 1) //Si esta en el ultimo paquete
                    indexHigh =  indexLow + residuo; //Si son 186 y debe obetener las ultimas 6 que quedan

                TorniquteNumberControl torniquteNumberControlAux = new TorniquteNumberControl(routeId, eurId, stationId, deviceId, serie);
                List<TorniqueteTransaction> torniqueteTransactionsAux = torniqueteTransactions.subList(indexLow, indexHigh);
                torniquteNumberControlAux.setTorniqueteTransactions(torniqueteTransactionsAux);
                torniquteNumberControlList.add(torniquteNumberControlAux);
                indexLow = indexHigh;
                indexHigh += maxTransactions;
            }
        }else{
            TorniquteNumberControl torniquteNumberControl = new TorniquteNumberControl(routeId, eurId, stationId, deviceId, serie);
            torniquteNumberControl.setTorniqueteTransactions(torniqueteTransactions);
            torniquteNumberControlList.add(torniquteNumberControl);
        }
        return torniquteNumberControlList;
    }
}
