package com.idear.fimpe.lam.application;

import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.lam.domain.LAMRepository;
import com.idear.fimpe.lam.domain.LAMTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.Folder.*;

public class LAMAcknowledgmentService {
    private Logger logger = LoggerFactory.getLogger(LAMAcknowledgmentService.class);
    private LAMRepository lamRepository;

    public LAMAcknowledgmentService(LAMRepository lamRepository) {
        this.lamRepository = lamRepository;
    }

    public int processSuccessfulFile(Path file, Long folioCut){
        try{
            lamRepository.updateSuccessfulAckReceived(folioCut);
            String folder = FileManager.createFolder(Folder.LAM.getPath(), OK_ACK.getPath());
            FileManager.moveFile(file, folder);
            return 1;
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las transacciones con el folio " + folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }

    public int processErrorFile(Path file, Long folioCut){
        try{
            lamRepository.updateErrorAckReceived(folioCut);
            String folder = FileManager.createFolder(LAM.getPath(), ERROR_ACK.getPath());
            FileManager.moveFile(file, folder);
            return 1;
        }catch (NumberFormatException ex){
            logger.error("Error al intentar conseguir el corredor por medio del nombre del archivo " + file.getFileName().toString(), ex);
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las transacciones con el folio " + folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }

    public int processLAMFile(List<LAMTransaction> transactions){
        try {
            List<LAMTransaction> oldLAMTransactions = lamRepository.getAllLAMTransactions();

            //get the new elements to insert
            List<LAMTransaction> lamTransactionsToInsert = new ArrayList<>(transactions);
            lamTransactionsToInsert.removeAll(oldLAMTransactions);

            List<LAMTransaction> lamTransactionsToDelete = new ArrayList<>(oldLAMTransactions);
            lamTransactionsToDelete.removeAll(transactions);

            logger.info("Insertando {} transacciones ", lamTransactionsToInsert.size());
            lamRepository.insertLAMTransactions(lamTransactionsToInsert);
            logger.info("Eliminando {} transacciones ", lamTransactionsToDelete.size());
            lamRepository.deleteLAMTransactions(lamTransactionsToDelete);

        } catch (SQLException e) {
            logger.error("Error al intentar grabar la lista LAM :{}", e.getMessage());
        }
        return 0;
    }
}
