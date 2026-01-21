package com.idear.fimpe.cash.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CashTransaction {

    private String vehicleId;
    private String originalVehicleId;
    private LocalDate date;
    private Float amount;
    private Integer totalPassengers;

    public CashTransaction(String vehicleId, LocalDate date, Float amount, Integer totalPassengers, String routeDescription) {
        this.originalVehicleId = vehicleId;
        this.vehicleId = routeDescription + "-U" + vehicleId;//R08-U23001
        this.date = date;
        this.amount = amount;
        this.totalPassengers = totalPassengers;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getOriginalVehicleId() {
        return originalVehicleId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Integer getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(Integer totalPassengers) {
        this.totalPassengers = totalPassengers;
    }
}
