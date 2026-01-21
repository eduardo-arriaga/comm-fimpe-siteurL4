package com.idear.fimpe.cash.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CashReportRecord {

    private String busDescription;
    private String routeDescription;
    private String eurId;
    private LocalDate date;
    private Integer passengers;
    private Float amount;

    public CashReportRecord(String busDescription, String routeDescription, String eurId, LocalDate date, Integer passengers, Float amount) {
        this.busDescription = busDescription;
        this.routeDescription = routeDescription;
        this.eurId = eurId;
        this.date = date;
        this.passengers = passengers;
        this.amount = amount;
    }

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("Id SIR");
        headers.add("Corredor");
        headers.add("Autobus");
        headers.add("Fecha");
        headers.add("Pasajeros");
        headers.add("Monto");
        return headers;
    }

    public List<String> getOrderValues() {
        List<String> classOrderValues = new ArrayList<>();
        classOrderValues.add(eurId);
        classOrderValues.add(routeDescription);
        classOrderValues.add(busDescription);
        classOrderValues.add(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        classOrderValues.add(String.valueOf(passengers));
        classOrderValues.add(String.valueOf(amount));
        return classOrderValues;
    }
}
