package com.idear.fimpe.cash.infraestructure;

import com.idear.fimpe.cash.domain.CashNumberControl;
import com.idear.fimpe.cash.domain.CashReportRecord;
import com.idear.fimpe.cash.domain.CashSQLRepository;
import com.idear.fimpe.cash.domain.CashTransaction;
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

public class CashSQLServerRepository implements CashSQLRepository {

    private Logger logger = LoggerFactory.getLogger(CashSQLServerRepository.class);

    @Override
    public List<CashNumberControl> getCashNumberControls(LocalDateTime startDate, LocalDateTime endDate) {

        List<CashNumberControl> cashNumberControls = new ArrayList<>();

        String sql = "" +
                "SELECT SUM(m.Monto) as 'monto', SUM(m.Pases) as 'pases', a.Descripcion, r.idCorredor, r.idSIR " +
                "FROM wTransMonedas m " +
                "INNER JOIN cRutas r " +
                "ON r.Numero = m.NumRuta " +
                "INNER JOIN cAutobuses a " +
                "ON a.idAutobus = m.idAutobus " +
                "WHERE m.FechaHora BETWEEN ? AND ? " +
                "AND m.EstadoRespuestaFimpe IN (?, ?) " +
                "AND r.idCorredor IS NOT NULL " +
                "GROUP BY a.Descripcion, r.idCorredor, r.idSIR " +
                "ORDER BY r.idCorredor";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, NOT_SENT.getValue());
                preparedStatement.setInt(4, SENT_WITH_ERROR.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){

                    CashNumberControl cashNumberControl = new CashNumberControl();
                    String oldRouteDescription = null;
                    boolean isFirstRecord = true;

                    while(resultSet.next()){
                        String vehicleId = resultSet.getString("Descripcion");
                        Float amount = resultSet.getFloat("monto");
                        Integer passengers = resultSet.getInt("pases");
                        String routeDescription = resultSet.getString("idCorredor");
                        String idSIR = resultSet.getString("idSIR");

                        //Si el campo descripcion o identificador de dispositivo
                        // tiene mas de 6 caracteres (014682002313400) lo saltamos
                        if(vehicleId.length() > 6 || passengers == 0)
                            continue;

                        if(isFirstRecord){
                            //Si es el primer registro, creamos un control de cifras y le asignamos el registro
                            isFirstRecord = false;
                            cashNumberControl = new CashNumberControl(routeDescription, idSIR);
                            //Agregamos el control de cifras a la lista principal
                            cashNumberControls.add(cashNumberControl);
                            //Agregamos la transaccion al control de cifras
                            cashNumberControl.getCashTransactions().add(
                                    new CashTransaction(vehicleId, endDate.toLocalDate(),
                                            0F, passengers, routeDescription));
                        }else{
                            //la ruta cambia MP-A07 -> MP-A08
                            //quiere decir que necesitamos crear un nuevo control de cifras y agregar la transaccion
                            if(!routeDescription.equals(oldRouteDescription)){
                                //Creamos un control de cifras y lo agregamos a la lista principal
                                cashNumberControl = new CashNumberControl(routeDescription, idSIR);
                                cashNumberControls.add(cashNumberControl);
                            }

                            //Se agrega la transaccion al control de cifras
                            cashNumberControl.getCashTransactions().add(
                                    new CashTransaction(vehicleId, endDate.toLocalDate(),
                                            0F, passengers, routeDescription));
                        }
                        oldRouteDescription = routeDescription;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error al intentar conseguir el las rutas para las transacciones de efectivo", e);
        }
        return cashNumberControls;
    }

