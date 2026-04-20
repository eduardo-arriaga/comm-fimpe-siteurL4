package com.idear.fimpe.torniquete.application;

import com.idear.fimpe.enums.FimpeStatus;
import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.torniquete.domain.*;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteFilesGeneratorXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        logger.info("Iniciando proceso de revision de transacciones no pendientes de contestar");
        checkIfThereAreTransactionsWithNoAnswer();

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
                    List<TorniqueteTransaction> torniqueteTransaccionsNewsOrWithError = torniqueteTransactionNonExportedList.stream()
                            .filter(torniqueteTransaction -> torniqueteTransaction.getFimpeStatus().equals(FimpeStatus.NOT_SENT) ||
                                    torniqueteTransaction.getFimpeStatus().equals(FimpeStatus.SENT_WITH_ERROR))
                            .collect(Collectors.toList());

                    if (!torniqueteTransaccionsNewsOrWithError.isEmpty()) {
                        logger.info("Se encontraron {} transacciones nuevas o con error", torniqueteTransaccionsNewsOrWithError.size());
                        makeSent(torniquteNumberControl, torniqueteTransaccionsNewsOrWithError, dateStartLimitToSearch, dateEndLimitToSearch);
                    }

                    List<TorniqueteTransaction> torniqueteTransaccionsCardOutOfCatalog = torniqueteTransactionNonExportedList.stream()
                            .filter(torniqueteTransaction -> torniqueteTransaction.getFimpeStatus().equals(FimpeStatus.CARD_OUT_OF_CATALOG))
                            .collect(Collectors.toList());

                    if (!torniqueteTransaccionsCardOutOfCatalog.isEmpty()) {
                        logger.info("Se encontraron {} transacciones con tarjetas fuera de catalogo", torniqueteTransaccionsCardOutOfCatalog.size());
                        makeSent(torniquteNumberControl, torniqueteTransaccionsCardOutOfCatalog, dateStartLimitToSearch, dateEndLimitToSearch);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void makeSent(TorniquteNumberControl torniquteNumberControl,
                          List<TorniqueteTransaction> torniqueteTransactions,
                          LocalDateTime dateStartLimitToSearch, LocalDateTime dateFinalLimitToSearch) {
        try {
            torniquteNumberControl.setTorniqueteTransactions(torniqueteTransactions);

            torniquteNumberControl.setCutDate(LocalDateTime.now());
            torniquteNumberControl.setInitialCutDate(dateStartLimitToSearch);
            torniquteNumberControl.setFinalCutDate(dateFinalLimitToSearch);
            torniquteNumberControl.setCutId(commonRepository.getFoilCut());
            torniquteNumberControl.calculateNumberControl();

            logger.info("Generando archivo");

            torniqueteFilesGenerator.generateFiles(torniquteNumberControl, PrefixFile.RECHARGE);

            fimpeCommand.setFileCC(torniqueteFilesGenerator.getNumberControlFile());
            fimpeCommand.setFileDAT(torniqueteFilesGenerator.getDataFile());
            fimpeCommand.setRouteId(torniquteNumberControl.getRouteId() + File.separator + torniquteNumberControl.getStationId());

            fimpeCommand.uploadFiles();

            logger.info("Actualizando envios ");
            torniqueteRepository.updateTransactionsTorniquete(torniquteNumberControl);

            logger.info("archivos {} y {} enviados correctamente",
                    torniqueteFilesGenerator.getNumberControlFile().getFileName().toString(),
                    torniqueteFilesGenerator.getDataFile().getFileName().toString());
            filesSent++;

        } catch (SQLException | FileManagerException | FimpeException |
                 TorniqueteFilesGeneratorXMLException e) {
            logger.error("Error al intentar enviar los archivos debito TORNIQUETE ", e);
        }
    }

    private void checkIfThereAreTransactionsWithNoAnswer() {
        List<Long> packagesIds = torniqueteRepository.getPackagesWithNoAnswer(PropertiesHelper.DAYS_TO_CONSIDER_NO_ANSWER);
        if (!packagesIds.isEmpty()) {
            logger.info("Se encontraron {} paquetes sin respuesta de FIMPE, se actualizaran para reenvio", packagesIds.size());
            torniqueteRepository.updatePackagesWithNoAnswerAsNews(packagesIds);
        }
    }
}
