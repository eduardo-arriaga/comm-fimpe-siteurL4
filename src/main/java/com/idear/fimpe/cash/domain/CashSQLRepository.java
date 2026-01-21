package com.idear.fimpe.cash.domain;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface CashSQLRepository {

    List<CashNumberControl> getCashNumberControls(LocalDateTime startDate, LocalDateTime endDate);

    Long getCutFolio() throws SQLException;

    void updateCashNumberControlAsSent(CashNumberControl cashNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException;

    void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws  SQLException;

    List<CashReportRecord> getCashReportRecord(LocalDateTime startDate, LocalDateTime endDate);

    void updateTransactionsWithOutValidRoute();
}
