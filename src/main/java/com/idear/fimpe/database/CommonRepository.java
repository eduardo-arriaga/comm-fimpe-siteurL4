package com.idear.fimpe.database;

import java.sql.SQLException;

public interface CommonRepository {
    Long getFoilCut() throws SQLException;

    void insertFoilCut(Long folioCut, String deviceName, String deviceTable) throws SQLException;

    String getDeviceName(Long folioCut);
}
