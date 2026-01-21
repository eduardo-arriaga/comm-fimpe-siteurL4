package com.idear.fimpe.cet.application;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.cet.domain.*;
import com.idear.fimpe.cet.infraestructure.CETFilesGeneratorXMLException;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.idear.fimpe.enums.Device.CET;
import static com.idear.fimpe.properties.PropertiesHelper.MAX_TRANSACTIONS_PER_FILE;

public class CETSendService {
    private CETRepository cetRepository;
    private CommonRepository commonRepository;
    private CETFilesGenerator cetFilesGenerator;
    private FimpeCommand fimpeCommand;
    private int filesSent;
    private final String CET_TABLE = "wTransTarjetas";

    private Logger logger = LoggerFactory.getLogger(CETSendService.class);

    public CETSendService(CETRepository cetRepository, CommonRepository commonRepository, CETFilesGenerator cetFilesGenerator) {
        this.cetRepository = cetRepository;
        this.commonRepository = commonRepository;
        this.cetFilesGenerator = cetFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int executeSend() {
        logger.info("-------  Inicia el envio de debitos CET  ---------- ");
        executeDebitSends();
        logger.info("-------- Inicia el envio de recargas CET --------- ");
        executeRechargeSends();
        return filesSent;
    }

    private void executeDebitSends() {
        try {
            //Sacar el rango de fechas de la fecha actual establecida a menos 1 dia, y a las 11:59:59
            LocalDateTime dateFinalLimitToSearch = DateHelper.getYesterdayMidnight();
            //y la fecha de la transaccion mas vieja que no se ha enviado y que pertenezca a una ruta que no sea la default

            LocalDateTime dateStartLimitToSearch =
                    DateHelper.convertDateToZeroTime(cetRepository.getOldestTransactionDateNonExported(
                            OperationType.DEBIT_OK_BPD_CET, OperationType.DEBIT_OK_CET));

            logger.info("Obteniendo lista de autobuses");
            List<CETNumberControl> cetNumberControlList =
                    cetRepository.getBusAndRouteList(dateStartLimitToSearch, dateFinalLimitToSearch);

            for (CETNumberControl cetNumberControl : cetNumberControlList) {
                logger.info("Obteniendo transacciones del autobus {}", cetNumberControl.getBusId());

                List<CETTransaction> cetTransactions =
                        cetRepository.getDebitTransactions(
                                cetNumberControl, dateStartLimitToSearch, dateFinalLimitToSearch);

                if (!cetTransactions.isEmpty()) {
                    try {
                        //HAce diferentes paquetes segun el maximo de transacciones
                        List<CETNumberControl> cetNumberControlExtraPackages =
                                cetNumberControl.getCETNumberControlList(cetTransactions, MAX_TRANSACTIONS_PER_FILE);

                        for (CETNumberControl cetNumberControlPackage : cetNumberControlExtraPackages) {

                            cetNumberControlPackage.setCutDate(LocalDateTime.now());
                            cetNumberControlPackage.setInitialCutDate(dateStartLimitToSearch);
                            cetNumberControlPackage.setFinalCutDate(dateFinalLimitToSearch);
                            cetNumberControlPackage.setCutId(commonRepository.getFoilCut());
                            cetNumberControlPackage.calculateNumberControlDebit();

                            logger.info("Generando archivo para el autobus {}", cetNumberControlPackage.getBusId());
                            cetFilesGenerator.generateFiles(cetNumberControlPackage, PrefixFile.DEBIT);

                            fimpeCommand.setFileCC(cetFilesGenerator.getNumberControlFile());
                            fimpeCommand.setFileDAT(cetFilesGenerator.getDataFile());
                            fimpeCommand.setRouteId(cetNumberControlPackage.getRouteIdDescription());

                            fimpeCommand.uploadFiles();
                            logger.info("Actualizando envios ");
                            cetRepository.updateTransactionsSent(cetNumberControlPackage);
                            commonRepository.insertFoilCut(cetNumberControlPackage.getCutId(), CET.getName(), CET_TABLE);
                            logger.info("archivos {} y {} enviados correctamente",
                                    cetFilesGenerator.getNumberControlFile().getFileName().toString(),
                                    cetFilesGenerator.getDataFile().getFileName().toString());
                            filesSent++;
                        }

                    } catch (SQLException e) {
                        logger.error("Error al intentar obtener informacion para envio de debito del CET ", e);
                    } catch (CETFilesGeneratorXMLException e) {
                        logger.error("Error al intentar generar los archivos de debito ", e);
                    } catch (FileManagerException | FimpeException e) {
                        logger.error("Error al intentar subir los archivos de debito ", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void executeRechargeSends() {
        try {
            //Sacar el rango de fechas de la fecha actual establecida a menos 1 dia, y a las 11:59:59
            LocalDateTime dateFinalLimitToSearch = DateHelper.getYesterdayMidnight();
            //y la fecha de la transaccion mas vieja que no se ha enviado y que pertenezca a una ruta que no sea la default
            LocalDateTime dateStartLimitToSearch =
                    DateHelper.convertDateToZeroTime(cetRepository.getOldestTransactionDateNonExported(
                            OperationType.RECHARGE_OK_CET));
            logger.info("Obteniendo lista de autobuses");
            List<CETNumberControl> cetNumberControlList =
                    cetRepository.getBusAndRouteList(dateStartLimitToSearch, dateFinalLimitToSearch);

            for (CETNumberControl cetNumberControl : cetNumberControlList) {

                logger.info("Obteniendo transacciones del autobus {}", cetNumberControl.getBusId());
                List<CETTransaction> cetTransactions =
                        cetRepository.getRechargeTransactions(
                                cetNumberControl, dateStartLimitToSearch, dateFinalLimitToSearch);
                if (!cetTransactions.isEmpty()) {

                    //Divide los paquetes segun el maximo
                    List<CETNumberControl> cetNumberControlExtraPackages =
                            cetNumberControl.getCETNumberControlList(cetTransactions, MAX_TRANSACTIONS_PER_FILE);

                    for (CETNumberControl cetNumberControlPackage : cetNumberControlExtraPackages) {
                        try {
                            cetNumberControlPackage.setCutDate(LocalDateTime.now());
                            cetNumberControlPackage.setInitialCutDate(dateStartLimitToSearch);
                            cetNumberControlPackage.setFinalCutDate(dateFinalLimitToSearch);
                            cetNumberControlPackage.setCutId(commonRepository.getFoilCut());
                            cetNumberControlPackage.calculateNumberControlRecharge();
                            logger.info("Generando archivo para el autobus {}", cetNumberControlPackage.getBusId());
                            cetFilesGenerator.generateFiles(cetNumberControlPackage, PrefixFile.RECHARGE);

                            fimpeCommand.setFileCC(cetFilesGenerator.getNumberControlFile());
                            fimpeCommand.setFileDAT(cetFilesGenerator.getDataFile());
                            fimpeCommand.setRouteId(cetNumberControlPackage.getRouteIdDescription());

                            fimpeCommand.uploadFiles();
                            logger.info("Actualizando envios ");
                            cetRepository.updateTransactionsSent(cetNumberControlPackage);
                            commonRepository.insertFoilCut(cetNumberControlPackage.getCutId(), CET.getName(), CET_TABLE);
                            logger.info("archivos {} y {} enviados correctamente",
                                    cetFilesGenerator.getNumberControlFile().getFileName().toString(),
                                    cetFilesGenerator.getDataFile().getFileName().toString());
                            filesSent++;

                        } catch (SQLException e) {
                            logger.error("Error al intentar obtener informacion para envio de debito del CET ", e);
                        } catch (CETFilesGeneratorXMLException e) {
                            logger.error("Error al intentar generar los archivos de debito ", e);
                        } catch (FileManagerException | FimpeException e) {
                            logger.error("Error al intentar subir los archivos de debito ", e);
                        }
                    }

                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
