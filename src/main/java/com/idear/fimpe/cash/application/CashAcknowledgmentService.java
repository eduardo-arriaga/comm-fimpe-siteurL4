package com.idear.fimpe.cash.application;

import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.cash.domain.CashSQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Folder.ERROR_ACK;
import static com.idear.fimpe.enums.Folder.OK_ACK;

public class CashAcknowledgmentService {

    private CashSQLRepository cashSQLRepository;
    private Logger logger = LoggerFactory.getLogger(CashAcknowledgmentService.class);

    public CashAcknowledgmentService(CashSQLRepository cashSQLRepository){
        this.cashSQLRepository = cashSQLRepository;
    }

    public int processSuccesFulFile(Path fileToProcess, Long folioCut) {
        try {
            String fileName = fileToProcess.getFileName().toString();
            LocalDateTime receivedDate = LocalDateTime.now();
            String routeId = fileName.split("_")[4];
            cashSQLRepository.updateSuccessfulAckReceived(folioCut, receivedDate, fileName);
            String folder = FileManager.createFolder(routeId, OK_ACK.getPath());
            FileManager.moveFile(fileToProcess, folder);
            return 1;
        } catch (NumberFormatException ex) {
            logger.error("Error al intentar con seguir el corredor por medio del nombre del archivo " + fileToProcess.getFileName().toString(), ex);
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
            cashSQLRepository.updateErrorAckReceived(folioCut, receivedDate);
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
