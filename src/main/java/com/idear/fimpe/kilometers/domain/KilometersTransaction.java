package com.idear.fimpe.kilometers.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KilometersTransaction {

    private Long transactionId;
    private String vehicleId;
    private String routeDescription;
    private LocalDate date;
    private Double accumulatedKilometers;
    private Double traveledKilometers;
    private Double laps;

    public KilometersTransaction(Long transactionId, String vehicleId, String routeDescription, LocalDate date,
                                 Double accumulatedKilometers, Double traveledKilometers, Double laps) {
        this.transactionId = transactionId;
        this.vehicleId = routeDescription + "-U" + vehicleId;//MP-A01-R-UAR002
        this.routeDescription = routeDescription;
        this.date = date;
        this.accumulatedKilometers = accumulatedKilometers;
        this.traveledKilometers = traveledKilometers;
        this.laps = laps;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getAccumulatedKilometers() {
        return accumulatedKilometers;
    }

    public Double getTraveledKilometers() {
        return traveledKilometers;
    }

    public Double getLaps() {
        return laps;
    }

    public Long getTransactionId() {
        return transactionId;
    }
}
