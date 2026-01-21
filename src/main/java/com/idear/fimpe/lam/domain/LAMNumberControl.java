package com.idear.fimpe.lam.domain;

import java.time.LocalDateTime;
import java.util.List;

public class LAMNumberControl {
    private Long folioCut;
    private LocalDateTime dateTimeSend;
    private Integer totalRecords;
    private List<LAMTransaction> lamRequestTransactions;
    private List<LAMTransaction> lamConfirmationTransactions;

    public Long getFolioCut() {
        return folioCut;
    }

    public void setFolioCut(Long folioCut) {
        this.folioCut = folioCut;
    }

    public LocalDateTime getDateTimeSend() {
        return dateTimeSend;
    }

    public void setDateTimeSend(LocalDateTime dateTimeSend) {
        this.dateTimeSend = dateTimeSend;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public List<LAMTransaction> getLamRequestTransactions() {
        return lamRequestTransactions;
    }

    public void setLamRequestTransactions(List<LAMTransaction> lamRequestTransactions) {
        this.lamRequestTransactions = lamRequestTransactions;
    }

    public List<LAMTransaction> getLamConfirmationTransactions() {
        return lamConfirmationTransactions;
    }

    public void setLamConfirmationTransactions(List<LAMTransaction> lamConfirmationTransactions) {
        this.lamConfirmationTransactions = lamConfirmationTransactions;
    }
}