    @Override
    public Long getCutFolio() throws SQLException {
        String sql = "" +
                "SELECT MAX(FolioCorteFimpe) as 'cutFolio' FROM wTransMonedas";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()){
                        return resultSet.getLong("cutFolio") + 1;
                    }
                    throw new SQLException("El folio de corte no pudo ser conseguido");
                }
            }
        }
    }

    @Override
    public void updateCashNumberControlAsSent(CashNumberControl cashNumberControl) throws SQLException {

        String sql = "" +
                "UPDATE m " +
                "SET m.FolioCorteFimpe = ?, m.EstadoRespuestaFimpe = ?, m.FechaEnvioFimpe = ? " +
                "FROM wTransMonedas m " +
                "INNER JOIN cAutobuses a " +
                "ON m.idAutobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON m.NumRuta = r.Numero " +
                "WHERE m.FechaHora BETWEEN ? AND ? " +
                "AND a.Descripcion = ? " +
                "AND r.idCorredor = ? " +
                "AND m.EstadoRespuestaFimpe IN(?, ?) " +
                "AND r.idCorredor IS NOT NULL";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){

                for (CashTransaction cashTransaction: cashNumberControl.getCashTransactions()) {
                    //Por cada registro de autobus, ejecutar el query
                    preparedStatement.setLong(1, cashNumberControl.getCutId());
                    preparedStatement.setInt(2, SENT_AND_PENDIENT.getValue());
                    preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    preparedStatement.setTimestamp(4, Timestamp.valueOf(cashNumberControl.getInitialCutDate()));
                    preparedStatement.setTimestamp(5, Timestamp.valueOf(cashNumberControl.getFinalCutDate()));
                    preparedStatement.setString(6, cashTransaction.getOriginalVehicleId());
                    preparedStatement.setString(7, cashNumberControl.getRouteIdDescription());
                    preparedStatement.setInt(8, NOT_SENT.getValue());
                    preparedStatement.setInt(9, SENT_WITH_ERROR.getValue());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    @Override
    public void updateSuccessfulAckReceived(Long folioCut,  LocalDateTime receivedDate, String fileName) throws SQLException {
        String sql = "" +
                "UPDATE wTransMonedas " +
                "SET EstadoRespuestaFimpe = ?, " +
                "ArchivoFimpe = ?, " +
                "FechaRespuestaFimpe = ? " +
                "WHERE FolioCorteFimpe = ?";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
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
                "UPDATE wTransMonedas " +
                "SET EstadoRespuestaFimpe = ?," +
                "FechaRespuestaFimpe = ? " +
                "WHERE FolioCorteFimpe = ?";

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setInt(1, SENT_WITH_ERROR.getValue());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(receivedDate));
                preparedStatement.setLong(3, folioCut);
                preparedStatement.execute();
            }
        }
    }

    @Override
    public List<CashReportRecord> getCashReportRecord(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "" +
                "SELECT SUM(m.Monto) as 'monto', SUM(m.Pases) as 'pases', a.Descripcion, r.idCorredor, r.idSIR " +
                "FROM wTransMonedas m " +
                "INNER JOIN cRutas r " +
                "ON r.Numero = m.NumRuta " +
                "INNER JOIN cAutobuses a " +
                "ON a.idAutobus = m.idAutobus " +
                "WHERE m.FechaRespuestaFimpe BETWEEN ? AND ? " +
                "AND m.EstadoRespuestaFimpe = ? " +
                "AND r.idCorredor IS NOT NULL " +
                "GROUP BY a.Descripcion, r.idCorredor, r.idSIR " +
                "ORDER BY r.idCorredor";

        List<CashReportRecord> cashReportRecords = new ArrayList<>();

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){

            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setTimestamp(1, Timestamp.valueOf(startDate));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(endDate));
                preparedStatement.setInt(3, SENT_OK.getValue());

                try(ResultSet resultSet = preparedStatement.executeQuery()){

                    while(resultSet.next()){

                        Float amount = resultSet.getFloat("monto");
                        Integer passengers = resultSet.getInt("pases");
                        String busDescription = resultSet.getString("Descripcion");
                        String routeDescription = resultSet.getString("idCorredor");
                        String eurId = resultSet.getString("idSIR");

                        cashReportRecords.add(new CashReportRecord(
                                busDescription, routeDescription, eurId, startDate.toLocalDate(), passengers, amount));
                    }
                }
            }
        }catch (SQLException ex){
            logger.error("Error al intentar conseguir los registros para generar el reporte de efectivo", ex);
        }
        return cashReportRecords;
    }

    @Override
    public void updateTransactionsWithOutValidRoute() {

        String selectTransactions = "" +
                "SELECT t.idTransaccion , a.Descripcion " +
                "FROM wTransMonedas  t " +
                "INNER JOIN cAutobuses a " +
                "ON t.idAutobus = a.idAutobus " +
                "INNER JOIN cRutas r " +
                "ON t.NumRuta = r.Numero " +
                "AND t.EstadoRespuestaFimpe IN(?, ?) " +
                "AND t.NumRuta IN(?, ?, ?, ?) " +
                "AND r.idSistema = 2";

        String updateTransactions = "" +
                "UPDATE wTransMonedas " +
                "SET NumRuta = ? " +
                "WHERE idTransaccion = ?";

        Map<Long, String> transactions = new HashMap<>();

        try(Connection connection = SQLServerDatabaseConnection.getConnection()){

            try(PreparedStatement preparedStatement = connection.prepareStatement(selectTransactions)){

                preparedStatement.setInt(1, NOT_SENT.getValue());
                preparedStatement.setInt(2, SENT_WITH_ERROR.getValue());
                preparedStatement.setInt(3, T01.getRouteId());
                preparedStatement.setInt(4, T02.getRouteId());
                preparedStatement.setInt(5, T03.getRouteId());
                preparedStatement.setInt(6, T02A.getRouteId());

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
            logger.error("Error al intentar actualizar las transacciones de EFECTIVO que se encuentran en troncales " +
                    "y deben de estar en el corredor NI", ex);
        }
    }
}
