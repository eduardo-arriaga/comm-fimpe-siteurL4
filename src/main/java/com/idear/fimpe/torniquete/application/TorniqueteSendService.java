package com.idear.fimpe.torniquete.application;

import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.torniquete.domain.*;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteFilesGeneratorXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.idear.fimpe.enums.Device.TORNIQUETE;
import static com.idear.fimpe.properties.PropertiesHelper.MAX_TRANSACTIONS_PER_FILE;

public class TorniqueteSendService {

    private FimpeCommand fimpeCommand;
    private int filesSent;
    private TorniqueteRepository torniqueteRepository;
    private CommonRepository commonRepository;
    private TorniqueteFilesGenerator torniqueteFilesGenerator;
    private Logger logger = LoggerFactory.getLogger(TorniqueteSendService.class);
    private final String TORNIQUETE_TABLE = "wTransAbonoDisp";

    /**
     * Constructor de clase
     *
     * @param torniqueteRepository
     * @param commonRepository
     * @param torniqueteFilesGenerator
     */
    public TorniqueteSendService(TorniqueteRepository torniqueteRepository, CommonRepository commonRepository, TorniqueteFilesGenerator torniqueteFilesGenerator) {

        this.torniqueteRepository = torniqueteRepository;
        this.commonRepository = commonRepository;
        this.torniqueteFilesGenerator = torniqueteFilesGenerator;
        fimpeCommand = new FimpeCommand();

    }

    /**
     * Ejecuta los metodos necesarios para el envio de transacciones de torniquetes.
     *
     * @return Numero de archivos de Torniquete enviados.
     */
    public int executeSend() {
        logger.info(" ----------- Inicia el envio de torniquetes ------------ ");
        executeDebitSends();
        return filesSent;
    }

    /**
     * Ejecuta los metodos necesarios, para enviar las transacciones de torniquete y generar sus archivos.
     */
    private void executeDebitSends() {
        try {
            //Obtener lista de dispositivos con su estacion correspondiente
            List<TorniquteNumberControl> torniquteNumberControls = torniqueteRepository.getTorniqueteDevices();

            // Fecha actual establecida a menos 1 dia, y a las 11:59:59.
            LocalDateTime dateEndLimitToSearch = DateHelper.getYesterdayMidnight();

            //Fecha de la transaccion mas vieja que no se ha enviado de Torniquetes.
            LocalDateTime dateStartLimitToSearch = DateHelper.convertDateToZeroTime(
                    torniqueteRepository.getOldestTransactionDateNonExported(
                            OperationType.DEBIT_OK_TORNIQUETE,
                            OperationType.DEBIT_OK_GARITA,
                            OperationType.DEBIT_QR_OK_TORNIQUETE));

            //Ciclo de trabajo para cada elemento de la lista de dispositivos torniquteNumberControl.
            for (TorniquteNumberControl torniquteNumberControl : torniquteNumberControls) {

                //Obtener lista de transacciones no exportadas, del dispositivo actual de la lista torniquteNumberControl.
                logger.info("Obteniendo transacciones del torniquete {}", torniquteNumberControl.getDeviceId());
                List<TorniqueteTransaction> torniqueteTransactionNonExportedList = torniqueteRepository.getNonExportedTransaction(
                        torniquteNumberControl.getDeviceId(),
                        dateStartLimitToSearch,
                        dateEndLimitToSearch);
                logger.info("Finaliza la obtencion de transacciones");

                if (!torniqueteTransactionNonExportedList.isEmpty()) {

                    //Empaca las transacciones
                    List<TorniquteNumberControl> torniquteNumberControlListPackages =
                            torniquteNumberControl.getTorniqueteNumberControlList(
                                    torniqueteTransactionNonExportedList, MAX_TRANSACTIONS_PER_FILE);

                    for (TorniquteNumberControl torniquteNumberControlPackage : torniquteNumberControlListPackages) {
                        try {
                            //Calcular datos necesarios para la generacion de archivos.
                            torniquteNumberControlPackage.setCutId(commonRepository.getFoilCut());
                            torniquteNumberControlPackage.setCutDate(LocalDateTime.now());
                            torniquteNumberControlPackage.setInitialCutDate(dateStartLimitToSearch);
                            torniquteNumberControlPackage.setFinalCutDate(dateEndLimitToSearch);
                            torniquteNumberControlPackage.calculateNumberControl();

                            //Generar los archivos XML y subirlos.
                            logger.info("Generando archivos ");
                            torniqueteFilesGenerator.generateFiles(torniquteNumberControlPackage, PrefixFile.DEBIT);
                            fimpeCommand.setFileCC(torniqueteFilesGenerator.getNumberControlFile());
                            fimpeCommand.setFileDAT(torniqueteFilesGenerator.getDataFile());
                            fimpeCommand.setRouteId(torniquteNumberControlPackage.getRouteId() + File.separator + torniquteNumberControlPackage.getStationId());
                            fimpeCommand.uploadFiles();

                            //Actualizar transacciones enviadas,
                            logger.info("Actualizando envios ");
                            torniqueteRepository.updateTransactionsTorniquete(torniquteNumberControlPackage);
                            commonRepository.insertFoilCut(torniquteNumberControlPackage.getCutId(), TORNIQUETE.getName(), TORNIQUETE_TABLE);

                            logger.info("archivos {} y  {}  enviados correctamente",
                                    torniqueteFilesGenerator.getNumberControlFile().getFileName(),
                                    torniqueteFilesGenerator.getDataFile().getFileName());
                            filesSent++; //Aumenta en 1 el valor de archivos enviados.
                        } catch (SQLException | TorniqueteFilesGeneratorXMLException | FimpeException |
                                 FileManagerException e) {
                            logger.error("Error al intentar enviar los archivos debito TORNIQUETE ", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
