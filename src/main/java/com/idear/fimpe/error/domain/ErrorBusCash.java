package com.idear.fimpe.error.domain;

import java.util.ArrayList;
import java.util.List;

public class ErrorBusCash {

    private String economicNumber;
    private String routeDescription;
    private String errorDescription;
    private String fileErrorName;

    public ErrorBusCash(String economicNumber, String routeDescription, String errorDescription, String fileErrorName) {
        this.economicNumber = economicNumber;
        this.routeDescription = routeDescription;
        this.errorDescription = errorDescription;
        this.fileErrorName = fileErrorName;
    }

    public String getEconomicNumber() {
        return economicNumber;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getFileErrorName() {
        return fileErrorName;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("NumeroEconomico");
        headers.add("Id Corredor");
        headers.add("Descripcion Error");
        headers.add("Nombre Archivo");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();

        orderValues.add(economicNumber);
        orderValues.add(routeDescription);
        orderValues.add(errorDescription);
        orderValues.add(fileErrorName);
        return orderValues;
    }
}
