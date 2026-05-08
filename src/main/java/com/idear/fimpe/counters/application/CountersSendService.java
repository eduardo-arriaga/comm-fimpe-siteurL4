package com.idear.fimpe.counters.application;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.counters.domain.CountersFilesGenerator;
import com.idear.fimpe.counters.domain.CountersNumberControl;
import com.idear.fimpe.counters.domain.CountersSQLRepository;
import com.idear.fimpe.counters.infraestructure.CountersFileGeneratorException;
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

public class CountersSendService {

    private Logger logger = LoggerFactory.getLogger(CountersSendService.class);
    private CountersSQLRepository countersSQLRepository;
    private CountersFilesGenerator countersFilesGenerator;
    private FimpeCommand fimpeCommand;
    private int filesSent;

    public CountersSendService(CountersSQLRepository countersSQLRepository, CountersFilesGenerator countersFilesGenerator) {
        this.countersSQLRepository = countersSQLRepository;
        this.countersFilesGenerator = countersFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int executeSend() {

        try {
            logger.info(" ----------- Inicia el envio de archivos de CONTADORES ------------ ");

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

            List<CountersNumberControl> countersNumberControlList =
                    countersSQLRepository.getCountersNumberControls(dateStartLimitToSearch, dateFinalLimitToSearch);

            for (CountersNumberControl countersNumberControl : countersNumberControlList) {

                try {
                    countersNumberControl.setCutId(countersSQLRepository.getCutFolio());
                    countersNumberControl.setCutDate(LocalDateTime.now());
                    countersNumberControl.setInitialCutDate(dateStartLimitToSearch);
                    countersNumberControl.setFinalCutDate(dateFinalLimitToSearch);
                    countersNumberControl.setTechnologicalProvider(TECHNOLOGIC_PROVIDER_ID);
                    countersNumberControl.calculate();

                    countersFilesGenerator.generateFiles(countersNumberControl, PrefixFile.COUNTERS);

                    fimpeCommand.setFileCC(countersFilesGenerator.getNumberControlFile());
                    fimpeCommand.setFileDAT(countersFilesGenerator.getDataFile());
                    fimpeCommand.setRouteId(countersNumberControl.getRouteIdDescription());

                    fimpeCommand.uploadFiles();

                    countersSQLRepository.updateCountersNumberControlAsSent(countersNumberControl);

                    logger.info("Archivos {} y {} enviados correctamente",
                            countersFilesGenerator.getDataFile().getFileName(),
                            countersFilesGenerator.getNumberControlFile().getFileName());

                    filesSent++;
                } catch (CountersFileGeneratorException | FileManagerException | FimpeException | SQLException e) {
                    logger.error("Error al intentar generar los archivos de CONTADORES y enviarlos a FIMPE", e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return filesSent;
    }

    private void checkIfThereAreTransactionsWithNoAnswer() {
        List<Long> packagesIds = countersSQLRepository.getPackagesWithNoAnswer(PropertiesHelper.DAYS_TO_CONSIDER_NO_ANSWER);
        if (!packagesIds.isEmpty()) {
            logger.info("Se encontraron {} paquetes sin respuesta de FIMPE, se actualizaran para reenvio", packagesIds.size());
            countersSQLRepository.updatePackagesWithNoAnswerAsNews(packagesIds);
        }
    }
}
