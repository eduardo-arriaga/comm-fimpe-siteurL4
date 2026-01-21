package com.idear.fimpe.torniquete.infraestructure;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.torniquete.domain.TorniqueteReportRecord;
import com.idear.fimpe.torniquete.domain.TorniqueteRepository;
import com.idear.fimpe.torniquete.domain.TorniqueteTransaction;
import com.idear.fimpe.torniquete.domain.TorniquteNumberControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;
import static com.idear.fimpe.enums.OperationType.*;


public class TorniqueteSQLServerRepository implements TorniqueteRepository {
    Logger logger = LoggerFactory.getLogger(TorniqueteSQLServerRepository.class);

    /**
     * Aplica query en la BD, para buscar la transaccion de torniquete mas vieja, no enviada a fimpe.
     *
     * @param operationType    tipo de operacion de torniquete (6).
     * @param operationTypeTwo tipo de operacion de garita (7).
     * @return La fecha mas vieja encontrada.
     */
    @Override
    public LocalDateTime getOldestTransactionDateNonExported(OperationType operationType, OperationType operationTypeTwo, OperationType operationTypeThree) {
        String query = "" +
                "SELECT MIN(FechaHora) as FechaHora " +
                "FROM wTransAbonoDisp wtt " +
                "WHERE estado_respuesta_fimpe IN(?, ?) " +
                "AND TipoOperacion IN(?, ?, ?)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                preparedStatement.setInt(3, operationType.getValue());
                preparedStatement.setInt(4, operationTypeTwo.getValue());
                preparedStatement.setInt(5, operationTypeThree.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getTimestamp("FechaHora").toLocalDateTime();
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

    /**
     * Aplica qyery en BD, para encontrar todos los dispositivos torniquete, perteneciente al corredor de MMP.
     *
     * @return Lista de torniquetes
     */
    @Override
    public List<TorniquteNumberControl> getTorniqueteDevices() {
        String query = "" +
                "SELECT DISTINCT " +
                "e.idCorredor, " +
                "e.idSIR, " +
                "e.idEstacionFimpe, " +
                "d.idDispositivo, " +
                "d.serie, " +
                "e.idEstacion " +
                "FROM cTorniquetes t " +
                "INNER JOIN cEstaciones e " +
                "ON t.idEstacion = e.idEstacion " +
                "INNER JOIN cDispositivos d " +
                "ON t.idTorniquete = d.idTorniquete " +
                "AND e.idCorredor IS NOT NULL " +
                "Order by e.idEstacion";

        List<TorniquteNumberControl> torniquteNumberControlList = new ArrayList<>();
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {

                        String routeID = resultSet.getString("idCorredor");
                        String eurID = resultSet.getString("idSIR");
                        String stationID = resultSet.getString("idEstacionFimpe");
                        String deviceId = resultSet.getString("idDispositivo");
                        String serie = resultSet.getString("Serie");

                        TorniquteNumberControl device = new TorniquteNumberControl(routeID, eurID, stationID, deviceId, serie);
                        torniquteNumberControlList.add(device);
                    }
                    return torniquteNumberControlList;
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir la lista de dispositivos", ex);
        }
        return torniquteNumberControlList;
    }

    /**
     * Aplica query en la BD, para encontrar todas las transacciones no exportadas de cierto dispositivo.
     *
     * @param deviceId  Id del dispositivo que se va a buscar.
     * @param startDate Fecha inicial de busqueda.
     * @param endDate   Fecha final de busqueda.
     * @return Lista de transacciones no expostadas del dispositivo buscado.
     */
    @Override
    public List<TorniqueteTransaction> getNonExportedTransaction(String deviceId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = "SELECT " +
                "TipoOperacion," +
                "idHoozie," +
                "idTransAbono, " +
                "transactionReceipt, " +
                "FechaHora, " +
                "CodigoFisico, " +
                "panMasked, " +
                "idProducto, " +
                "MontoEvento, " +
                "SaldoInicial, " +
                "SaldoFinal, " +
                "bpdsInicial, " +
                "bpdsFinal, " +
                "idSAM, " +
                "ContFinalSAM, " +
                "FolioTarjeta, " +
                "TipoDebito, " +
                "Procesada " +
                "FROM wTransAbonoDisp " +
                "WHERE idDispositivo = ? " +
                "AND FechaHora BETWEEN ? AND ? " +
                "AND TipoOperacion IN(?, ?) " +
                "AND estado_respuesta_fimpe  IN(?, ?) " +
                "AND Clase NOT IN(9, 16, 50)";

        List<TorniqueteTransaction> torniqueteTransactionsNonExported = new ArrayList<>();
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, deviceId);
                preparedStatement.setTimestamp(2, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(3, Timestamp.valueOf(endDate));
                preparedStatement.setInt(4, DEBIT_OK_TORNIQUETE.getValue());
                preparedStatement.setInt(5, DEBIT_OK_GARITA.getValue());
                preparedStatement.setInt(6, NOT_SENT.getValue());
                preparedStatement.setInt(7, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {

                        int operation = resultSet.getInt("TipoOperacion");
                        long transactionId = resultSet.getLong("idTransAbono");
                        LocalDateTime transactionDate = resultSet.getTimestamp("fechaHora").toLocalDateTime();
                        float transactionAmmount = resultSet.getFloat("montoEvento");
                        float initalBalance = Math.abs(resultSet.getFloat("saldoInicial"));
                        float finalBalance = Math.abs(resultSet.getFloat("saldoFinal"));
                        String samId = resultSet.getString("idSam");
                        Integer debitType = resultSet.getInt("tipoDebito");
                        Integer procesada = resultSet.getInt("Procesada");

                        //Revisa y separa las trasacciones de QR de Hoozie
                        if (debitType == DEBIT_QR_OK_TORNIQUETE.getValue()) {

                            String idHoozie = resultSet.getString("idHoozie");

                            TorniqueteTransaction transaction = new TorniqueteTransaction(transactionId, transactionDate,
                                    idHoozie, transactionAmmount, initalBalance, finalBalance);

                            torniqueteTransactionsNonExported.add(transaction);

                            //Revisa y procesa las transacciones de debitos
                        }else if(debitType == DEBIT_TB_OK_TORNIQUETE.getValue()){

                        } else if(operation == DEBIT_OK_GARITA.getValue() || operation == DEBIT_OK_TORNIQUETE.getValue()) {
                            String serialCard = resultSet.getString("codigoFisico");
                            String product = resultSet.getString("idProducto");
                            float bpdsInicial = resultSet.getFloat("bpdsInicial");
                            float bpdsFinal = resultSet.getFloat("bpdsFinal");
                            String samTransactionCounter = resultSet.getString("contFinalSAM");
                            long cardTransactionCounter = resultSet.getLong("folioTarjeta");

                            TorniqueteTransaction transaction = new TorniqueteTransaction(transactionId, transactionDate,
                                    serialCard, product, transactionAmmount, initalBalance, finalBalance, bpdsInicial,
                                    bpdsFinal, samId, samTransactionCounter, cardTransactionCounter, debitType);
                            torniqueteTransactionsNonExported.add(transaction);
                        }
                    }
                    return torniqueteTransactionsNonExported;
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir la lista de transacciones", ex);
        }
        return torniqueteTransactionsNonExported;
    }

    /**
     * Aplica query en BD, para actualizar las transacciones que fueron enviadas.
     *
     * @param torniquteNumberControl Disposito del cual se actualizaran sus transacciones.
     * @throws SQLException
     */
    @Override
    public void updateTransactionsTorniquete(TorniquteNumberControl torniquteNumberControl) throws SQLException {
        String query = "UPDATE " +
                "wTransAbonoDisp " +
                "SET " +
                "folio_corte_fimpe = (?), " +
                "fecha_envio_fimpe = (?), " +
                "estado_respuesta_fimpe = (?) " +
                "WHERE idTransAbono = (?)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (TorniqueteTransaction torniqueteTransaction : torniquteNumberControl.getTorniqueteTransactions()) {
                    preparedStatement.setLong(1, torniquteNumberControl.getCutId());
                    preparedStatement.setTimestamp(2, Timestamp.valueOf(torniquteNumberControl.getCutDate()));
                    preparedStatement.setInt(3, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setLong(4, torniqueteTransaction.getTransactionId());
                    preparedStatement.addBatch();
                }
                int transactionsUpdated = preparedStatement.executeBatch().length;
                logger.info("{} transacciones actualizadas de {}", transactionsUpdated, torniquteNumberControl.getTorniqueteTransactions().size());
            }
        } catch (SQLException ex) {
            throw new SQLException("Las transacciones enviadas de torniquete no pudieron ser actualizadas", ex);
        }
    }

    /**
     * Aplica query en BD, para actualizar las transacciones de las que se recivio acuse con extension ok.
     *
     * @param folioCut     folio del cortes
     * @param receivedDate fecha
     * @param fileName     nombre del archivo con extension ok
     * @throws SQLException
     */
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

    /**
     * Aplica query en BD, para actualizar las transacciones de la que se recivio acuse con extension err.
     *
     * @param folioCut folio del corte.
     * @throws SQLException
     */
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
    public List<TorniqueteReportRecord> getTorniqueteReportRecord() {
        List<TorniqueteReportRecord> torniqueteReportRecordList = new ArrayList<>();
        String query = "SELECT DISTINCT  " +
                "e.Descripcion ," +
                "d.serie, " +
                "d.idDispositivo " +
                "FROM cTorniquetes t " +
                "INNER JOIN cEstaciones e " +
                "ON t.idEstacion = e.idEstacion " +
                "INNER JOIN cDispositivos d " +
                "ON t.idTorniquete = d.idTorniquete " +
                "AND e.idCorredor IS NOT NULL " +
                "Order by e.Descripcion";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String station = resultSet.getString("Descripcion");
                        String locationId = resultSet.getString("serie");
                        Long deviceId = resultSet.getLong("idDispositivo");

                        torniqueteReportRecordList.add(new TorniqueteReportRecord(station, locationId, deviceId));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir la informacion de torniquetes", ex);
        }
        return torniqueteReportRecordList;
    }

    @Override
    public void collectTorniqueteRecordsReport(LocalDateTime startDate, LocalDateTime endDate,
                                               List<TorniqueteReportRecord> torniqueteReportRecordList) {

        for (TorniqueteReportRecord torniqueteReportRecord : torniqueteReportRecordList) {
            collectTorniqueteRecordsPerType(DEBIT_OK_TORNIQUETE, torniqueteReportRecord.getDeviceId(),
                    startDate, endDate, torniqueteReportRecord);
            collectTorniqueteRecordsPerType(DEBIT_OK_GARITA, torniqueteReportRecord.getDeviceId(),
                    startDate, endDate, torniqueteReportRecord);
            collectTorniqueteRecordsPerType(DEBIT_QR_OK_TORNIQUETE, torniqueteReportRecord.getDeviceId(),
                    startDate, endDate, torniqueteReportRecord);
        }


    }

    private void collectTorniqueteRecordsPerType(OperationType operationType,
                                                 Long deviceId, LocalDateTime startDate,
                                                 LocalDateTime endDate, TorniqueteReportRecord torniqueteReportRecord) {
        String query = "SELECT " +
                "COUNT(*) as transacciones," +
                "estado_respuesta_fimpe " +
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
                preparedStatement.setLong(6, torniqueteReportRecord.getDeviceId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int transactions = resultSet.getInt("transacciones");
                        int fimpeStatus = resultSet.getInt("estado_respuesta_fimpe");

                        if (fimpeStatus == SENT_OK.getValue()) {
                            switch (operationType) {
                                case DEBIT_OK_TORNIQUETE:
                                    torniqueteReportRecord.setDebitTorniqueteRecordsOk(transactions);
                                    break;
                                case DEBIT_OK_GARITA:
                                    torniqueteReportRecord.setDebitGaritaRecordsOk(transactions);
                                    break;
                                case DEBIT_QR_OK_TORNIQUETE:
                                    torniqueteReportRecord.setDebitQRRecordsOk(transactions);
                                    break;
                            }
                        }
                        if (fimpeStatus == SENT_WITH_ERROR.getValue()) {
                            switch (operationType) {
                                case DEBIT_OK_TORNIQUETE:
                                    torniqueteReportRecord.setDebitTorniqueteRecordsError(transactions);
                                    break;
                                case DEBIT_OK_GARITA:
                                    torniqueteReportRecord.setDebitGaritaRecordsError(transactions);
                                    break;
                                case DEBIT_QR_OK_TORNIQUETE:
                                    torniqueteReportRecord.setDebitQRRecordsError(transactions);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir las informacion para el reporte de  torniquetes", ex);
        }
    }
}


