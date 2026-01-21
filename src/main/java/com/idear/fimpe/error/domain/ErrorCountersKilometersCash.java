package com.idear.fimpe.error.domain;

import java.util.ArrayList;
import java.util.List;

public class ErrorCountersKilometersCash {

    private String fileType;
    private String economicNumber;
    private String routeDescription;
    private String errorDescription;
    private String fileErrorName;

    public ErrorCountersKilometersCash(String fileType, String economicNumber, String routeDescription, String errorDescription, String fileErrorName) {
        this.fileType = fileType;
        this.economicNumber = economicNumber;
        this.routeDescription = routeDescription;
        this.errorDescription = errorDescription;
        this.fileErrorName = fileErrorName;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Tipo de Archivo");
        headers.add("NumeroEconomico");
        headers.add("Id Corredor");
        headers.add("Descripcion Error");
        headers.add("Nombre Archivo");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();
        orderValues.add(fileType);
        orderValues.add(economicNumber);
        orderValues.add(routeDescription);
        orderValues.add(errorDescription);
        orderValues.add(fileErrorName);
        return orderValues;
    }
}
