package com.idear.fimpe.remoterecharge.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteRechargeNumberControl {

    private Long cutFoil;
    private LocalDateTime generationDateTime;
    private Integer totalRecords;
    private Float totalRecordAmount;
    private Integer totalRequestRecords;
    private Float totalRequestRecordAmount;
    private Integer totalConfirmationRecords;
    private Float totalConfirmationRecordAmount;
    private String technologicProviderId;
    private List<RemoteRechargeTransaction> requests;
    private List<RemoteRechargeTransaction> confirmations;

    public RemoteRechargeNumberControl() {
        totalRecords = 0;
        totalRecordAmount = 0F;
        totalRequestRecords = 0;
        totalRequestRecordAmount = 0F;
        totalConfirmationRecords = 0;
        totalConfirmationRecordAmount = 0F;
    }

    public void setCutFoil(Long cutFoil) {
        this.cutFoil = cutFoil;
    }

    public void setGenerationDateTime(LocalDateTime generationDateTime) {
        this.generationDateTime = generationDateTime;
    }

    public void setTechnologicProviderId(String technologicProviderId) {
        this.technologicProviderId = technologicProviderId;
    }

    public void setRequests(List<RemoteRechargeTransaction> requests) {
        this.requests = requests;
    }

    public void setConfirmations(List<RemoteRechargeTransaction> confirmations) {
        this.confirmations = confirmations;
    }

    public void calculate(){
        totalRecords = requests.size() + confirmations.size();
        totalRequestRecords = requests.size();
        totalConfirmationRecords = confirmations.size();

        totalRequestRecordAmount = requests.stream()
                .map( x-> x.getAmount())
                .collect(Collectors.summingDouble(Float::floatValue))
                .floatValue();

        totalConfirmationRecordAmount = confirmations.stream()
                .map( x-> x.getAmount())
                .collect(Collectors.summingDouble(Float::floatValue))
                .floatValue();

        totalRecordAmount = totalConfirmationRecordAmount + totalRequestRecordAmount;
    }

    public Long getCutFoil() {
        return cutFoil;
    }

    public LocalDateTime getGenerationDateTime() {
        return generationDateTime;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public Float getTotalRecordAmount() {
        return totalRecordAmount;
    }

    public Integer getTotalRequestRecords() {
        return totalRequestRecords;
    }

    public Float getTotalRequestRecordAmount() {
        return totalRequestRecordAmount;
    }

    public Integer getTotalConfirmationRecords() {
        return totalConfirmationRecords;
    }

    public Float getTotalConfirmationRecordAmount() {
        return totalConfirmationRecordAmount;
    }

    public String getTechnologicProviderId() {
        return technologicProviderId;
    }

    public List<RemoteRechargeTransaction> getRequests() {
        return requests;
    }

    public List<RemoteRechargeTransaction> getConfirmations() {
        return confirmations;
    }
}
