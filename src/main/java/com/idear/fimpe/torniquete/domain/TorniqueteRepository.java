package com.idear.fimpe.torniquete.domain;

import com.idear.fimpe.enums.OperationType;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface TorniqueteRepository {

    LocalDateTime getOldestTransactionDateNonExported(OperationType operationType, OperationType operationTypeTwo, OperationType operationTypeThree);

    List<TorniquteNumberControl> getTorniqueteDevices();

   List<TorniqueteTransaction> getNonExportedTransaction(String deviceId, LocalDateTime startDate, LocalDateTime endDate);

    void updateTransactionsTorniquete(TorniquteNumberControl torniquteNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException;

    void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws  SQLException;

    List<TorniqueteReportRecord> getTorniqueteReportRecord();

    void collectTorniqueteRecordsReport(LocalDateTime startDate, LocalDateTime endDate, List<TorniqueteReportRecord> torniqueteReportRecordList);
}
