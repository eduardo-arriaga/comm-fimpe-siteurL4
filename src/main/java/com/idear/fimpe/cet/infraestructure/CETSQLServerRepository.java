package com.idear.fimpe.cet.infraestructure;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.cet.domain.CETNumberControl;
import com.idear.fimpe.cet.domain.CETReportRecord;
import com.idear.fimpe.cet.domain.CETRepository;
import com.idear.fimpe.cet.domain.CETTransaction;
import com.idear.fimpe.database.SQLServerDatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.idear.fimpe.enums.BusRoute.*;
import static com.idear.fimpe.enums.FimpeStatus.*;
import static com.idear.fimpe.enums.OperationType.*;

public class CETSQLServerRepository implements CETRepository {
    Logger logger = LoggerFactory.getLogger(CETSQLServerRepository.class);

    @Override
    public LocalDateTime getOldestTransactionDateNonExported(OperationType operationType, OperationType operationTypeTwo) {
        String query = "" +
                "SELECT MIN(FechaHora) as FechaHora " +
                "FROM wTransTarjetas wtt " +
                "WHERE estado_respuesta_fimpe IN(?, ?) " +
                "AND TipoOperacion IN(?, ?)";
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                preparedStatement.setInt(3, operationType.getValue());
                preparedStatement.setInt(4, operationTypeTwo.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getTimestamp("FechaHora").toLocalDateTime();
                    }
                }
            }
        } catch (SQLException | NullPointerException ex) {
            logger.error("Error al intentar conseguir la fecha de la transaccion de debito/BPD mas vieja no enviada a FIMPE", ex);
        }
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime getOldestTransactionDateNonExported(OperationType operationType) {
        String query = "" +
                "SELECT MIN(FechaHora) " +
                "FROM wTransTarjetas wtt " +
                "WHERE estado_respuesta_fimpe IN(?, ?) " +
                "AND TipoOperacion = ?";
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                preparedStatement.setInt(3, operationType.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getTimestamp(1).toLocalDateTime();
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir la fecha de la transaccion de debito/BPD mas vieja no enviada a FIMPE", ex);
        } catch (NullPointerException ex) {
            logger.error("Sin fecha incial o no hay transacciones para enviar");
        }

        return LocalDateTime.now();
    }

    @Override
    public List<CETNumberControl> getBusAndRouteList(LocalDateTime startDate, LocalDateTime endDate) {
        List<CETNumberControl> cetNumberControlList = new ArrayList<>();
        String query = "" +
                "SELECT DISTINCT " +
                "t.Autobus, " +
                "t.NumRuta, " +
                "a.Descripcion, " +
                "r.idSIR, " +
                "r.idCorredor " +
                "FROM wTransTarjetas t " +
                "INNER JOIN cAutobuses a " +
                "ON t.Autobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON t.NumRuta = r.Numero " +
                "AND t.estado_respuesta_fimpe IN(?, ?) " +
                "AND r.idSistema = 2 " +
                "AND r.idSIR IS NOT NULL " +
                "AND r.idCorredor IS NOT NULL " +
                "AND FechaHora BETWEEN ? AND ? ";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                preparedStatement.setTimestamp(3, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(4, Timestamp.valueOf(endDate));

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Long busId = resultSet.getLong("Autobus");
                        Long routeId = resultSet.getLong("NumRuta");
                        String deviceId = resultSet.getString("Descripcion");
                        String eurId = resultSet.getString("idSIR");
                        String routeDescription = resultSet.getString("idCorredor");
                        cetNumberControlList.add(new CETNumberControl(busId, routeId, deviceId, routeDescription, eurId));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir autobuses - cet ", ex);
        }

        return cetNumberControlList;
    }

    @Override
    public List<CETTransaction> getDebitTransactions(CETNumberControl cetNumberControl, LocalDateTime startDate, LocalDateTime endDate) {
        List<CETTransaction> cetTransactionsList = new ArrayList<>();
        String query = "" +
                "SELECT " +
                "idTransaccion, " +
                "FechaHora, " +
                "idElectronico, " +
                "idProducto, " +
                "MontoEvento, " +
                "SaldoInicial, " +
                "Saldo, " +
                "bpds_inicial, " +
                "bpds_final, " +
                "idSAM, " +
                "ConsecutivoSAM, " +
                "FolioTarjeta, " +
                "TipoDebito " +
                "FROM wTransTarjetas " +
                "WHERE Autobus = ? " +
                "AND NumRuta = ? " +
                "AND FechaHora BETWEEN ? AND ? " +
                "AND TipoOperacion IN(?, ?) " +
                "AND estado_respuesta_fimpe IN(?, ?) " +
                "AND Clase NOT IN(9, 16, 50)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, cetNumberControl.getBusId());
                preparedStatement.setLong(2, cetNumberControl.getRouteId());
                preparedStatement.setTimestamp(3, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(4, Timestamp.valueOf(endDate));
                preparedStatement.setInt(5, DEBIT_OK_BPD_CET.getValue());
                preparedStatement.setInt(6, DEBIT_OK_CET.getValue());
                preparedStatement.setInt(7, NOT_SENT.getValue());
                preparedStatement.setInt(8, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransaccion");
                        LocalDateTime transactionDate = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String serialCard = resultSet.getString("idElectronico");
                        String product = resultSet.getString("idProducto");
                        Float transactionAmmount = Math.abs(resultSet.getFloat("MontoEvento"));
                        Float initialBalance = Math.abs(resultSet.getFloat("SaldoInicial"));
                        Float finalBalance = Math.abs(resultSet.getFloat("Saldo"));
                        Float initialBPD = resultSet.getFloat("bpds_inicial");
                        Float finalBPD = resultSet.getFloat("bpds_final");
                        String samId = resultSet.getString("idSAM");
                        String samTransactionCounter = resultSet.getString("ConsecutivoSAM");
                        Long cardTransactionCounter = resultSet.getLong("FolioTarjeta");
                        Integer debitType = resultSet.getInt("TipoDebito");

                        CETTransaction transaction = new CETTransaction(transactionId, transactionDate, serialCard,
                                product, transactionAmmount, initialBalance, finalBalance, initialBPD, finalBPD, samId, samTransactionCounter,
                                cardTransactionCounter, debitType);

                        cetTransactionsList.add(transaction);
                    }
                    return cetTransactionsList;
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir las transacciones de debito", ex);
        }
        return cetTransactionsList;
    }

    @Override
    public List<CETTransaction> getRechargeTransactions(CETNumberControl cetNumberControl, LocalDateTime startDate, LocalDateTime endDate) {
        String query = "" +
                "SELECT " +
                "idTransaccion, " +
                "FechaHora, " +
                "idElectronico, " +
                "idProducto, " +
                "MontoEvento, " +
                "SaldoInicial, " +
                "Saldo, " +
                "bpds_inicial, " +
                "bpds_final, " +
                "idSAM, " +
                "ConsecutivoSAM, " +
                "FolioTarjeta, " +
                "TipoDebito, " +
                "Autobus, " +
                "NumRuta " +
                "FROM wTransTarjetas " +
                "WHERE Autobus = ? " +
                "AND NumRuta = ? " +
                "AND FechaHora BETWEEN ? AND ? " +
                "AND TipoOperacion = ? " +
                "AND estado_respuesta_fimpe IN(?, ?) " +
                "AND Clase NOT IN(9, 16, 50) ";

        List<CETTransaction> transactionList = new ArrayList<>();

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, cetNumberControl.getBusId());
                preparedStatement.setLong(2, cetNumberControl.getRouteId());
                preparedStatement.setTimestamp(3, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(4, Timestamp.valueOf(endDate));
                preparedStatement.setInt(5, RECHARGE_OK_CET.getValue());
                preparedStatement.setInt(6, NOT_SENT.getValue());
                preparedStatement.setInt(7, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransaccion");
                        LocalDateTime transactionDate = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String serialCard = resultSet.getString("idElectronico");
                        String product = resultSet.getString("idProducto");
                        Float transactionAmmount = Math.abs(resultSet.getFloat("MontoEvento"));
                        Float initialBalance = Math.abs(resultSet.getFloat("SaldoInicial"));
                        Float finalBalance = Math.abs(resultSet.getFloat("Saldo"));
                        Float initialBPD = resultSet.getFloat("bpds_inicial");
                        Float finalBPD = resultSet.getFloat("bpds_final");
                        String samId = resultSet.getString("idSAM");
                        String samTransactionCounter = resultSet.getString("ConsecutivoSAM");
                        Long cardTransactionCounter = resultSet.getLong("FolioTarjeta");
                        Integer debitType = resultSet.getInt("TipoDebito");

                        CETTransaction transaction = new CETTransaction(transactionId, transactionDate, serialCard,
                                product, transactionAmmount, initialBalance, finalBalance, initialBPD, finalBPD, samId, samTransactionCounter,
                                cardTransactionCounter, debitType);

                        transactionList.add(transaction);
                    }
                    return transactionList;
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir las transacciones de debito", ex);
        }
        return transactionList;
    }

    @Override
    public void updateTransactionsSent(CETNumberControl cetNumberControl) throws SQLException {
        String query = "" +
                "UPDATE " +
                "wTransTarjetas " +
                "SET " +
                "folio_corte_fimpe = (?), " +
                "fecha_envio_fimpe = (?), " +
                "estado_respuesta_fimpe = (?) " +
                "WHERE idTransaccion = (?)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (CETTransaction cetTransaction : cetNumberControl.getCetTransactions()) {
                    preparedStatement.setLong(1, cetNumberControl.getCutId());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(cetNumberControl.getCutDate()));
                    preparedStatement.setInt(3, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setLong(4, cetTransaction.getTransactionId());
                    preparedStatement.addBatch();
                }
                int transactionsUpdated = preparedStatement.executeBatch().length;
                logger.info("{} transacciones actualizadas de {}", transactionsUpdated,  cetNumberControl.getCetTransactions().size());
            }
        } catch (SQLException ex) {
            throw new SQLException("El folio de corte no pudo ser obtenido", ex);
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException {
        String query = "" +
                "UPDATE " +
                "wTransTarjetas " +
                "SET " +
                "FechaFIMPE = ?, " +
                "archivo_fimpe = ?, " +
                "estado_respuesta_fimpe = ? " +
                "WHERE folio_corte_fimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(receivedDate));
                preparedStatement.setString(2, fileName);
                preparedStatement.setInt(3, SENT_OK.getValue());
                preparedStatement.setLong(4, folioCut);
                preparedStatement.execute();
            }
        }
    }

    @Override
    public void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException {
        String query = "" +
                "UPDATE " +
                "wTransTarjetas " +
                "SET " +
                "estado_respuesta_fimpe = ?," +
                "FechaFIMPE = ? " +
                "WHERE folio_corte_fimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, SENT_WITH_ERROR.getValue());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(receivedDate));
                preparedStatement.setLong(3, folioCut);
                preparedStatement.execute();
            }
        }
    }

    @Override
    public List<CETReportRecord> getCETReportRecords(LocalDateTime startDate, LocalDateTime endDate) {
        List<CETReportRecord> cetReportRecordList = new ArrayList<>();
        String query = "" +
                "SELECT " +
                "DISTINCT a.idAutobus, " +
                "a.Descripcion " +
                "FROM wTransTarjetas t " +
                "INNER JOIN cAutobuses a " +
                "ON a.idAutobus = t.Autobus " +
                "WHERE  t.fecha_envio_fimpe BETWEEN ? AND ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Long busId = resultSet.getLong("idAutobus");
                        String busDescription = resultSet.getString("Descripcion");
                        cetReportRecordList.add(new CETReportRecord(busId, busDescription));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("No se pudo conseguir informacion para el reporte del CET", ex);
        }
        return cetReportRecordList;
    }

    @Override
    public void collectCETReportRecordInfo(List<CETReportRecord> cetReportRecordList, LocalDateTime startDate, LocalDateTime endDate) {
        for (CETReportRecord cetReportRecord : cetReportRecordList) {
            collectCETReportRecordPerType(DEBIT_OK_CET, startDate, endDate, cetReportRecord);
            collectCETReportRecordPerType(RECHARGE_OK_CET, startDate, endDate, cetReportRecord);
        }
    }

    @Override
    public void updateTransactionsWithOutValidRoute() {
        String updateTransactions = "" +
                "UPDATE wTransTarjetas " +
                "SET NumRuta = ? " +
                "WHERE idTransaccion = ? ";

        String selectTransactions = "" +
                "SELECT t.idTransaccion, a.Descripcion " +
                "FROM wTransTarjetas t " +
                "INNER JOIN cAutobuses a " +
                "ON t.Autobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON t.NumRuta = r.Numero " +
                "AND t.estado_respuesta_fimpe IN(?, ?) " +
                "AND t.TipoOperacion IN(?, ?, ?) " +
                "AND t.NumRuta IN(?, ?, ?, ?) " +
                "AND r.idSistema = 2";

        Map<Long, String> transactions = new HashMap<>();

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){

            try(PreparedStatement preparedStatement = connection.prepareStatement(selectTransactions)){

                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                preparedStatement.setInt(3, DEBIT_OK_BPD_CET.getValue());
                preparedStatement.setInt(4, DEBIT_OK_CET.getValue());
                preparedStatement.setInt(5, RECHARGE_OK_CET.getValue());
                preparedStatement.setInt(6, T01.getRouteId());
                preparedStatement.setInt(7, T02.getRouteId());
                preparedStatement.setInt(8, T03.getRouteId());
                preparedStatement.setInt(9, T02A.getRouteId());

                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        Long transactionId = resultSet.getLong("idTransaccion");
                        String busDescription = resultSet.getString("Descripcion");
                        transactions.put(transactionId, busDescription);
                    }
                }

            }

            try(PreparedStatement preparedStatement = connection.prepareStatement(updateTransactions)){
                for (Map.Entry<Long, String> transaction: transactions.entrySet()) {
                    //Identifica que ruta le va a asignar segun su prefijo en la descripcion
                    //Si es TP-112 entonces la ruta es la 19, si es TR-62 entonces la ruta 20
                    if(transaction.getValue().contains("P"))
                        preparedStatement.setInt(1, NI_P.getRouteId());
                    if(transaction.getValue().contains("R"))
                        preparedStatement.setInt(1, NI_R.getRouteId());

                    preparedStatement.setLong(2, transaction.getKey());

                    preparedStatement.addBatch();
                }

                preparedStatement.executeBatch();
            }
        }catch (SQLException ex){
            logger.error("Error al intentar actualizar las transacciones del CET que se encuentran en troncales " +
                    "y deben de estar en el corredor NI", ex);
        }
    }

    private void collectCETReportRecordPerType(OperationType operationType,
                                               LocalDateTime startDate, LocalDateTime endDate,
                                               CETReportRecord cetReportRecord) {
        String query = "" +
                "SELECT " +
                "COUNT(*) as transacciones,  " +
                "estado_respuesta_fimpe  " +
                "FROM wTransTarjetas " +
                "WHERE FechaFIMPE BETWEEN ? AND ? " +
                "AND TipoOperacion IN(?) " +
                "AND estado_respuesta_fimpe IN(?, ?) " +
                "AND Autobus = ? " +
                "GROUP BY estado_respuesta_fimpe";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, operationType.getValue());
                preparedStatement.setInt(4, SENT_OK.getValue());
                preparedStatement.setInt(5, SENT_WITH_ERROR.getValue());
                preparedStatement.setLong(6, cetReportRecord.getBusId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int transactions = resultSet.getInt("transacciones");
                        int fimpeStatus = resultSet.getInt("estado_respuesta_fimpe");

                        if (fimpeStatus == SENT_OK.getValue()) {
                            if (operationType == DEBIT_OK_CET) {
                                cetReportRecord.setDebitRecordsOk(transactions);
                            } else {
                                cetReportRecord.setRechargeRecordsOk(transactions);
                            }
                        }
                        if (fimpeStatus == SENT_WITH_ERROR.getValue()) {
                            if (operationType == DEBIT_OK_CET) {
                                cetReportRecord.setDebitRecordsError(transactions);
                            } else {
                                cetReportRecord.setRechargeRecordsError(transactions);
                            }
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            logger.error("No se pudo establecer conexion con la Base de datos", ex);
        }
    }
}
