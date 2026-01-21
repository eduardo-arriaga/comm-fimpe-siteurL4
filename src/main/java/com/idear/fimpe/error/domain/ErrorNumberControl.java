package com.idear.fimpe.error.domain;

import java.util.ArrayList;
import java.util.List;

public class ErrorNumberControl {

    private Long cutFoil;
    private String errorDescription;
    private String fileErrorName;

    public ErrorNumberControl(Long cutFoil, String errorDescription, String fileErrorName) {
        this.cutFoil = cutFoil;
        this.errorDescription = errorDescription;
        this.fileErrorName = fileErrorName;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Folio de Corte");
        headers.add("Descripcion Error");
        headers.add("Nombre Archivo");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();

        orderValues.add(String.valueOf(cutFoil));
        orderValues.add(errorDescription);
        orderValues.add(fileErrorName);
        return orderValues;
    }
}
