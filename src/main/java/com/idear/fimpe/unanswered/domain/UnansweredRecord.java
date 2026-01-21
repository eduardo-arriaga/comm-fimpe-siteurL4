package com.idear.fimpe.unanswered.domain;

import com.idear.fimpe.helpers.dates.DateHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UnansweredRecord {

    private String recordType;
    private Long transactions;
    private LocalDateTime sendDate;
    private Long cutFoil;

    public UnansweredRecord(String recordType, Long transactions, LocalDateTime sendDate, Long cutFoil) {
        this.recordType = recordType;
        this.transactions = transactions;
        this.sendDate = sendDate;
        this.cutFoil = cutFoil;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Archivo-Dispositivo");
        headers.add("Total Transacciones");
        headers.add("Fecha Envio");
        headers.add("Folio Corte");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();
        orderValues.add(recordType);
        orderValues.add(String.valueOf(transactions));
        orderValues.add(DateHelper.getDateFormatted(sendDate));
        orderValues.add(String.valueOf(cutFoil));
        return orderValues;
    }

}
