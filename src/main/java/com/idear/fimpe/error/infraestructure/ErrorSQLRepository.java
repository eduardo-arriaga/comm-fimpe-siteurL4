package com.idear.fimpe.error.infraestructure;

import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.error.domain.ErrorBusStation;
import com.idear.fimpe.error.domain.ErrorRepository;
import com.idear.fimpe.error.domain.ErrorTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ErrorSQLRepository implements ErrorRepository {

    private Logger logger = LoggerFactory.getLogger(ErrorRepository.class);

    @Override
    public void getBusInfo(ErrorBusStation errorBusStation) {
        String query = "SELECT " +
                "DISTINCT " +
                "t.idTransaccion, " +
                "t.Autobus, " +
                "a.Descripcion, " +
                "r.idCorredor " +
                "FROM wTransTarjetas t " +
                "INNER JOIN cAutobuses a " +
                "ON t.Autobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON t.NumRuta = r.Numero " +
                "AND t.idElectronico = ? " +
                "AND t.FolioTarjeta = ? " +
                "AND r.idCorredor IS NOT NULL";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, errorBusStation.getCardId());
                preparedStatement.setLong(2, errorBusStation.getCardFoil());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransaccion");
                        Long busId = resultSet.getLong("Autobus");
                        String busDescription = resultSet.getString("Descripcion");
                        String routeId = resultSet.getString("idCorredor");
                        errorBusStation.setTransactionId(transactionId);
                        errorBusStation.setBusStationId(busId);
                        errorBusStation.setLocationIdDescription(busDescription);
                        errorBusStation.setRouteId(routeId);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir informacion del autobus para crear el reporte", ex);
        }
    }

    @Override
    public void getStationInfo(ErrorBusStation errorBusStation) {
        String query = "SELECT " +
                "DISTINCT " +
                "t.idTransAbono, " +
                "t.idDispositivo, " +
                "d.Serie, " +
                "'MP-T0' as idCorredor " +
                "FROM wTransAbonoDisp t " +
                "INNER JOIN cDispositivos d " +
                "ON t.idDispositivo = d.idDispositivo  " +
                "AND CodigoFisico = ? " +
                "AND FolioTarjeta = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, errorBusStation.getCardId());
                preparedStatement.setLong(2, errorBusStation.getCardFoil());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransAbono");
                        Long deviceId = resultSet.getLong("idDispositivo");
                        String locationId = resultSet.getString("Serie");
                        String routeId = resultSet.getString("idCorredor");
                        errorBusStation.setTransactionId(transactionId);
                        errorBusStation.setBusStationId(deviceId);
                        errorBusStation.setLocationIdDescription(locationId);
                        errorBusStation.setRouteId(routeId);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir informacion del autobus para crear el reporte", ex);
        }
    }

    @Override
    public void getBusTransactionInfo(ErrorTransaction errorTransaction) {
        String query = "SELECT " +
                "idTransaccion, " +
                "FechaHora, " +
                "idProducto, " +
                "Clase " +
                "FROM wTransTarjetas " +
                "WHERE idElectronico = ? " +
                "AND FolioTarjeta = ? " +
                "AND folio_corte_fimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, errorTransaction.getCardId());
                preparedStatement.setLong(2, errorTransaction.getCardFoil());
                preparedStatement.setLong(3, errorTransaction.getCutFoil());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransaccion");
                        LocalDateTime dateTimeTransaction = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String productId = resultSet.getString("idProducto");
                        Integer cardClass = resultSet.getInt("Clase");

                        errorTransaction.setTransactionId(transactionId);
                        errorTransaction.setTransactionDate(dateTimeTransaction);
                        errorTransaction.setProductId(productId);
                        errorTransaction.setCardClass(cardClass);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir informacion del autobus para crear el reporte", ex);
        }
    }

    @Override
    public void getStationTransactionInfo(ErrorTransaction errorTransaction) {
        String query = "SELECT " +
                "idTransAbono, " +
                "FechaHora ," +
                "idProducto, " +
                "Clase " +
                "FROM wTransAbonoDisp " +
                "WHERE CodigoFisico = ? " +
                "AND FolioTarjeta = ? " +
                "AND folio_corte_fimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, errorTransaction.getCardId());
                preparedStatement.setLong(2, errorTransaction.getCardFoil());
                preparedStatement.setLong(3, errorTransaction.getCutFoil());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransAbono");
                        LocalDateTime dateTimeTransaction = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String productId = resultSet.getString("idProducto");
                        Integer cardClass = resultSet.getInt("Clase");

                        errorTransaction.setTransactionId(transactionId);
                        errorTransaction.setTransactionDate(dateTimeTransaction);
                        errorTransaction.setProductId(productId);
                        errorTransaction.setCardClass(cardClass);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir informacion de la transaccion de tarjeta para crear el reporte", ex);
        }
    }

    @Override
    public void getStationTransactionQRInfo(ErrorTransaction errorTransaction) {
        String query = "SELECT " +
                "idTransAbono, " +
                "FechaHora " +
                "FROM wTransAbonoDisp " +
                "WHERE idHoozie = ? " +
                "AND folio_corte_fimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, errorTransaction.getCardId());
                preparedStatement.setLong(2, errorTransaction.getCutFoil());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransAbono");
                        LocalDateTime dateTimeTransaction = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String productId = "QR";
                        Integer cardClass = 0;

                        errorTransaction.setTransactionId(transactionId);
                        errorTransaction.setTransactionDate(dateTimeTransaction);
                        errorTransaction.setProductId(productId);
                        errorTransaction.setCardClass(cardClass);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir informacion de la transaccion QR para crear el reporte", ex);
        }
    }

    @Override
    public void getStationTransactionBancariaInfo(ErrorTransaction errorTransaction) {
        String query = "SELECT " +
                "idTransAbono, " +
                "FechaHora " +
                "FROM wTransAbonoDisp " +
                "WHERE transactionReceipt = ? " +
                "AND folio_corte_fimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, errorTransaction.getCardId());
                preparedStatement.setLong(2, errorTransaction.getCutFoil());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Long transactionId = resultSet.getLong("idTransAbono");
                        LocalDateTime dateTimeTransaction = resultSet.getTimestamp("FechaHora").toLocalDateTime();
                        String productId = "TB";
                        Integer cardClass = 0;

                        errorTransaction.setTransactionId(transactionId);
                        errorTransaction.setTransactionDate(dateTimeTransaction);
                        errorTransaction.setProductId(productId);
                        errorTransaction.setCardClass(cardClass);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir informacion de la transaccion QR para crear el reporte", ex);
        }
    }
}
