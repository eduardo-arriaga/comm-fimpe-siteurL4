package com.idear.fimpe.torniquete.domain;

import java.util.ArrayList;
import java.util.List;

public class TorniqueteReportRecord {
    private String station;
    private String locationId;
    private Long deviceId;
    private Integer debitTorniqueteRecordsOk;
    private Integer debitTorniqueteRecordsError;

    private Integer debitGaritaRecordsOk;
    private Integer debitGaritaRecordsError;
    private Integer debitQRRecordsOk;
    private Integer debitQRRecordsError;

    public TorniqueteReportRecord(String station, String locationId, Long deviceId) {
        this.station = station;
        this.locationId = locationId;
        this.deviceId = deviceId;
        debitTorniqueteRecordsOk = 0;
        debitTorniqueteRecordsError = 0;
        debitGaritaRecordsOk = 0;
        debitGaritaRecordsError = 0;
        debitQRRecordsOk = 0;
        debitQRRecordsError = 0;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDebitTorniqueteRecordsOk() {
        return debitTorniqueteRecordsOk;
    }

    public void setDebitTorniqueteRecordsOk(Integer debitTorniqueteRecordsOk) {
        this.debitTorniqueteRecordsOk = debitTorniqueteRecordsOk;
    }

    public Integer getDebitTorniqueteRecordsError() {
        return debitTorniqueteRecordsError;
    }

    public void setDebitTorniqueteRecordsError(Integer debitTorniqueteRecordsError) {
        this.debitTorniqueteRecordsError = debitTorniqueteRecordsError;
    }

    public Integer getDebitQRRecordsOk() {
        return debitQRRecordsOk;
    }

    public void setDebitQRRecordsOk(Integer debitQRRecordsOk) {
        this.debitQRRecordsOk = debitQRRecordsOk;
    }

    public Integer getDebitQRRecordsError() {
        return debitQRRecordsError;
    }

    public void setDebitQRRecordsError(Integer debitQRRecordsError) {
        this.debitQRRecordsError = debitQRRecordsError;
    }

    public Integer getDebitGaritaRecordsOk() {
        return debitGaritaRecordsOk;
    }

    public void setDebitGaritaRecordsOk(Integer debitGaritaRecordsOk) {
        this.debitGaritaRecordsOk = debitGaritaRecordsOk;
    }

    public Integer getDebitGaritaRecordsError() {
        return debitGaritaRecordsError;
    }

    public void setDebitGaritaRecordsError(Integer debitGaritaRecordsError) {
        this.debitGaritaRecordsError = debitGaritaRecordsError;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Estacion");
        headers.add("LocationId");
        headers.add("Debito Torniquete Exitoso");
        headers.add("Debito Torniquete Error");
        headers.add("Debito Garita Exitoso");
        headers.add("Debito Garita Error");
        headers.add("Debito QR Exitoso");
        headers.add("Debito QR Error");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();
        orderValues.add(station);
        orderValues.add(locationId);
        orderValues.add(String.valueOf(debitTorniqueteRecordsOk));
        orderValues.add(String.valueOf(debitTorniqueteRecordsError));
        orderValues.add(String.valueOf(debitGaritaRecordsOk));
        orderValues.add(String.valueOf(debitGaritaRecordsError));
        orderValues.add(String.valueOf(debitQRRecordsOk));
        orderValues.add(String.valueOf(debitQRRecordsError));
        return orderValues;
    }
}
