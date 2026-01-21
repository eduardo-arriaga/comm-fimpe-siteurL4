package com.idear.fimpe.lam.domain;

import com.idear.fimpe.enums.LAMAction;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.idear.fimpe.enums.LAMAction.*;

public class LAMTransaction {

    private String serialCard;
    private LocalDateTime dateTimeDetection;
    private String cardTransactionCounter;
    private LAMAction action;

    public LAMTransaction(String serialCard, LocalDateTime dateTimeDetection, Long cardTransactionCounter, Integer action) {
        this.serialCard = serialCard;
        this.dateTimeDetection = dateTimeDetection;
        this.cardTransactionCounter = String.valueOf(cardTransactionCounter);
        this.action = setAction(action);
    }

    private LAMAction setAction(Integer action) {
        switch (action){
            case 1:
                return REACTIVATION;
            case 2:
                return DESACTIVATION;
            default:
                return BLOCKING;
        }
    }

    public String getSerialCard() {
        return serialCard;
    }

    public LocalDateTime getDateTimeDetection() {
        return dateTimeDetection;
    }

    public String getCardTransactionCounter() {
        return cardTransactionCounter;
    }

    public LAMAction getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LAMTransaction that = (LAMTransaction) o;
        return Objects.equals(serialCard, that.serialCard) && Objects.equals(cardTransactionCounter, that.cardTransactionCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialCard, cardTransactionCounter);
    }
}
