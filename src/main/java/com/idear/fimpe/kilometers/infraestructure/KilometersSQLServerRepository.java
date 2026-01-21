package com.idear.fimpe.kilometers.infraestructure;

import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.kilometers.domain.KilometersNumberControl;
import com.idear.fimpe.kilometers.domain.KilometersReportRecord;
import com.idear.fimpe.kilometers.domain.KilometersSQLRepository;
import com.idear.fimpe.kilometers.domain.KilometersTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;

public class KilometersSQLServerRepository implements KilometersSQLRepository {

    private Logger logger = LoggerFactory.getLogger(KilometersSQLServerRepository.class);

    @Override
    public List<KilometersNumberControl> getKilometersNumberControls(LocalDateTime startDate, LocalDateTime endDate) {
        List<KilometersNumberControl> kilometersNumberControls = new ArrayList<>();

        String sql = "" +
                "SELECT k.idTransaccion, CAST(k.Kilometros as DECIMAL(18, 2)) kilometros ,k.Vueltas, k.FechaHora, a.Descripcion, r.idCorredor, r.idSIR " +
                "FROM wKmAutobus k " +
                "INNER JOIN cAutobuses a " +
                "ON k.idAutobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON k.idRuta = r.idRuta " +
                "WHERE k.FechaHora BETWEEN ? AND ? " +
                "AND k.EstadoRespuestaFimpe IN (?, ?) " +
                "AND r.idCorredor IS NOT NULL " +
                "ORDER BY a.Descripcion, r.idCorredor";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, NOT_SENT.getValue());
                preparedStatement.setInt(4, SENT_WITH_ERROR.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){

                    KilometersNumberControl kilometersNumberControl = new KilometersNumberControl();
                    String oldRouteDescription = null;
                    boolean isFirstRecord = true;

                    while(resultSet.next()){
                        String vehicleId = resultSet.getString("Descripcion");
                        Long transactionId = resultSet.getLong("idTransaccion");
                        Double accumulatedKilometers = resultSet.getDouble("kilometros");
                        Double laps = resultSet.getDouble("Vueltas");
                        LocalDate localDate = resultSet.getTimestamp("FechaHora").toLocalDateTime().toLocalDate();
                        String routeDescription = resultSet.getString("idCorredor");
                        String idSIR = resultSet.getString("idSIR");

                        //Si el campo descripcion o identificador de dispositivo
                        // tiene mas de 6 caracteres (014682002313400) lo saltamos
                        if(vehicleId.length() > 6 || (accumulatedKilometers == 0 && laps == 0))
                            continue;

                        if(isFirstRecord){
                            //Si es el primer registro, creamos un control de cifras y le asignamos el registro
                            isFirstRecord = false;
                            kilometersNumberControl = new KilometersNumberControl(routeDescription, idSIR);
                            //Agregamos el control de cifras a la lista principal
                            kilometersNumberControls.add(kilometersNumberControl);
                            //Agregamos la transaccion al control de cifras
                            kilometersNumberControl.getKilometersTransactions().add(
                                    new KilometersTransaction(transactionId, vehicleId,  routeDescription, localDate,
                                            accumulatedKilometers, accumulatedKilometers, laps));
                        }else{
                            //la ruta cambia MP-A07 -> MP-A08
                            //quiere decir que necesitamos crear un nuevo control de cifras y agregar la transaccion
                            if(!routeDescription.equals(oldRouteDescription)){
                                //Creamos un control de cifras y lo agregamos a la lista principal
                                kilometersNumberControl = new KilometersNumberControl(routeDescription, idSIR);
                                kilometersNumberControls.add(kilometersNumberControl);
                            }

                            //Se agrega la transaccion al control de cifras
                            kilometersNumberControl.getKilometersTransactions().add(
                                    new KilometersTransaction( transactionId, vehicleId,  routeDescription, localDate,
                                            accumulatedKilometers, accumulatedKilometers, laps));
                        }
                        oldRouteDescription = routeDescription;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error al intentar conseguir el las rutas para las transacciones de kilometros", e);
        }
        return kilometersNumberControls;
    }

    @Override
    public Long getCutFolio() throws SQLException {
        String sql = "" +
                "SELECT MAX(FolioCorteFimpe) as 'cutFolio' " +
                "FROM wKmAutobus";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong("cutFolio") + 1;
                    }
                    throw new SQLException("El folio de corte no pudo ser conseguido");
                }
            }
        }
    }

    @Override
    public void updateKilometersNumberControlAsSent(KilometersNumberControl kilometersNumberControl) throws SQLException {
        String sql = "" +
                "UPDATE wKmAutobus " +
                "SET FolioCorteFimpe = ?, " +
                "EstadoRespuestaFimpe = ?, " +
                "FechaEnvioFimpe = ? " +
                "WHERE idTransaccion = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (KilometersTransaction kilometersTransaction : kilometersNumberControl.getKilometersTransactions()) {
                    //Por cada registro de autobus, ejecutar el query
                    preparedStatement.setLong(1, kilometersNumberControl.getCutId());
                    preparedStatement.setInt(2, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    preparedStatement.setLong(4, kilometersTransaction.getTransactionId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException {
        String sql = "" +
                "UPDATE wKmAutobus " +
                "SET EstadoRespuestaFimpe = ?, " +
                "ArchivoFimpe = ?, " +
                "FechaRespuestaFimpe = ? " +
                "WHERE FolioCorteFimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, SENT_OK.getValue());
                preparedStatement.setString(2, fileName);
                preparedStatement.setTimestamp(3, Timestamp.valueOf(receivedDate));
                preparedStatement.setLong(4, folioCut);
                preparedStatement.execute();
            }
        }
    }

    @Override
    public void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException {
        String sql = "" +
                "UPDATE wKmAutobus " +
                "SET EstadoRespuestaFimpe = ?," +
                "FechaRespuestaFimpe = ? " +
                "WHERE FolioCorteFimpe = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, SENT_WITH_ERROR.getValue());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(receivedDate));
                preparedStatement.setLong(3, folioCut);
                preparedStatement.execute();
            }
        }
    }

    @Override
    public List<KilometersReportRecord> getKilometersReportRecord(LocalDateTime startDate, LocalDateTime endDate) {
        List<KilometersReportRecord> kilometersReportRecords = new ArrayList<>();

        String sql = "" +
                "SELECT SUM(k.Vueltas) as 'vueltas', SUM(CAST(k.Kilometros as DECIMAL(18, 2))) 'kilometros', r.idCorredor, r.idSIR " +
                "FROM wKmAutobus k " +
                "INNER JOIN cRutas r " +
                "ON k.idRuta = r.idRuta " +
                "WHERE k.FechaRespuestaFimpe  BETWEEN ? AND ? " +
                "AND k.EstadoRespuestaFimpe = ? " +
                "AND r.idCorredor IS NOT NULL " +
                "GROUP BY r.idCorredor, r.idSIR " +
                "ORDER BY r.idSIR";


        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, SENT_OK.getValue());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                        Double laps = resultSet.getDouble("vueltas");
                        Double traveledKilometers = resultSet.getDouble("kilometros");
                        String routeDescription = resultSet.getString("idCorredor");
                        String eurId = resultSet.getString("idSIR");


                        kilometersReportRecords.add(new KilometersReportRecord(routeDescription, eurId,
                                startDate.toLocalDate(), laps, traveledKilometers, traveledKilometers));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir los registros para generar el reporte de kilometros", ex);
        }
        return kilometersReportRecords;
    }
}
