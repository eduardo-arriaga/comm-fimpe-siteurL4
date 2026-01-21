package com.idear.fimpe.counters.infraestructure;

import com.idear.fimpe.counters.domain.CountersNumberControl;
import com.idear.fimpe.counters.domain.CountersReportRecord;
import com.idear.fimpe.counters.domain.CountersSQLRepository;
import com.idear.fimpe.counters.domain.CountersTransaction;
import com.idear.fimpe.database.SQLServerDatabaseConnection;
import com.idear.fimpe.kilometers.infraestructure.KilometersSQLServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.FimpeStatus.*;

public class CountersSQLServerRepository implements CountersSQLRepository {

    private Logger logger = LoggerFactory.getLogger(KilometersSQLServerRepository.class);

    @Override
    public List<CountersNumberControl> getCountersNumberControls(LocalDateTime startDate, LocalDateTime endDate) {

        List<CountersNumberControl> countersNumberControls = new ArrayList<>();

        String sql = "" +
                "SELECT c.idTransaccion, c.Subidas , c.Bajadas , c.FechaHora, a.Descripcion,  r.idCorredor, r.idSIR " +
                "FROM wContadoresAutobus c " +
                "INNER JOIN cAutobuses a " +
                "ON c.idAutobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON c.idRuta = r.idRuta " +
                "WHERE c.FechaHora BETWEEN ? AND ? " +
                "AND c.EstadoRespuestaFimpe IN(?,?) " +
                "AND r.idCorredor IS NOT NULL " +
                "ORDER BY a.Descripcion, r.idCorredor";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, NOT_SENT.getValue());
                preparedStatement.setInt(4, SENT_WITH_ERROR.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){

                    CountersNumberControl countersNumberControl = new CountersNumberControl();
                    String oldRouteDescription = null;
                    boolean isFirstRecord = true;

                    while(resultSet.next()){
                        String vehicleId = resultSet.getString("Descripcion");
                        Long transactionId = resultSet.getLong("idTransaccion");
                        Integer getOn = resultSet.getInt("subidas");
                        Integer getOff = resultSet.getInt("bajadas");
                        LocalDate localDate = resultSet.getTimestamp("FechaHora").toLocalDateTime().toLocalDate();
                        String routeDescription = resultSet.getString("idCorredor");
                        String idSIR = resultSet.getString("idSIR");

                        //Si el campo descripcion o identificador de dispositivo
                        // tiene mas de 6 caracteres (014682002313400) lo saltamos
                        if(vehicleId.length() > 6 || (getOff == 0 && getOn == 0))
                            continue;

                        if(isFirstRecord){
                            //Si es el primer registro, creamos un control de cifras y le asignamos el registro
                            isFirstRecord = false;
                            countersNumberControl = new CountersNumberControl(routeDescription, idSIR);
                            //Agregamos el control de cifras a la lista principal
                            countersNumberControls.add(countersNumberControl);
                            //Agregamos la transaccion al control de cifras
                            countersNumberControl.getCountersTransactions().add(
                                    new CountersTransaction(transactionId, vehicleId,  routeDescription, localDate,
                                            getOn, getOff));
                        }else{
                            //la ruta cambia MP-A07 -> MP-A08
                            //quiere decir que necesitamos crear un nuevo control de cifras y agregar la transaccion
                            if(!routeDescription.equals(oldRouteDescription)){
                                //Creamos un control de cifras y lo agregamos a la lista principal
                                countersNumberControl = new CountersNumberControl(routeDescription, idSIR);
                                countersNumberControls.add(countersNumberControl);
                            }

                            //Se agrega la transaccion al control de cifras
                            countersNumberControl.getCountersTransactions().add(
                                    new CountersTransaction( transactionId, vehicleId,  routeDescription, localDate,
                                            getOn, getOff));
                        }
                        oldRouteDescription = routeDescription;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error al intentar conseguir el las rutas para las transacciones de contadores", e);
        }
        return countersNumberControls;
    }

    @Override
    public Long getCutFolio() throws SQLException {
        String sql = "" +
                "SELECT MAX(FolioCorteFimpe) as 'cutFolio' " +
                "FROM wContadoresAutobus";

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
    public void updateCountersNumberControlAsSent(CountersNumberControl countersNumberControl) throws SQLException {
        String sql = "" +
                "UPDATE wContadoresAutobus " +
                "SET FolioCorteFimpe = ?, " +
                "EstadoRespuestaFimpe = ?, " +
                "FechaEnvioFimpe = ? " +
                "WHERE idTransaccion = ?";

        try (Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (CountersTransaction countersTransaction : countersNumberControl.getCountersTransactions()) {
                    //Por cada registro de autobus, ejecutar el query
                    preparedStatement.setLong(1, countersNumberControl.getCutId());
                    preparedStatement.setInt(2, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    preparedStatement.setLong(4, countersTransaction.getTransactionId());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut, LocalDateTime receivedDate, String fileName) throws SQLException{
        String sql = "" +
                "UPDATE wContadoresAutobus " +
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
    public void updateErrorAckReceived(Long folioCut, LocalDateTime receivedDate) throws SQLException{
        String sql = "" +
                "UPDATE wContadoresAutobus " +
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
    public List<CountersReportRecord> getCountersReportRecord(LocalDateTime startDate, LocalDateTime endDate) {
        List<CountersReportRecord> countersReportRecords = new ArrayList<>();

        String sql = "" +
                "SELECT SUM(c.Subidas) as 'subidas', SUM(c.Bajadas) as 'bajadas', r.idCorredor, r.idSIR " +
                "FROM wContadoresAutobus c " +
                "INNER JOIN cRutas r " +
                "ON c.idRuta = r.idRuta " +
                "WHERE c.FechaRespuestaFimpe  BETWEEN ? AND ? " +
                "AND c.EstadoRespuestaFimpe = ? " +
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

                        Integer getOn = resultSet.getInt("subidas");
                        Integer getOff = resultSet.getInt("bajadas");
                        String routeDescription = resultSet.getString("idCorredor");
                        String eurId = resultSet.getString("idSIR");

                        countersReportRecords.add(new CountersReportRecord(eurId, routeDescription,
                                startDate.toLocalDate(), getOn, getOff));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al intentar conseguir los registros para generar el reporte de contadores", ex);
        }
        return countersReportRecords;
    }
}
