package com.idear.fimpe.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLServerCommonRepository implements CommonRepository {
    private Logger logger = LoggerFactory.getLogger(SQLServerCommonRepository.class);

    @Override
    public Long getFoilCut() throws SQLException{
        String query =  "SELECT MAX(folioCorteId) as folio FROM wFimpeFolioCortes";
        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        return resultSet.getLong("folio") + 1;
                    }
                }
            }
        }catch(SQLException ex){
            throw new SQLException("El folio de corte no pudo ser obtenido", ex);
        }
        return 0L;
    }

    @Override
    public void insertFoilCut(Long folioCut, String deviceName, String deviceTable) throws SQLException {
        String query =  "INSERT INTO " +
                "wFimpeFolioCortes " +
                "(folioCorteId, nombreDispositivo, nombreTabla) " +
                "VALUES(?, ?, ?)";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setLong(1, folioCut);
                preparedStatement.setString(2, deviceName);
                preparedStatement.setString(3, deviceTable);
                preparedStatement.execute();
            }
        }catch(SQLException ex){
            throw new SQLException("El folio de corte no pudo ser insertado", ex);
        }
    }

    /**
     * Obtiene el nombre del dispositivo por medio del folio de corte
     * @param folioCut Folio de corte
     * @return Nombre del archivo
     */
    @Override
    public String getDeviceName(Long folioCut) {
        String query =  "SELECT nombreDispositivo " +
                "FROM wFimpeFolioCortes wtt " +
                "WHERE folioCorteId =? ";
        String deviceName = "";
        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
                preparedStatement.setLong(1, folioCut);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        deviceName = resultSet.getString("nombreDispositivo");
                    }
                }
            }
        }catch(SQLException ex){
            logger.error("Error al intentar conseguir el nombre del dispositivo en la tabla de folios de corte, " +
                    "folio de corte: " + folioCut, ex);
        }
        return deviceName;
    }
}
