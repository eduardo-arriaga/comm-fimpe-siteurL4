package com.idear.fimpe.vrt.domain;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface VRTRepository {

    List<VRTNumberControl> getStations();

    LocalDateTime getOldestTransactionDateNonExported(Long deviceId);

    List<VRTTransaction> getVRTTransactionsNonExported(Long deviceId, LocalDateTime startLimit, LocalDateTime endLimit);

    void updateTransactionsSent(VRTNumberControl vrtNumberControl)throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException;

    void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException;

    List<VRTReportRecord> getVRTReportRecord();

    void collectVRTReportRecordInfo(LocalDateTime startDate, LocalDateTime endDate, List<VRTReportRecord> vrtReportRecordList);
}
