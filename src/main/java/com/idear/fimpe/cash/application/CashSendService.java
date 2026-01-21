package com.idear.fimpe.cash.application;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.cash.domain.CashFilesGenerator;
import com.idear.fimpe.cash.domain.CashNumberControl;
import com.idear.fimpe.cash.domain.CashSQLRepository;
import com.idear.fimpe.cash.infraestructure.CashFilesGeneratorException;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.idear.fimpe.properties.PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;

public class CashSendService {

    private Logger logger = LoggerFactory.getLogger(CashSendService.class);
    private CashSQLRepository cashSQLRepository;
    private CashFilesGenerator cashFilesGenerator;
    private FimpeCommand fimpeCommand;
    private int filesSent;

    public CashSendService(CashSQLRepository cashSQLRepository, CashFilesGenerator cashFilesGenerator) {
        this.cashSQLRepository = cashSQLRepository;
        this.cashFilesGenerator = cashFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int executeSend() {

        try {
            logger.info("-------  Inicia la asignacion de transacciones de EFECTIVO, de rutas troncales a la ruta NI  ---------- ");
            cashSQLRepository.updateTransactionsWithOutValidRoute();
            logger.info(" ----------- Inicia el envio de archivos de EFECTIVO ------------ ");

            LocalDate januaryFirst = LocalDate.of(2023, 1, 1);
            LocalDate yesterday = LocalDate.now().minusDays(1);

            //La fecha inicial para buscar
            LocalDateTime starDate = LocalDateTime.of(januaryFirst, LocalTime.of(0, 0));
            LocalDateTime endDate = LocalDateTime.of(yesterday, LocalTime.of(23, 59));

            List<CashNumberControl> cashNumberControlList = cashSQLRepository.getCashNumberControls(starDate, endDate);

            for (CashNumberControl cashNumberControl : cashNumberControlList) {

                try {
                    cashNumberControl.setCutId(cashSQLRepository.getCutFolio());
                    cashNumberControl.setCutDate(LocalDateTime.now());
                    cashNumberControl.setInitialCutDate(starDate);
                    cashNumberControl.setFinalCutDate(endDate);
                    cashNumberControl.setTechnologicalProvider(TECHNOLOGIC_PROVIDER_ID);
                    cashNumberControl.calculate();

                    cashFilesGenerator.generateFiles(cashNumberControl, PrefixFile.CASH);

                    fimpeCommand.setFileCC(cashFilesGenerator.getNumberControlFile());
                    fimpeCommand.setFileDAT(cashFilesGenerator.getDataFile());
                    fimpeCommand.setRouteId(cashNumberControl.getRouteIdDescription());

                    fimpeCommand.uploadFiles();

                    cashSQLRepository.updateCashNumberControlAsSent(cashNumberControl);

                    logger.info("Archivos {} y {} enviados correctamente",
                            cashFilesGenerator.getDataFile().getFileName(),
                            cashFilesGenerator.getNumberControlFile().getFileName());

                    filesSent++;
                } catch (CashFilesGeneratorException | FileManagerException | FimpeException | SQLException e) {
                    logger.error("Error al intentar generar los archivos de efectivo y enviarlos a FIMPE", e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return filesSent;
    }
}
