package com.idear.fimpe.cash.application;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.cash.domain.CashFilesGenerator;
import com.idear.fimpe.cash.domain.CashNumberControl;
import com.idear.fimpe.cash.domain.CashSQLRepository;
import com.idear.fimpe.cash.infraestructure.CashFilesGeneratorException;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.properties.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
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
            logger.info(" ----------- Inicia el envio de archivos de EFECTIVO ------------ ");

            logger.info("Iniciando proceso de revision de transacciones no pendientes de contestar");
            checkIfThereAreTransactionsWithNoAnswer();

            LocalDateTime dateFinalLimitToSearch;
            LocalDateTime dateStartLimitToSearch;

            if (PropertiesHelper.MAKE_SEND_BASED_ON_PERIOD_OF_DATES) {
                dateStartLimitToSearch = PropertiesHelper.START_SEND_DATE;
                dateFinalLimitToSearch = PropertiesHelper.END_SEND_DATE;
            } else {
                LocalDateTime now = LocalDateTime.now();
                dateStartLimitToSearch = LocalDateTime.of(now.getYear(), Month.JANUARY, 1, 0, 0, 0);
                dateFinalLimitToSearch = now.minusDays(1).withHour(23).withMinute(59).withSecond(59);
            }

            List<CashNumberControl> cashNumberControlList =
                    cashSQLRepository.getCashNumberControls(dateStartLimitToSearch, dateFinalLimitToSearch);

            for (CashNumberControl cashNumberControl : cashNumberControlList) {

                try {
                    cashNumberControl.setCutId(cashSQLRepository.getCutFolio());
                    cashNumberControl.setCutDate(LocalDateTime.now());
                    cashNumberControl.setInitialCutDate(dateStartLimitToSearch);
                    cashNumberControl.setFinalCutDate(dateFinalLimitToSearch);
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

    private void checkIfThereAreTransactionsWithNoAnswer() {
        List<Long> packagesIds = cashSQLRepository.getPackagesWithNoAnswer(PropertiesHelper.DAYS_TO_CONSIDER_NO_ANSWER);
        if (!packagesIds.isEmpty()) {
            logger.info("Se encontraron {} paquetes sin respuesta de FIMPE, se actualizaran para reenvio", packagesIds.size());
            cashSQLRepository.updatePackagesWithNoAnswerAsNews(packagesIds);
        }
    }
}
