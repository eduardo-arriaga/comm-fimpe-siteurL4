package com.idear.fimpe.error.domain;

import com.idear.fimpe.helpers.dates.DateHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErrorTransaction {

    private Long cutFoil;
    private Long transactionId;
    private String cardId;
    private Long cardFoil;
    private LocalDateTime transactionDate;
    private String productId;
    private Integer cardClass;
    private String errorDescription;
    private String fileName;

    public ErrorTransaction(Long cutFoil, String cardId, Long cardFoil, String errorDescription, String fileName) {
        this.cutFoil = cutFoil;
        this.cardId = cardId;
        this.cardFoil = cardFoil;
        this.errorDescription = errorDescription;
        this.fileName = fileName;
        transactionId = 0L;
        cardClass = 0;
        transactionDate = LocalDateTime.now();
        productId = "Sin informacion";
    }

    public Long getCutFoil() {
        return cutFoil;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardId() {
        return cardId;
    }

    public Long getCardFoil() {
        return cardFoil;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setCardClass(Integer cardClass) {
        this.cardClass = cardClass;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getFileName() {
        return fileName;
    }

    public static List<String> getHeaders(){
        List<String> headers = new ArrayList<>();
        headers.add("Id Transaccion");
        headers.add("Id Tarjeta");
        headers.add("Folio Tarjeta");
        headers.add("Fecha Transaccion ");
        headers.add("Id Producto");
        headers.add("Clase");
        headers.add("Descripcion Error");
        headers.add("Nombre Archivo");
        return headers;
    }

    public List<String> getOrderValues(){
        List<String> orderValues = new ArrayList<>();
        orderValues.add(String.valueOf(transactionId));
        orderValues.add(cardId);
        orderValues.add(String.valueOf(cardFoil));
        orderValues.add(DateHelper.getDateFormatted(transactionDate));
        orderValues.add(productId);
        orderValues.add(String.valueOf(cardClass));
        orderValues.add(errorDescription);
        orderValues.add(fileName);
        return orderValues;
    }
}