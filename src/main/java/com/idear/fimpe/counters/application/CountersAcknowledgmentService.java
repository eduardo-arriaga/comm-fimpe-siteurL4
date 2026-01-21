package com.idear.fimpe.counters.application;



import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.counters.domain.CountersSQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Folder.ERROR_ACK;
import static com.idear.fimpe.enums.Folder.OK_ACK;

public class CountersAcknowledgmentService {
    private CountersSQLRepository countersSQLRepository;
    private Logger logger = LoggerFactory.getLogger(CountersAcknowledgmentService.class);

    public CountersAcknowledgmentService(CountersSQLRepository countersSQLRepository){
        this.countersSQLRepository = countersSQLRepository;
    }

    public int processSuccesFulFile(Path fileToProcess, Long folioCut) {
        try {
            String fileName = fileToProcess.getFileName().toString();
            LocalDateTime receivedDate = LocalDateTime.now();
            String routeId = fileName.split("_")[4];
            countersSQLRepository.updateSuccessfulAckReceived(folioCut, receivedDate, fileName);
            String folder = FileManager.createFolder(routeId, OK_ACK.getPath());
            FileManager.moveFile(fileToProcess, folder);
            return 1;
        } catch (NumberFormatException ex) {
            logger.error("Error al intentar con seguir el corredor por medio del nombre del archivo "
                    + fileToProcess.getFileName().toString(), ex);

        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las transacciones con el folio " + folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }

    public int processErrorFile(Path fileToProcess, Long folioCut) {
        try {
            String routeId = fileToProcess.getFileName().toString().split("_")[4];
            LocalDateTime receivedDate = LocalDateTime.now();
            countersSQLRepository.updateErrorAckReceived(folioCut, receivedDate);
            String folder = FileManager.createFolder(routeId, ERROR_ACK.getPath());
            FileManager.moveFile(fileToProcess, folder);
            return 1;
        } catch (NumberFormatException ex) {
            logger.error("Error al intentar conseguir el corredor por medio del nombre del archivo " + fileToProcess.getFileName().toString(), ex);
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las transacciones con el folio " + folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }
}
