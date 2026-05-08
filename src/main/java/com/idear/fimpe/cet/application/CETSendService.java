package com.idear.fimpe.cet.application;

import com.idear.fimpe.enums.FimpeStatus;
import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.cet.domain.*;
import com.idear.fimpe.cet.infraestructure.CETFilesGeneratorXMLException;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.properties.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        logger.info("Iniciando proceso de revision de transacciones no pendientes de contestar");
        checkIfThereAreTransactionsWithNoAnswer();

        logger.info("-------  Inicia el envio de debitos CET  ---------- ");
        executeDebitSends();
        logger.info("-------- Inicia el envio de recargas CET --------- ");
        executeRechargeSends();
        return filesSent;
    }

    private void executeDebitSends() {
        try {
            LocalDateTime dateFinalLimitToSearch;
            LocalDateTime dateStartLimitToSearch;

            if (PropertiesHelper.MAKE_SEND_BASED_ON_PERIOD_OF_DATES) {
                dateStartLimitToSearch = PropertiesHelper.START_SEND_DATE;
                dateFinalLimitToSearch = PropertiesHelper.END_SEND_DATE;
            } else {
                dateFinalLimitToSearch = DateHelper.getYesterdayMidnight();
                dateStartLimitToSearch = DateHelper.convertDateToZeroTime(
                        cetRepository.getOldestTransactionDateNonExported(
                                OperationType.DEBIT_OK_BPD_CET,
                                OperationType.DEBIT_OK_CET));
            }

            logger.info("Obteniendo lista de autobuses");
            List<CETNumberControl> cetNumberControlList =
                    cetRepository.getBusAndRouteList(dateStartLimitToSearch, dateFinalLimitToSearch);

            for (CETNumberControl cetNumberControl : cetNumberControlList) {
                logger.info("Obteniendo transacciones del autobus {}", cetNumberControl.getBusId());

                List<CETTransaction> cetTransactions =
                        cetRepository.getDebitTransactions(
                                cetNumberControl, dateStartLimitToSearch, dateFinalLimitToSearch);

                if (!cetTransactions.isEmpty()) {
                    List<CETTransaction> cetTransactionsNewsOrWithError = cetTransactions.stream()
                            .filter(transaction -> transaction.getFimpeStatus() == FimpeStatus.NOT_SENT || transaction.getFimpeStatus() == FimpeStatus.SENT_WITH_ERROR)
                            .collect(Collectors.toList());

                    if (!cetTransactionsNewsOrWithError.isEmpty()) {
                        logger.info("Se encontraron {} transacciones nuevas o con error", cetTransactionsNewsOrWithError.size());
                        makeSent(cetNumberControl, cetTransactionsNewsOrWithError, dateStartLimitToSearch, dateFinalLimitToSearch, PrefixFile.DEBIT);
                    }

                    List<CETTransaction> cetTransactionsWithCardOutOfCatalog = cetTransactions.stream()
                            .filter(transaction -> transaction.getFimpeStatus() == FimpeStatus.CARD_OUT_OF_CATALOG)
                            .collect(Collectors.toList());

                    if (!cetTransactionsWithCardOutOfCatalog.isEmpty()) {
                        logger.info("Se encontraron {} transacciones con tarjeta fuera de catalogo", cetTransactionsWithCardOutOfCatalog.size());
                        makeSent(cetNumberControl, cetTransactionsWithCardOutOfCatalog, dateStartLimitToSearch, dateFinalLimitToSearch, PrefixFile.DEBIT);
                    }
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void makeSent(CETNumberControl cetNumberControl, List<CETTransaction> cetTransactions,
                          LocalDateTime dateStartLimitToSearch, LocalDateTime dateFinalLimitToSearch,
                          PrefixFile prefixFile) {
        try {
            //Hace diferentes paquetes segun el maximo de transacciones
            List<CETNumberControl> cetNumberControlExtraPackages =
                    cetNumberControl.getCETNumberControlList(cetTransactions, MAX_TRANSACTIONS_PER_FILE);

            for (CETNumberControl cetNumberControlPackage : cetNumberControlExtraPackages) {

                cetNumberControlPackage.setCutDate(LocalDateTime.now());
                cetNumberControlPackage.setInitialCutDate(dateStartLimitToSearch);
                cetNumberControlPackage.setFinalCutDate(dateFinalLimitToSearch);
                cetNumberControlPackage.setCutId(commonRepository.getFoilCut());

                if (prefixFile.equals(PrefixFile.DEBIT))
                    cetNumberControlPackage.calculateNumberControlDebit();
                else
                    cetNumberControlPackage.calculateNumberControlRecharge();

                logger.info("Generando archivo para el autobus {}", cetNumberControlPackage.getBusId());
                cetFilesGenerator.generateFiles(cetNumberControlPackage, prefixFile);

                fimpeCommand.setFileCC(cetFilesGenerator.getNumberControlFile());
                fimpeCommand.setFileDAT(cetFilesGenerator.getDataFile());
                fimpeCommand.setRouteId(cetNumberControlPackage.getRouteIdDescription());

                fimpeCommand.uploadFiles();

                logger.info("Actualizando envios ");
                cetRepository.updateTransactionsSent(cetNumberControlPackage);
                logger.info("Insertando corte nuevo");
                commonRepository.insertFoilCut(cetNumberControlPackage.getCutId(), CET.name(), CET_TABLE);

                logger.info("archivos {} y {} enviados correctamente",
                        cetFilesGenerator.getNumberControlFile().getFileName().toString(),
                        cetFilesGenerator.getDataFile().getFileName().toString());
                filesSent++;
            }

        } catch (SQLException e) {
            logger.error("Error al intentar obtener informacion para envio del CET ", e);
        } catch (CETFilesGeneratorXMLException e) {
            logger.error("Error al intentar generar los archivos", e);
        } catch (FileManagerException | FimpeException e) {
            logger.error("Error al intentar subir los archivos ", e);
        }
    }

    private void executeRechargeSends() {
        try {
            LocalDateTime dateFinalLimitToSearch;
            LocalDateTime dateStartLimitToSearch;

            if (PropertiesHelper.MAKE_SEND_BASED_ON_PERIOD_OF_DATES) {
                dateStartLimitToSearch = PropertiesHelper.START_SEND_DATE;
                dateFinalLimitToSearch = PropertiesHelper.END_SEND_DATE;
            } else {
                dateFinalLimitToSearch = DateHelper.getYesterdayMidnight();
                dateStartLimitToSearch =
                        DateHelper.convertDateToZeroTime(cetRepository.getOldestTransactionDateNonExported(
                                OperationType.RECHARGE_OK_CET));
            }

            logger.info("Obteniendo lista de autobuses");
            List<CETNumberControl> cetNumberControlList =
                    cetRepository.getBusAndRouteList(dateStartLimitToSearch, dateFinalLimitToSearch);

            for (CETNumberControl cetNumberControl : cetNumberControlList) {

                logger.info("Obteniendo transacciones del autobus {}", cetNumberControl.getBusId());
                List<CETTransaction> cetTransactions =
                        cetRepository.getRechargeTransactions(
                                cetNumberControl, dateStartLimitToSearch, dateFinalLimitToSearch);
                if (!cetTransactions.isEmpty()) {
                    List<CETTransaction> cetTransactionsNewsOrWithError = cetTransactions.stream()
                            .filter(transaction -> transaction.getFimpeStatus() == FimpeStatus.NOT_SENT || transaction.getFimpeStatus() == FimpeStatus.SENT_WITH_ERROR)
                            .collect(Collectors.toList());

                    if (!cetTransactionsNewsOrWithError.isEmpty()) {
                        logger.info("Se encontraron {} transacciones nuevas o con error", cetTransactionsNewsOrWithError.size());
                        makeSent(cetNumberControl, cetTransactionsNewsOrWithError, dateStartLimitToSearch, dateFinalLimitToSearch, PrefixFile.DEBIT);
                    }

                    List<CETTransaction> cetTransactionsWithCardOutOfCatalog = cetTransactions.stream()
                            .filter(transaction -> transaction.getFimpeStatus() == FimpeStatus.CARD_OUT_OF_CATALOG)
                            .collect(Collectors.toList());

                    if (!cetTransactionsWithCardOutOfCatalog.isEmpty()) {
                        logger.info("Se encontraron {} transacciones con tarjeta fuera de catalogo", cetTransactionsWithCardOutOfCatalog.size());
                        makeSent(cetNumberControl, cetTransactionsWithCardOutOfCatalog, dateStartLimitToSearch, dateFinalLimitToSearch, PrefixFile.DEBIT);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void checkIfThereAreTransactionsWithNoAnswer() {
        List<Long> packagesIds = cetRepository.getPackagesWithNoAnswer(PropertiesHelper.DAYS_TO_CONSIDER_NO_ANSWER);
        if (!packagesIds.isEmpty()) {
            logger.info("Se encontraron {} paquetes sin respuesta de FIMPE, se actualizaran para reenvio", packagesIds.size());
            cetRepository.updatePackagesWithNoAnswerAsNews(packagesIds);
        }
    }
}
