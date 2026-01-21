package com.idear.fimpe.kilometers.domain;



import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface KilometersSQLRepository {

    List<KilometersNumberControl> getKilometersNumberControls(LocalDateTime startDate, LocalDateTime endDate);

    Long getCutFolio() throws SQLException;

    void updateKilometersNumberControlAsSent(KilometersNumberControl kilometersNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException;

    void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException;

    List<KilometersReportRecord> getKilometersReportRecord(LocalDateTime starDate, LocalDateTime endDate);
}
