package com.idear.fimpe.remoterecharge.infraestructure;

import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeNumberControl;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeRepository;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;
import static com.idear.fimpe.enums.RemoteRechargeState.*;

/**
 * @author rperez
 * @version 1.0
 * @see RemoteRechargeRepository
 */
public class RemoteRechargeSQLServerRepository implements RemoteRechargeRepository {

    private Logger logger = LoggerFactory.getLogger(RemoteRechargeSQLServerRepository.class);

    @Override
    public void insertRequests(List<RemoteRechargeTransaction> requests) throws SQLException {

        String insert = "" +
                "INSERT wRecargasRemotas (idTarjeta, monto, numeroAccionProducto, fechaRegistro, idProducto, tipoRecarga, estadoPeticion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";

        String delete = "" +
                "DELETE wRecargasRemotas " +
                "WHERE estadoPeticion = 'UNCONFIRMED'  OR ( estadoPeticion = 'UNREQUESTED' AND estadoRespuestaFIMPE = 2)";

        //Elimina lo que este sin confirmar o lo que ya se haya solicitado a FIMPE
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(delete)) {
                preparedStatement.execute();
            }
        }


        //Inserta la lista de peticiones
        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(insert)) {
                for (RemoteRechargeTransaction remoteRechargeTransaction : requests) {
                    preparedStatement.setString(1, remoteRechargeTransaction.getCardId());
                    preparedStatement.setFloat(2, remoteRechargeTransaction.getAmount());
                    preparedStatement.setInt(3, remoteRechargeTransaction.getActionNumberAppliedToProduct());
                    preparedStatement.setTimestamp(4, Timestamp.valueOf(remoteRechargeTransaction.getRegisterDateTime()));
                    preparedStatement.setString(5, remoteRechargeTransaction.getProductId());
                    preparedStatement.setString(6, remoteRechargeTransaction.getRechargeType());
                    preparedStatement.setString(7, UNCONFIRMED.name());
                    preparedStatement.addBatch();
                }

                preparedStatement.executeBatch();
            }
        }
    }

    @Override
    public Long getCutFoil() throws SQLException {

        String select = "" +
                "SELECT MAX(folioCorteFIMPE) as folioCorte " +
                "FROM wRecargasRemotas";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(select)) {
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
    public List<RemoteRechargeTransaction> getUnrequestedRemoteRecharge() throws SQLException {

        List<RemoteRechargeTransaction> remoteRechargeTransactions = new ArrayList<>();

        String select = "" +
                "SELECT " +
                "idTarjeta, fechaRegistro, numeroAccionProducto, monto, idProducto, tipoRecarga " +
                "FROM wRecargasRemotas " +
                "WHERE estadoPeticion = ? " +
                "AND estadoRespuestaFIMPE IN (?, ?)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(select)) {
                preparedStatement.setString(1, UNREQUESTED.name());
                preparedStatement.setInt(2, NOT_SENT.getValue());
                preparedStatement.setInt(3, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String cardId = resultSet.getString("idTarjeta");
                        LocalDateTime registerDateTime = resultSet.getTimestamp("fechaRegistro").toLocalDateTime();
                        Integer actionNumberAppliedToProduct = resultSet.getInt("numeroAccionProducto");
                        Float amount = resultSet.getFloat("monto");
                        String productId = resultSet.getString("idProducto");
                        String rechargeType = resultSet.getString("tipoRecarga");

                        remoteRechargeTransactions.add(
                                new RemoteRechargeTransaction(
                                        cardId, registerDateTime, actionNumberAppliedToProduct,
                                        amount, productId, rechargeType));
                    }
                }
            }
        }
        return remoteRechargeTransactions;
    }

    @Override
    public List<RemoteRechargeTransaction> getConfirmedRemoteRecharge() throws SQLException {

        List<RemoteRechargeTransaction> remoteRechargeTransactions = new ArrayList<>();

        String select = "" +
                "SELECT " +
                "idTarjeta, fechaRegistro, numeroAccionProducto, monto, idProducto, tipoRecarga, idSAM " +
                "FROM wRecargasRemotas " +
                "WHERE estadoPeticion = ? " +
                "AND estadoRespuestaFIMPE IN (?, ?)";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(select)) {
                preparedStatement.setString(1, CONFIRMED.name());
                preparedStatement.setInt(2, NOT_SENT.getValue());
                preparedStatement.setInt(3, SENT_WITH_ERROR.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String cardId = resultSet.getString("idTarjeta");
                        LocalDateTime registerDateTime = resultSet.getTimestamp("fechaRegistro").toLocalDateTime();
                        Integer actionNumberAppliedToProduct = resultSet.getInt("numeroAccionProducto");
                        Float amount = resultSet.getFloat("monto");
                        String productId = resultSet.getString("idProducto");
                        String rechargeType = resultSet.getString("tipoRecarga");
                        String samId = resultSet.getString("idSAM");

                        remoteRechargeTransactions.add(
                                new RemoteRechargeTransaction(
                                        cardId, registerDateTime, actionNumberAppliedToProduct,
                                        amount, productId, rechargeType, samId));
                    }
                }
            }
        }
        return remoteRechargeTransactions;
    }

    @Override
    public void updateAsSentRemoteRecharge(RemoteRechargeNumberControl remoteRechargeNumberControl) throws SQLException {

        String update = "" +
                "UPDATE wRecargasRemotas " +
                "SET estadoRespuestaFIMPE = ?, fechaEnvioFIMPE = ?, folioCorteFIMPE = ? " +
                "WHERE idTarjeta = ? " +
                "  AND numeroAccionProducto = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(update)) {
                for (RemoteRechargeTransaction remoteRechargeTransaction : remoteRechargeNumberControl.getConfirmations()) {
                    fillPreparedStatementToUpdateSents(
                            preparedStatement,
                            remoteRechargeNumberControl.getGenerationDateTime(),
                            remoteRechargeNumberControl.getCutFoil(),
                            remoteRechargeTransaction.getCardId(),
                            remoteRechargeTransaction.getActionNumberAppliedToProduct());
                }

                for (RemoteRechargeTransaction remoteRechargeTransaction : remoteRechargeNumberControl.getRequests()) {
                    fillPreparedStatementToUpdateSents(
                            preparedStatement,
                            remoteRechargeNumberControl.getGenerationDateTime(),
                            remoteRechargeNumberControl.getCutFoil(),
                            remoteRechargeTransaction.getCardId(),
                            remoteRechargeTransaction.getActionNumberAppliedToProduct());
                }

                preparedStatement.executeBatch();
            }
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut) throws SQLException {

        String update = "" +
                "UPDATE wRecargasRemotas " +
                "SET estadoRespuestaFIMPE = ?, fechaRespuestaFIMPE = ? " +
                "WHERE folioCorteFIMPE = ? ";

        try(Connection connection =SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(update)){
                preparedStatement.setInt(1, SENT_OK.getValue());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.setLong(3, folioCut);

                preparedStatement.execute();
            }
        }
    }

    @Override
    public void updateErrorAckReceived(Long folioCut) throws SQLException {
        String update = "" +
                "UPDATE wRecargasRemotas " +
                "SET estadoRespuestaFIMPE = ? " +
                "WHERE folioCorteFIMPE = ?";

        try(Connection connection =SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(update)){
                preparedStatement.setInt(1, SENT_WITH_ERROR.getValue());
                preparedStatement.setLong(2, folioCut);

                preparedStatement.execute();
            }
        }
    }

    private void fillPreparedStatementToUpdateSents(PreparedStatement preparedStatement,
                                                    LocalDateTime sentDate,
                                                    Long cutFoil, String cardId,
                                                    Integer actionNumberAppliedToProduct) throws SQLException{

        preparedStatement.setInt(1, SENT_AND_PENDIENT.getValue());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(sentDate));
        preparedStatement.setLong(3, cutFoil);
        preparedStatement.setString(4, cardId);
        preparedStatement.setInt(5, actionNumberAppliedToProduct);
        preparedStatement.addBatch();
    }
}
