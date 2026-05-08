package com.idear.fimpe.vrt.application;

import com.idear.fimpe.enums.FimpeStatus;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.vrt.domain.VRTFilesGenerator;
import com.idear.fimpe.vrt.domain.VRTNumberControl;
import com.idear.fimpe.vrt.domain.VRTRepository;
import com.idear.fimpe.vrt.domain.VRTTransaction;
import com.idear.fimpe.vrt.infraestructure.VRTFilesGeneratorXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.idear.fimpe.enums.Device.VRT;
import static com.idear.fimpe.properties.PropertiesHelper.MAX_TRANSACTIONS_PER_FILE;

public class VRTSendService {

    private Logger logger = LoggerFactory.getLogger(VRTSendService.class);
    private VRTRepository vrtRepository;
    private CommonRepository commonRepository;
    private VRTFilesGenerator vrtFilesGenerator;
    private FimpeCommand fimpeCommand;
    private int filesProceced;
    private final String VRT_TABLE = "wTransAbonoDisp";

    public VRTSendService(VRTRepository vrtRepository, CommonRepository commonRepository, VRTFilesGenerator vrtFilesGenerator) {
        this.vrtRepository = vrtRepository;
        this.commonRepository = commonRepository;
        this.vrtFilesGenerator = vrtFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int send() {
        try {
            logger.info(" --------- Inicia el proceso de envios de la VRT ----------");

            logger.info("Iniciando proceso de revision de transacciones no pendientes de contestar");
            checkIfThereAreTransactionsWithNoAnswer();

            List<VRTNumberControl> vrtNumberControls = vrtRepository.getStations();
            for (VRTNumberControl vrtNumberControl : vrtNumberControls) {

                LocalDateTime dateFinalLimitToSearch;
                LocalDateTime dateStartLimitToSearch;

                if (PropertiesHelper.MAKE_SEND_BASED_ON_PERIOD_OF_DATES) {
                    dateStartLimitToSearch = PropertiesHelper.START_SEND_DATE;
                    dateFinalLimitToSearch = PropertiesHelper.END_SEND_DATE;
                } else {
                    dateFinalLimitToSearch = DateHelper.getYesterdayMidnight();
                    dateStartLimitToSearch = DateHelper.convertDateToZeroTime(
                            vrtRepository.getOldestTransactionDateNonExported(vrtNumberControl.getDeviceId()));
                }

                logger.info("Obteniendo transacciones de la VRT {}", vrtNumberControl.getDeviceId());
                List<VRTTransaction> vrtTransactions = vrtRepository.getVRTTransactionsNonExported(
                        vrtNumberControl.getDeviceId(), dateStartLimitToSearch, dateFinalLimitToSearch);

                logger.info("Obtencion de transacciones finalizada");
                if (!vrtTransactions.isEmpty()) {

                    List<VRTTransaction> vrtTransaccionsNewsOrWithError = vrtTransactions.stream()
                            .filter(vrtTransaction -> vrtTransaction.getFimpeStatus().equals(FimpeStatus.NOT_SENT) ||
                                    vrtTransaction.getFimpeStatus().equals(FimpeStatus.SENT_WITH_ERROR))
                            .collect(Collectors.toList());

                    if (!vrtTransaccionsNewsOrWithError.isEmpty()) {
                        logger.info("Se encontraron {} transacciones nuevas o con error", vrtTransaccionsNewsOrWithError.size());
                        makeSent(vrtNumberControl, vrtTransaccionsNewsOrWithError, dateStartLimitToSearch, dateFinalLimitToSearch);
                    }

                    List<VRTTransaction> vrtTransaccionsCardOutOfCatalog = vrtTransactions.stream()
                            .filter(torniqueteTransaction -> torniqueteTransaction.getFimpeStatus().equals(FimpeStatus.CARD_OUT_OF_CATALOG))
                            .collect(Collectors.toList());

                    if (!vrtTransaccionsCardOutOfCatalog.isEmpty()) {
                        logger.info("Se encontraron {} transacciones con tarjetas fuera de catalogo", vrtTransaccionsCardOutOfCatalog.size());
                        makeSent(vrtNumberControl, vrtTransaccionsCardOutOfCatalog, dateStartLimitToSearch, dateFinalLimitToSearch);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return filesProceced;
    }

    private void makeSent(VRTNumberControl vrtNumberControl,
                          List<VRTTransaction> vrtTransactions,
                          LocalDateTime dateStartLimitToSearch, LocalDateTime dateFinalLimitToSearch) {
        try {
            vrtNumberControl.setVrtTransactions(vrtTransactions);

            vrtNumberControl.setCutDate(LocalDateTime.now());
            vrtNumberControl.setInitialCutDate(dateStartLimitToSearch);
            vrtNumberControl.setFinalCutDate(dateFinalLimitToSearch);
            vrtNumberControl.setCutId(commonRepository.getFoilCut());
            vrtNumberControl.calculateNumberControl();

            logger.info("Generando archivo");

            vrtFilesGenerator.generateFiles(vrtNumberControl);

            fimpeCommand.setFileCC(vrtFilesGenerator.getNumberControlFile());
            fimpeCommand.setFileDAT(vrtFilesGenerator.getDataFile());
            fimpeCommand.setRouteId(vrtNumberControl.getRouteId() + File.separator + vrtNumberControl.getStationId());

            fimpeCommand.uploadFiles();

            logger.info("Actualizando envios ");
            vrtRepository.updateTransactionsSent(vrtNumberControl);
            logger.info("Insertando corte nuevo");
            commonRepository.insertFoilCut(vrtNumberControl.getCutId(), VRT.name(), VRT_TABLE);

            logger.info("archivos {} y {} enviados correctamente",
                    vrtFilesGenerator.getNumberControlFile().getFileName().toString(),
                    vrtFilesGenerator.getDataFile().getFileName().toString());
            filesProceced++;

        } catch (SQLException | FileManagerException | FimpeException |
                 VRTFilesGeneratorXMLException e) {
            logger.error("Error al intentar enviar los archivos debito TORNIQUETE ", e);
        }
    }

    private void checkIfThereAreTransactionsWithNoAnswer() {
        List<Long> packagesIds = vrtRepository.getPackagesWithNoAnswer(PropertiesHelper.DAYS_TO_CONSIDER_NO_ANSWER);
        if (!packagesIds.isEmpty()) {
            logger.info("Se encontraron {} paquetes sin respuesta de FIMPE, se actualizaran para reenvio", packagesIds.size());
            vrtRepository.updatePackagesWithNoAnswerAsNews(packagesIds);
        }
    }
}
