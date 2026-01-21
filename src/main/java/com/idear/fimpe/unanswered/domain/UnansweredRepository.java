package com.idear.fimpe.unanswered.domain;

import java.sql.SQLException;
import java.util.List;

public interface UnansweredRepository {

    List<UnansweredRecord> getUnansweredRecordsCET() throws SQLException;
    List<UnansweredRecord> getUnansweredRecordsVRT() throws SQLException;
    List<UnansweredRecord> getUnansweredRecordsTorniquete() throws SQLException;
    List<UnansweredRecord> getUnansweredRecordsCash() throws SQLException;
    List<UnansweredRecord> getUnansweredRecordsCounters() throws SQLException;
    List<UnansweredRecord> getUnansweredRecordsKilometers() throws SQLException;
}
