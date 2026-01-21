package com.idear.fimpe.kilometers.application;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.kilometers.domain.KilometersFilesGenerator;
import com.idear.fimpe.kilometers.domain.KilometersNumberControl;
import com.idear.fimpe.kilometers.domain.KilometersSQLRepository;
import com.idear.fimpe.kilometers.infraestructure.KilometersFileGeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.idear.fimpe.properties.PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;

public class KilometersSendService {

    private Logger logger = LoggerFactory.getLogger(KilometersSendService.class);
    private KilometersSQLRepository kilometersSQLRepository;
    private KilometersFilesGenerator kilometersFilesGenerator;
    private FimpeCommand fimpeCommand;
    private int filesSent;

    public KilometersSendService(KilometersSQLRepository kilometersSQLRepository,
                                 KilometersFilesGenerator kilometersFilesGenerator) {

        this.kilometersSQLRepository = kilometersSQLRepository;
        this.kilometersFilesGenerator = kilometersFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int executeSend() {

        try {
            logger.info(" ----------- Inicia el envio de archivos de KILOMETROS ------------ ");
            //La fecha inicial para buscar
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime starDate = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(yesterday, LocalTime.of(23, 59));

            List<KilometersNumberControl> kilometersNumberControlList =
                    kilometersSQLRepository.getKilometersNumberControls(starDate, endDate);

            for (KilometersNumberControl kilometersNumberControl : kilometersNumberControlList) {

                try {
                    kilometersNumberControl.setCutId(kilometersSQLRepository.getCutFolio());
                    kilometersNumberControl.setCutDate(LocalDateTime.now());
                    kilometersNumberControl.setInitialCutDate(starDate);
                    kilometersNumberControl.setFinalCutDate(endDate);
                    kilometersNumberControl.setTechnologicalProvider(TECHNOLOGIC_PROVIDER_ID);
                    kilometersNumberControl.calculate();

                    kilometersFilesGenerator.generateFiles(kilometersNumberControl, PrefixFile.KILOMETERS);

                    fimpeCommand.setFileCC(kilometersFilesGenerator.getNumberControlFile());
                    fimpeCommand.setFileDAT(kilometersFilesGenerator.getDataFile());
                    fimpeCommand.setRouteId(kilometersNumberControl.getRouteIdDescription());

                    fimpeCommand.uploadFiles();

                    kilometersSQLRepository.updateKilometersNumberControlAsSent(kilometersNumberControl);

                    logger.info("Archivos {} y {} enviados correctamente",
                            kilometersFilesGenerator.getDataFile().getFileName(),
                            kilometersFilesGenerator.getNumberControlFile().getFileName());

                    filesSent++;
                } catch (KilometersFileGeneratorException | FileManagerException | FimpeException | SQLException e) {
                    logger.error("Error al intentar generar los archivos de KILOMETROS y enviarlos a FIMPE", e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return filesSent;
    }
}
