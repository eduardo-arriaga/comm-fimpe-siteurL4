package com.idear.fimpe.counters.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CountersReportRecord {

    private String busDescription;
    private String routeDescription;
    private LocalDate date;
    private Integer getOnNumbers;
    private Integer getOffNumbers;

    public CountersReportRecord(String busDescription, String routeDescription, LocalDate date,
                                Integer getOnNumbers, Integer getOffNumbers) {

        this.busDescription = busDescription;
        this.routeDescription = routeDescription;
        this.date = date;
        this.getOnNumbers = getOnNumbers;
        this.getOffNumbers = getOffNumbers;
    }

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("Autobus");
        headers.add("Corredor");
        headers.add("Fecha");
        headers.add("Subidas");
        headers.add("Bajadas");
        return headers;
    }

    public List<String> getOrderValues() {
        List<String> classOrderValues = new ArrayList<>();
        classOrderValues.add(busDescription);
        classOrderValues.add(routeDescription);
        classOrderValues.add(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        classOrderValues.add(String.valueOf(getOnNumbers));
        classOrderValues.add(String.valueOf(getOffNumbers));
        return classOrderValues;
    }
}
