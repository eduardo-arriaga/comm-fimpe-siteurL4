package com.idear.fimpe.counters.domain;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface CountersSQLRepository {

    List<CountersNumberControl> getCountersNumberControls(LocalDateTime startDate, LocalDateTime endDate);

    Long getCutFolio() throws SQLException;

    void updateCountersNumberControlAsSent(CountersNumberControl countersNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException;

    void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException;

    List<CountersReportRecord> getCountersReportRecord(LocalDateTime starDate, LocalDateTime endDate);
}
