package com.idear.fimpe.vrt.application;

import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
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
            List<VRTNumberControl> vrtNumberControls = vrtRepository.getStations();
            for (VRTNumberControl vrtNumberControl : vrtNumberControls) {

                LocalDateTime dateStartLimitToSearch = DateHelper.convertDateToZeroTime(
                        vrtRepository.getOldestTransactionDateNonExported(vrtNumberControl.getDeviceId()));

                LocalDateTime dateEndLimitToSearch = DateHelper.getYesterdayMidnight();

                logger.info("Obteniendo transacciones de la VRT {}", vrtNumberControl.getDeviceId());
                List<VRTTransaction> vrtTransactions = vrtRepository.getVRTTransactionsNonExported(
                        vrtNumberControl.getDeviceId(), dateStartLimitToSearch, dateEndLimitToSearch);

                logger.info("Obtencion de transacciones finalizada");
                if (!vrtTransactions.isEmpty()) {

                    List<VRTNumberControl> vrtNumberControlListPackages =
                            vrtNumberControl.getVRTNumberControlList(vrtTransactions, MAX_TRANSACTIONS_PER_FILE);

                    for (VRTNumberControl vrtNumberControlPackage : vrtNumberControlListPackages) {
                        try {
                            vrtNumberControlPackage.setCutId(commonRepository.getFoilCut());
                            vrtNumberControlPackage.setCutDate(LocalDateTime.now());
                            vrtNumberControlPackage.setInitialCutDate(dateStartLimitToSearch);
                            vrtNumberControlPackage.setFinalCutDate(dateEndLimitToSearch);
                            vrtNumberControlPackage.calculateNumberControl();
                            logger.info("Generando archivos ");
                            vrtFilesGenerator.generateFiles(vrtNumberControlPackage);
                            fimpeCommand.setFileCC(vrtFilesGenerator.getNumberControlFile());
                            fimpeCommand.setFileDAT(vrtFilesGenerator.getDataFile());
                            fimpeCommand.setRouteId(vrtNumberControlPackage.getRouteId() + File.separator + vrtNumberControlPackage.getStationId());
                            fimpeCommand.uploadFiles();
                            logger.info("Actualizando envios ");
                            vrtRepository.updateTransactionsSent(vrtNumberControlPackage);
                            commonRepository.insertFoilCut(vrtNumberControlPackage.getCutId(), VRT.getName(), VRT_TABLE);

                            logger.info("archivos  {} y {} enviados correctamente",
                                    vrtFilesGenerator.getNumberControlFile().getFileName(),
                                    vrtFilesGenerator.getDataFile().getFileName());
                            filesProceced++;
                        } catch (SQLException | VRTFilesGeneratorXMLException | FileManagerException |
                                 FimpeException ex) {
                            logger.error("Error al intentar enviar los archivos venta/recarga VRT ", ex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return filesProceced;
    }
}
