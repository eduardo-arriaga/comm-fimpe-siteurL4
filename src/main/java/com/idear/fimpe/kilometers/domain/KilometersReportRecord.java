package com.idear.fimpe.kilometers.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KilometersReportRecord {

    private String routeDescription;
    private String eurId;
    private LocalDate date;
    private Double laps;
    private Double accumulatedKilometers;
    private Double traveledKilometers;

    public KilometersReportRecord(String routeDescription, String eurId, LocalDate date,
                                  Double laps, Double accumulatedKilometers, Double traveledKilometers) {

        this.routeDescription = routeDescription;
        this.eurId = eurId;
        this.date = date;
        this.laps = laps;
        this.accumulatedKilometers = accumulatedKilometers;
        this.traveledKilometers = traveledKilometers;
    }

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("Vueltas");
        headers.add("Kilometros Recorridos");
        headers.add("Kilometros Acumulados");
        headers.add("Fecha");
        headers.add("Corredor");
        headers.add("SIR");
        return headers;
    }

    public List<String> getOrderValues() {
        List<String> classOrderValues = new ArrayList<>();
        classOrderValues.add(String.valueOf(laps));
        classOrderValues.add(String.valueOf(accumulatedKilometers));
        classOrderValues.add(String.valueOf(traveledKilometers));
        classOrderValues.add(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        classOrderValues.add(routeDescription);
        classOrderValues.add(eurId);
        return classOrderValues;
    }
}
