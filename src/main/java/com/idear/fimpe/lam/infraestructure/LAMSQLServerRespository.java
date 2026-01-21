package com.idear.fimpe.lam.infraestructure;

import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.lam.domain.LAMNumberControl;
import com.idear.fimpe.lam.domain.LAMRepository;
import com.idear.fimpe.lam.domain.LAMTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;

public class LAMSQLServerRespository implements LAMRepository {

    Logger logger = LoggerFactory.getLogger(LAMSQLServerRespository.class);

    @Override
    public List<LAMTransaction> getTransactionUnRequested() {
        String query = "SELECT " +
                "idTarjeta, " +
                "folioTarjeta, " +
                "fechaDeteccion, " +
                "accion " +
                "FROM wAccionTarjetas " +
                "WHERE solicitudLocal = 1 " +
                "AND detectada = 0 " +
                "AND estadoEnvio IN(?, ?)";

        List<LAMTransaction> lamTransactions = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String serialCard = resultSet.getString("idTarjeta");
                        Long cardTransactionCounter = resultSet.getLong("folioTarjeta") + 1;
                        LocalDateTime dateTimeDetection = resultSet.getTimestamp("fechaDeteccion").toLocalDateTime();
                        Integer action = resultSet.getInt("accion");
                        LAMTransaction lamTransaction = new LAMTransaction(serialCard, dateTimeDetection, cardTransactionCounter, action);
                        lamTransactions.add(lamTransaction);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("LAM- Error al intentar conseguir las tarjetas sin solicitar ", ex);
        }
        return lamTransactions;
    }

    @Override
    public List<LAMTransaction> getTransactionConfirmed() {
        String query = "SELECT " +
                "idTarjeta, " +
                "folioTarjeta, " +
                "fechaDeteccion, " +
                "accion " +
                "FROM wAccionTarjetas " +
                "WHERE solicitudExterna = 1 " +
                "AND detectada = 1 " +
                "AND estadoEnvio IN(?, ?)";

        List<LAMTransaction> lamTransactions = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String serialCard = resultSet.getString("idTarjeta");
                        Long cardTransactionCounter = resultSet.getLong("folioTarjeta");
                        LocalDateTime dateTimeDetection = resultSet.getTimestamp("fechaDeteccion").toLocalDateTime();
                        Integer action = resultSet.getInt("accion");
                        LAMTransaction lamTransaction = new LAMTransaction(serialCard, dateTimeDetection, cardTransactionCounter, action);
                        lamTransactions.add(lamTransaction);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("LAM- Error al intentar conseguir las tarjetas sin confirmar ", ex);
        }
        return lamTransactions;
    }

    @Override
    public Long getFolioCut() throws SQLException {
        String query = "SELECT " +
                "MAX(folioCorte) as folioCorte " +
                "FROM wAccionTarjetas";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong("folioCorte");
                    }
                }
            }
        }
        return 0L;
    }

    @Override
    public void updateTransactionSend(LAMNumberControl lamNumberControl) throws SQLException {
        String query = "UPDATE " +
                "wAccionTarjetas " +
                "SET " +
                "folioCorte = ?, " +
                "fechaEnvio = ?, " +
                "estadoEnvio = ? " +
                "WHERE idTarjeta = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                //La lista de confirmaciones se agrega a la de solicitudes para actualizar una sola lista
                lamNumberControl.getLamRequestTransactions().addAll(lamNumberControl.getLamConfirmationTransactions());
                for (LAMTransaction lamTransaction : lamNumberControl.getLamRequestTransactions()) {
                    preparedStatement.setLong(1, lamNumberControl.getFolioCut());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(lamNumberControl.getDateTimeSend()));
                    preparedStatement.setInt(3, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setString(4, lamTransaction.getSerialCard());
                    preparedStatement.addBatch();
                }
                int transactionsUpdated = preparedStatement.executeBatch().length;
                logger.info("Actualizacion de listas LAM como enviadas");
                logger.info("Fueron actualizadas {} de {}",transactionsUpdated, lamNumberControl.getLamRequestTransactions().size());
            }
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut) throws SQLException {
        String query = "UPDATE " +
                "wAccionTarjetas " +
                "SET " +
                "estadoEnvio = ? " +
                "WHERE folioCorte = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_OK.getValue());
                preparedStatement.setLong(2, folioCut);
                int recordsUpdate = preparedStatement.executeUpdate();
                logger.info("Procesando acuse .OK LAM");
                logger.info("{} transacciones LAM actualizadas del folio {} ", recordsUpdate, folioCut);
            }
        }
    }

    @Override
    public void updateErrorAckReceived(Long folioCut) throws SQLException {
        String query = "UPDATE " +
                "wAccionTarjetas " +
                "SET " +
                "estadoEnvio = ? " +
                "WHERE folioCorte = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_WITH_ERROR.getValue());
                preparedStatement.setLong(2, folioCut);
                int recordsUpdate = preparedStatement.executeUpdate();
                logger.info("Procesando acuse .ERR LAM");
                logger.info("{} transacciones LAM actualizadas del folio {}", recordsUpdate,  folioCut);
            }
        }
    }

    @Override
    public List<LAMTransaction> getAllLAMTransactions() throws SQLException {
        String query = "" +
                "SELECT " +
                "idTarjeta, fechaDeteccion, folioTarjeta, accion " +
                "FROM wAccionTarjetas " +
                "WHERE detectada = 0 " +
                "AND solicitudExterna = 1";

        List<LAMTransaction> lamTransactions = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String serialCard = resultSet.getString("idTarjeta");
                        LocalDateTime dateDetection = resultSet.getTimestamp("fechaDeteccion").toLocalDateTime();
                        Long folioTarjeta = resultSet.getLong("folioTarjeta");
                        Integer accion = resultSet.getInt("accion");

                        lamTransactions.add(new LAMTransaction(serialCard, dateDetection, folioTarjeta, accion));
                    }
                }
            }
        }
        logger.info("{} transacciones LAM en la tabla", lamTransactions.size());
        return lamTransactions;
    }

    @Override
    public void insertLAMTransactions(List<LAMTransaction> transactions) throws SQLException {
        String query =  "" +
                "INSERT INTO wAccionTarjetas " +
                "(idTarjeta, fechaDeteccion, folioTarjeta, accion, solicitudExterna) " +
                "VALUES(?, ?, ?, ?, 1)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (LAMTransaction lamTransaction: transactions) {
                    preparedStatement.setString(1, lamTransaction.getSerialCard());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(lamTransaction.getDateTimeDetection()));
                    preparedStatement.setLong(3, Long.parseLong(lamTransaction.getCardTransactionCounter()));
                    preparedStatement.setInt(4, lamTransaction.getAction().getKey());
                    preparedStatement.addBatch();
                }
                int recordsUpdate = preparedStatement.executeBatch().length;
                logger.info("{} transacciones LAM insertadas como nuevas peticiones", recordsUpdate);
            }
        }
    }

    @Override
    public void deleteLAMTransactions(List<LAMTransaction> transactions) throws SQLException {
        String query =  "" +
                "DELETE FROM wAccionTarjetas " +
                "WHERE idTarjeta = ? " +
                "AND folioTarjeta = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (LAMTransaction lamTransaction: transactions) {
                    preparedStatement.setString(1, lamTransaction.getSerialCard());
                    preparedStatement.setLong(2, Long.parseLong(lamTransaction.getCardTransactionCounter()));
                    preparedStatement.addBatch();
                }
                int recordsUpdate = preparedStatement.executeBatch().length;
                logger.info("{} transacciones LAM eliminadas", recordsUpdate);
            }
        }
    }
}
