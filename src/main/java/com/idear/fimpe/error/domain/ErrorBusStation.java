package com.idear.fimpe.error.domain;

import java.util.ArrayList;
import java.util.List;

public class ErrorBusStation {

    private Long cutFoil;
    private Long transactionId;
    private String cardId;
    private Long cardFoil;
    private Long busStationId;
    private String locationIdDescription;
    private String routeId;
    private String errorDescription;
    private String fileName;

    public ErrorBusStation(Long cutFoil, String cardId, Long cardFoil, String errorDescription, String fileName) {
        this.cutFoil = cutFoil;
        this.cardId = cardId;
        this.cardFoil = cardFoil;
        this.errorDescription = errorDescription;
        this.fileName = fileName;
        busStationId = 0L;
        transactionId = 0L;
        locationIdDescription = "Sin informacion";
        routeId = "Sin informacion";
    }

    public Long getCutFoil() {
        return cutFoil;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardId() {
        return cardId;
    }

    public Long getCardFoil() {
        return cardFoil;
    }

    public Long getBusStationId() {
        return busStationId;
    }

    public void setBusStationId(Long busStationId) {
        this.busStationId = busStationId;
    }

    public String getLocationIdDescription() {
        return locationIdDescription;
    }

    public void setLocationIdDescription(String locationIdDescription) {
        this.locationIdDescription = locationIdDescription;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getFileName() {
        return fileName;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Id Transaccion");
        headers.add("Id Tarjeta");
        headers.add("Folio Tarjeta");
        headers.add("Id dispositivo");
        headers.add("LocationId-NumeroEconomico");
        headers.add("Id Corredor");
        headers.add("Descripcion Error");
        headers.add("Nombre Archivo");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();
        orderValues.add(String.valueOf(transactionId));
        orderValues.add(cardId);
        orderValues.add(String.valueOf(cardFoil));
        orderValues.add(String.valueOf(busStationId));
        orderValues.add(locationIdDescription);
        orderValues.add(routeId);
        orderValues.add(errorDescription);
        orderValues.add(String.valueOf(fileName));
        return orderValues;
    }
}
