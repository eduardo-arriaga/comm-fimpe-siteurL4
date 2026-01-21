package com.idear.fimpe.vrt.domain;

import java.util.ArrayList;
import java.util.List;

public class VRTReportRecord {
    private Long deviceId;
    private String station;
    private String locationId;
    private Integer rechargeRecordsOk;
    private Integer rechargeRecordsError;
    private Integer rechargeQRRecordsOk;
    private Integer rechargeQRRecordsError;
    private Integer sellRecordsOk;
    private Integer sellRecordsError;

    public VRTReportRecord(Long deviceId, String station, String locationId) {
        this.deviceId = deviceId;
        this.station = station;
        this.locationId = locationId;
        rechargeRecordsOk = 0;
        rechargeRecordsError = 0;
        rechargeQRRecordsOk = 0;
        rechargeQRRecordsError = 0;
        sellRecordsOk = 0;
        sellRecordsError = 0;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
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

    public Integer getRechargeQRRecordsOk() {
        return rechargeQRRecordsOk;
    }

    public void setRechargeQRRecordsOk(Integer rechargeQRRecordsOk) {
        this.rechargeQRRecordsOk = rechargeQRRecordsOk;
    }

    public Integer getRechargeQRRecordsError() {
        return rechargeQRRecordsError;
    }

    public void setRechargeQRRecordsError(Integer rechargeQRRecordsError) {
        this.rechargeQRRecordsError = rechargeQRRecordsError;
    }

    public Integer getSellRecordsOk() {
        return sellRecordsOk;
    }

    public void setSellRecordsOk(Integer sellRecordsOk) {
        this.sellRecordsOk = sellRecordsOk;
    }

    public Integer getSellRecordsError() {
        return sellRecordsError;
    }

    public void setSellRecordsError(Integer sellRecordsError) {
        this.sellRecordsError = sellRecordsError;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Estacion");
        headers.add("LocationId");
        headers.add("Recarga Exitoso");
        headers.add("Recarga Error");
        headers.add("Recarga QR Exitoso");
        headers.add("Recarga QR Error");
        headers.add("Venta Exitoso");
        headers.add("Venta Error");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();
        orderValues.add(station);
        orderValues.add(locationId);
        orderValues.add(String.valueOf(rechargeRecordsOk));
        orderValues.add(String.valueOf(rechargeRecordsError));
        orderValues.add(String.valueOf(rechargeQRRecordsOk));
        orderValues.add(String.valueOf(rechargeQRRecordsError));
        orderValues.add(String.valueOf(sellRecordsOk));
        orderValues.add(String.valueOf(sellRecordsError));
        return orderValues;
    }


}
