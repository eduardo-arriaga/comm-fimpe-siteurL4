package com.idear.fimpe.vrt.infraestructure;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.Product;
import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.vrt.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;
import static com.idear.fimpe.enums.OperationType.*;

public class VRTSQLServerRepository implements VRTRepository {
    private Logger logger = LoggerFactory.getLogger(VRTSQLServerRepository.class);

    @Override
    public List<VRTNumberControl> getStations() {
        String query = "SELECT " +
                "d.idDispositivo," +
                "d.Serie, " +
                "e.idCorredor, " +
                "e.idSIR, " +
                "e.idEstacionFimpe " +
                "FROM cDispositivos d " +
                "INNER JOIN cEstaciones e " +
                "ON d.idEstacion = e.idEstacion " +
                "AND d.idTipoDispositivo = 2 " +
                "AND e.estaActivo = 1 " +
                "AND e.idSIR IS NOT NULL " +
                "AND e.idCorredor IS NOT NULL " +
                "ORDER BY e.idEstacion";

        List<VRTNumberControl> vrtNumberControlList = new ArrayList<>();
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Long deviceId = resultSet.getLong("idDispositivo");
                        String deviceIdFimpe = resultSet.getString("Serie");
                        String routeId = resultSet.getString("idCorredor");
                        String eurId = resultSet.getString("idSIR");
                        String stationId = resultSet.getString("idEstacionFimpe");
                        VRTNumberControl vrtNumberControl = new VRTNumberControl(eurId, routeId, deviceIdFimpe, deviceId, stationId);
                        vrtNumberControlList.add(vrtNumberControl);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir las VRT de las estaciones", ex);
        }
        return vrtNumberControlList;
    }

    @Override
    public LocalDateTime getOldestTransactionDateNonExported(Long deviceId) {
        String query = "SELECT " +
                "MIN(FechaHora) as FechaHora " +
                "FROM wTransAbonoDisp " +
                "WHERE  idDispositivo = ? " +
                "AND TipoOperacion IN(?, ?, ?) " +
                "AND estado_respuesta_fimpe IN(?, ?)";
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, deviceId);
                preparedStatement.setInt(2, RECHARGE_OK_VRT.getValue());
                preparedStatement.setInt(3, RECHARGE_QR_OK_VRT.getValue());
                preparedStatement.setInt(4, SELL_OK_VRT.getValue());
                preparedStatement.setInt(5, NOT_SENT.getValue());
                preparedStatement.setInt(6, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getTimestamp("FechaHora").toLocalDateTime();
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir la fecha de la transaccion mas vieja no enviada de las estaciones", ex);
        } catch (NullPointerException ex) {
            logger.error("Sin fecha inicial o no hay transacciones para enviar");
        }
        return LocalDateTime.now();
    }

    @Override
    public List<VRTTransaction> getVRTTransactionsNonExported(Long deviceId, LocalDateTime startLimit, LocalDateTime endLimit) {
        String query = "SELECT  " +
                "idTransAbono, " +
                "FechaHora, " +
                "CodigoFisico, " +
                "idHoozie, " +
                "MontoEvento, " +
                "idSAM, " +
                "Clase, " +
                "inicioValidezProductoCredito, " +
                "finValidezProductoCredito, " +
                "inicioValidezProductoMonedero, " +
                "finValidezProductoMonedero, " +
                "idProducto, " +
                "SaldoInicial, " +
                "SaldoFinal, " +
                "ContFinalSAM, " +
                "FolioTarjeta, " +
                "TipoDebito, " +
                "TipoOperacion " +
                "FROM wTransAbonoDisp  " +
                "WHERE idDispositivo = ? " +
                "AND FechaHora BETWEEN ? AND ? " +
                "AND TipoOperacion IN(?, ?, ?, ?, ?) " +
                "AND estado_respuesta_fimpe IN(?, ?) " +
                "AND MontoEvento > 0";//Para no traerse las recargas en 0 despues de vender

        List<VRTTransaction> vrtTransactions = new ArrayList<>();
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, deviceId);
                preparedStatement.setTimestamp(2, Timestamp.valueOf(startLimit));
                preparedStatement.setTimestamp(3, Timestamp.valueOf(endLimit));
                preparedStatement.setInt(4, RECHARGE_OK_VRT.getValue());
                preparedStatement.setInt(5, REMOTE_RECHARGE_OK_VRT.getValue());
                preparedStatement.setInt(6, RECHARGE_QR_OK_VRT.getValue());
                preparedStatement.setInt(7, RECHARGE_QR_OK_COMMISSION_VRT.getValue());
                preparedStatement.setInt(8, SELL_OK_VRT.getValue());
                preparedStatement.setInt(9, NOT_SENT.getValue());
                preparedStatement.setInt(10, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        VRTTransaction vrtTransaction;
                        Integer operationType = resultSet.getInt("TipoOperacion");
                        Long transactionId = resultSet.getLong("idTransAbono");
                        LocalDateTime transactionDate = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String serialCard = resultSet.getString("CodigoFisico");
                        Float transactionAmmount = resultSet.getFloat("MontoEvento");
                        Float initialBalance = Math.abs(resultSet.getFloat("SaldoInicial"));
                        Float finalBalance = Math.abs(resultSet.getFloat("SaldoFinal"));
                        String samId = resultSet.getString("idSAM");
                        Long cardTransactionCounter = resultSet.getLong("FolioTarjeta");

                        if (operationType == RECHARGE_OK_VRT.getValue() ||
                                operationType == REMOTE_RECHARGE_OK_VRT.getValue()) {

                            String productId = resultSet.getString("idProducto");
                            String samTransactionCounter = resultSet.getString("ContFinalSAM");
                            Integer rechargeType = resultSet.getInt("TipoDebito");

                            vrtTransaction = new VRTTransaction(transactionId, RECHARGE_OK_VRT, transactionDate,
                                    serialCard, transactionAmmount, samId, productId, initialBalance, finalBalance,
                                    samTransactionCounter, cardTransactionCounter, rechargeType);

                            vrtTransactions.add(vrtTransaction);

                        } else if (operationType.equals(SELL_OK_VRT.getValue())) {
                            String profile = String.valueOf(resultSet.getInt("Clase"));
                            LocalDateTime creditStartDateTime = resultSet.getTimestamp("inicioValidezProductoCredito").toLocalDateTime();
                            LocalDateTime creditEndDateTime = resultSet.getTimestamp("finValidezProductoCredito").toLocalDateTime();
                            LocalDateTime moneyStartDateTime = resultSet.getTimestamp("inicioValidezProductoMonedero").toLocalDateTime();
                            LocalDateTime moneyEndDateTime = resultSet.getTimestamp("finValidezProductoMonedero").toLocalDateTime();
                            ProductSale creditProductSale = new ProductSale(
                                    Product.CREDIT,
                                    creditStartDateTime.toLocalDate(),
                                    creditEndDateTime.toLocalDate(),
                                    creditStartDateTime.toLocalTime(),
                                    creditEndDateTime.toLocalTime());

                            ProductSale moneyProductSale = new ProductSale(
                                    Product.DEBIT,
                                    moneyStartDateTime.toLocalDate(),
                                    moneyEndDateTime.toLocalDate(),
                                    moneyStartDateTime.toLocalTime(),
                                    moneyEndDateTime.toLocalTime());

                            vrtTransaction = new VRTTransaction(transactionId, SELL_OK_VRT, transactionDate, serialCard,
                                    cardTransactionCounter, transactionAmmount, samId, profile, creditProductSale, moneyProductSale);

                            vrtTransactions.add(vrtTransaction);
                        } else if (operationType.equals(RECHARGE_QR_OK_VRT.getValue()) ||
                                operationType.equals(RECHARGE_QR_OK_COMMISSION_VRT.getValue())) {

                            serialCard = resultSet.getString("idHoozie");
                            if (serialCard == null)
                                continue;

                            vrtTransaction = new VRTTransaction(transactionId, OperationType.getOperationType(operationType),
                                    transactionDate, serialCard, finalBalance);

                            vrtTransactions.add(vrtTransaction);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir las transacciones de la VRT ", ex);
        }
        return vrtTransactions;
    }

    @Override
    public void updateTransactionsSent(VRTNumberControl vrtNumberControl) throws SQLException {
        String query = "UPDATE " +
                "wTransAbonoDisp " +
                "SET " +
                "folio_corte_fimpe = (?), " +
                "fecha_envio_fimpe = (?), " +
                "estado_respuesta_fimpe = (?) " +
                "WHERE idTransAbono = (?)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (VRTTransaction vrtTransaction : vrtNumberControl.getVrtTransactions()) {
                    preparedStatement.setLong(1, vrtNumberControl.getCutId());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(vrtNumberControl.getCutDate()));
                    preparedStatement.setInt(3, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setLong(4, vrtTransaction.getTransactionId());
                    preparedStatement.addBatch();
                }
                int transactionsUpdated = preparedStatement.executeBatch().length;
                logger.info("{} transacciones actualizadas de {}", transactionsUpdated, vrtNumberControl.getVrtTransactions().size());
            }
        } catch (SQLException ex) {
            throw new SQLException("Las transacciones enviadas no pudieron ser actualizadas", ex);
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException {
        String query = "UPDATE " +
                "wTransAbonoDisp " +
                "SET " +
                "fecha_respuesta_fimpe = ?, " +
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
        String query = "UPDATE " +
                "wTransAbonoDisp " +
                "SET " +
                "estado_respuesta_fimpe = ?," +
                "fecha_respuesta_fimpe = ? " +
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
    public List<VRTReportRecord> getVRTReportRecord() {
        List<VRTReportRecord> vrtReportRecordList = new ArrayList<>();
        String query = "SELECT " +
                "d.idDispositivo," +
                "e.Descripcion," +
                "d.Serie " +
                "FROM cDispositivos d " +
                "INNER JOIN cEstaciones e " +
                "ON d.idEstacion = e.idEstacion " +
                "AND d.idTipoDispositivo = 2 " +
                "AND e.estaActivo = 1 " +
                "AND e.idSIR IS NOT NULL " +
                "AND e.idCorredor IS NOT NULL " +
                "ORDER BY e.idEstacion";
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Long deviceId = resultSet.getLong("idDispositivo");
                        String station = resultSet.getString("Descripcion");
                        String locationId = resultSet.getString("Serie");
                        vrtReportRecordList.add(new VRTReportRecord(deviceId, station, locationId));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir las VRT para generar el reporte", ex);
        }
        return vrtReportRecordList;
    }

    @Override
    public void collectVRTReportRecordInfo(LocalDateTime startDate, LocalDateTime endDate, List<VRTReportRecord> vrtReportRecordList) {
        for (VRTReportRecord vrtReportRecord : vrtReportRecordList) {
            collectVRTReportRecordPerType(RECHARGE_OK_VRT, vrtReportRecord.getDeviceId(), startDate, endDate, vrtReportRecord);
            collectVRTReportRecordPerType(SELL_OK_VRT, vrtReportRecord.getDeviceId(), startDate, endDate, vrtReportRecord);
            collectVRTReportRecordPerType(RECHARGE_QR_OK_VRT, vrtReportRecord.getDeviceId(), startDate, endDate, vrtReportRecord);
        }
    }

    private void collectVRTReportRecordPerType(OperationType operationType, Long deviceId, LocalDateTime startDate,
                                               LocalDateTime endDate, VRTReportRecord vrtReportRecord) {
        String query = "SELECT " +
                "COUNT(*) as transacciones,  " +
                "estado_respuesta_fimpe  " +
                "FROM wTransAbonoDisp " +
                "WHERE fecha_respuesta_fimpe BETWEEN ? AND ? " +
                "AND TipoOperacion IN(?) " +
                "AND estado_respuesta_fimpe IN(?, ?) " +
                "AND idDispositivo = ? " +
                "GROUP BY estado_respuesta_fimpe";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, operationType.getValue());
                preparedStatement.setInt(4, SENT_OK.getValue());
                preparedStatement.setInt(5, SENT_WITH_ERROR.getValue());
                preparedStatement.setLong(6, deviceId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int transactions = resultSet.getInt("transacciones");
                        int fimpeStatus = resultSet.getInt("estado_respuesta_fimpe");

                        if (fimpeStatus == SENT_OK.getValue()) {
                            switch (operationType) {
                                case RECHARGE_OK_VRT:
                                    vrtReportRecord.setRechargeRecordsOk(transactions);
                                    break;
                                case RECHARGE_QR_OK_VRT:
                                    vrtReportRecord.setRechargeQRRecordsOk(transactions);
                                    break;
                                case SELL_OK_VRT:
                                    vrtReportRecord.setSellRecordsOk(transactions);
                                    break;
                            }
                        }
                        if (fimpeStatus == SENT_WITH_ERROR.getValue()) {
                            switch (operationType) {
                                case RECHARGE_OK_VRT:
                                    vrtReportRecord.setRechargeRecordsError(transactions);
                                    break;
                                case RECHARGE_QR_OK_VRT:
                                    vrtReportRecord.setRechargeQRRecordsError(transactions);
                                    break;
                                case SELL_OK_VRT:
                                    vrtReportRecord.setSellRecordsError(transactions);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir el reporte de envios de la VRT " + deviceId, ex);
        }
    }
}
