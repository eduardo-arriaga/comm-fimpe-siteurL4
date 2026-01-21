package com.idear.fimpe.cet.domain;

import com.idear.fimpe.enums.OperationType;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface CETRepository {

    LocalDateTime getOldestTransactionDateNonExported(OperationType operationType);

    LocalDateTime getOldestTransactionDateNonExported(OperationType operationType, OperationType operationTypeTwo);

    List<CETNumberControl> getBusAndRouteList(LocalDateTime startDate, LocalDateTime endDate);

    List<CETTransaction> getDebitTransactions(CETNumberControl cetNumberControl, LocalDateTime startDate, LocalDateTime endDate);

    List<CETTransaction> getRechargeTransactions(CETNumberControl cetNumberControl, LocalDateTime startDate, LocalDateTime endDate);

    void updateTransactionsSent(CETNumberControl cetNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException;

    void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException;

    List<CETReportRecord> getCETReportRecords(LocalDateTime startDate, LocalDateTime endDate);

    void collectCETReportRecordInfo(List<CETReportRecord> cetReportRecordList, LocalDateTime startDate, LocalDateTime endDate);

    void updateTransactionsWithOutValidRoute();
}
