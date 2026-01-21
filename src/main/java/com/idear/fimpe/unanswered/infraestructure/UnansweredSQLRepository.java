package com.idear.fimpe.unanswered.infraestructure;

import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.unanswered.domain.UnansweredRecord;
import com.idear.fimpe.unanswered.domain.UnansweredRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;


public class UnansweredSQLRepository implements UnansweredRepository {

    @Override
    public List<UnansweredRecord> getUnansweredRecordsCET() throws SQLException {

        String query = "" +
                "SELECT COUNT(*) as transacciones, fecha_envio_fimpe, folio_corte_fimpe " +
                "FROM wTransTarjetas " +
                "WHERE estado_respuesta_fimpe = ? " +
                "GROUP BY folio_corte_fimpe, fecha_envio_fimpe";

        List<UnansweredRecord> unansweredRecords = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactions = resultSet.getLong("transacciones");
                        LocalDateTime sendDate = resultSet.getTimestamp("fecha_envio_fimpe").toLocalDateTime();
                        Long cutFoil = resultSet.getLong("folio_corte_fimpe");

                        unansweredRecords.add(new UnansweredRecord("CET", transactions, sendDate, cutFoil));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al intentar obtener las transacciones pendientes del CET", ex);
        }
        return unansweredRecords;
    }

    @Override
    public List<UnansweredRecord> getUnansweredRecordsVRT() throws SQLException {
        String query = "" +
                "SELECT COUNT(*) as transacciones, t.fecha_envio_fimpe, t.folio_corte_fimpe " +
                "FROM cDispositivos d " +
                "INNER JOIN cEstaciones e " +
                "ON d.idEstacion = e.idEstacion " +
                "INNER JOIN wTransAbonoDisp t " +
                "ON t.idDispositivo = d.idDispositivo " +
                "AND d.idTipoDispositivo = 2 " +
                "AND e.estaActivo = 1 " +
                "AND t.estado_respuesta_fimpe = ? " +
                "GROUP BY t.folio_corte_fimpe, t.fecha_envio_fimpe";

        List<UnansweredRecord> unansweredRecords = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactions = resultSet.getLong("transacciones");
                        LocalDateTime sendDate = resultSet.getTimestamp("fecha_envio_fimpe").toLocalDateTime();
                        Long cutFoil = resultSet.getLong("folio_corte_fimpe");

                        unansweredRecords.add(new UnansweredRecord("VRT", transactions, sendDate, cutFoil));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al intentar obtener las transacciones pendientes de VRT", ex);
        }
        return unansweredRecords;
    }

    @Override
    public List<UnansweredRecord> getUnansweredRecordsTorniquete() throws SQLException {
        String query = "" +
                "SELECT COUNT(*) as transacciones, t.fecha_envio_fimpe, t.folio_corte_fimpe " +
                "FROM  wTransAbonoDisp t " +
                "INNER JOIN cDispositivos d " +
                "ON t.idDispositivo  = d.idDispositivo " +
                "AND d.idTipoDispositivo = 3 " +
                "AND t.estado_respuesta_fimpe = ? " +
                "GROUP BY t.folio_corte_fimpe, t.fecha_envio_fimpe;";

        List<UnansweredRecord> unansweredRecords = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactions = resultSet.getLong("transacciones");
                        LocalDateTime sendDate = resultSet.getTimestamp("fecha_envio_fimpe").toLocalDateTime();
                        Long cutFoil = resultSet.getLong("folio_corte_fimpe");

                        unansweredRecords.add(new UnansweredRecord("TORNIQUETE", transactions, sendDate, cutFoil));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al intentar obtener las transacciones pendientes del Torniquete", ex);
        }
        return unansweredRecords;
    }

    @Override
    public List<UnansweredRecord> getUnansweredRecordsCash() throws SQLException{
        String query = "" +
                "SELECT COUNT(*) as transacciones, FechaEnvioFimpe , FolioCorteFimpe " +
                "FROM wTransMonedas " +
                "WHERE EstadoRespuestaFimpe = ? " +
                "GROUP BY FolioCorteFimpe , FechaEnvioFimpe";

        List<UnansweredRecord> unansweredRecords = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactions = resultSet.getLong("transacciones");
                        LocalDateTime sendDate = resultSet.getTimestamp("FechaEnvioFimpe").toLocalDateTime();
                        Long cutFoil = resultSet.getLong("FolioCorteFimpe");

                        unansweredRecords.add(new UnansweredRecord("EFECTIVO", transactions, sendDate, cutFoil));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al intentar obtener las transacciones pendientes de Efectivo", ex);
        }
        return unansweredRecords;
    }

    @Override
    public List<UnansweredRecord> getUnansweredRecordsCounters() throws SQLException {
        String query = "" +
                "SELECT COUNT(*) as transacciones, FechaEnvioFimpe , FolioCorteFimpe " +
                "FROM wContadoresAutobus " +
                "WHERE EstadoRespuestaFimpe = ? " +
                "GROUP BY FolioCorteFimpe , FechaEnvioFimpe";

        List<UnansweredRecord> unansweredRecords = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactions = resultSet.getLong("transacciones");
                        LocalDateTime sendDate = resultSet.getTimestamp("FechaEnvioFimpe").toLocalDateTime();
                        Long cutFoil = resultSet.getLong("FolioCorteFimpe");

                        unansweredRecords.add(new UnansweredRecord("CONTADORES", transactions, sendDate, cutFoil));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al intentar obtener las transacciones pendientes de Contadores", ex);
        }
        return unansweredRecords;
    }

    @Override
    public List<UnansweredRecord> getUnansweredRecordsKilometers() throws SQLException {
        String query = "" +
                "SELECT COUNT(*) as transacciones, FechaEnvioFimpe , FolioCorteFimpe " +
                "FROM wKmAutobus " +
                "WHERE EstadoRespuestaFimpe = ? " +
                "GROUP BY FolioCorteFimpe , FechaEnvioFimpe";

        List<UnansweredRecord> unansweredRecords = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactions = resultSet.getLong("transacciones");
                        LocalDateTime sendDate = resultSet.getTimestamp("FechaEnvioFimpe").toLocalDateTime();
                        Long cutFoil = resultSet.getLong("FolioCorteFimpe");

                        unansweredRecords.add(new UnansweredRecord("KILOMETROS", transactions, sendDate, cutFoil));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al intentar obtener las transacciones pendientes de Kilometros", ex);
        }
        return unansweredRecords;
    }
}
