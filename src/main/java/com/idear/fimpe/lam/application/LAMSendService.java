package com.idear.fimpe.lam.application;

import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.lam.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.idear.fimpe.enums.Folder.LAM;

public class LAMSendService {

    private Logger logger = LoggerFactory.getLogger(LAMSendService.class);
    private LAMRepository lamRepository;
    private LAMFilesGenerator lamFilesGenerator;
    private FimpeCommand fimpeCommand;

    public LAMSendService(LAMRepository lamRepository, LAMFilesGenerator lamFilesGenerator) {
        this.lamRepository = lamRepository;
        this.lamFilesGenerator = lamFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int send() {

        try {
            List<LAMTransaction> lamTransactionsUnRequested = lamRepository.getTransactionUnRequested();
            List<LAMTransaction> lamTransactionsConfirmed = lamRepository.getTransactionConfirmed();
            if(lamTransactionsUnRequested.size() > 0 || lamTransactionsConfirmed.size() > 0) {
                LAMNumberControl lamNumberControl = new LAMNumberControl();
                lamNumberControl.setLamConfirmationTransactions(lamTransactionsConfirmed);
                lamNumberControl.setLamRequestTransactions(lamTransactionsUnRequested);
                lamNumberControl.setFolioCut(lamRepository.getFolioCut() + 1);
                lamNumberControl.setDateTimeSend(LocalDateTime.now());
                lamNumberControl.setTotalRecords(lamTransactionsConfirmed.size() + lamTransactionsUnRequested.size());

                //Generar los archivos
                lamFilesGenerator.generateFiles(lamNumberControl);
                //Enviarlos a FIMPE
                fimpeCommand.setFileDAT(lamFilesGenerator.getDataFile());
                fimpeCommand.setFileCC(lamFilesGenerator.getNumberControlFile());
                fimpeCommand.setRouteId(LAM.getPath());
                fimpeCommand.uploadFiles();
                //Actualizar el envio
                lamRepository.updateTransactionSend(lamNumberControl);
                logger.info("Archvivos LAM enviados con exito {}, {}",
                        lamFilesGenerator.getDataFile().getFileName(),
                        lamFilesGenerator.getNumberControlFile().getFileName());
                return 1;
            }
        } catch (SQLException | LAMFilesGeneratorException | FimpeException | FileManagerException e) {
            logger.error("Error al intentar enviar las listas de accion", e);
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        return 0;
    }
}
