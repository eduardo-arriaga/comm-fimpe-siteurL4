package com.idear.fimpe.counters.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CountersTransaction {

    private Long transactionId;
    private String vehicleId;
    private String routeDescription;
    private LocalDate date;
    private Integer getOnNumbers;
    private Integer getOffNumbers;

    public CountersTransaction(Long transactionId, String vehicleId, String routeDescription, LocalDate date,
                               Integer getOnNumbers, Integer getOffNumbers) {

        this.transactionId = transactionId;
        this.vehicleId = routeDescription + "-U" + vehicleId;//MP-A01-R-UAR002
        this.routeDescription = routeDescription;
        this.date = date;
        this.getOnNumbers = getOnNumbers;
        this.getOffNumbers = getOffNumbers;
    }

    public Long getTransactionId() {
        return transactionId;
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

    public Integer getGetOnNumbers() {
        return getOnNumbers;
    }

    public Integer getGetOffNumbers() {
        return getOffNumbers;
    }
}
