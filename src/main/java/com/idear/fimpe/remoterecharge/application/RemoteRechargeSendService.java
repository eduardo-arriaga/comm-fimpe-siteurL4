package com.idear.fimpe.remoterecharge.application;

import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.remoterecharge.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.idear.fimpe.enums.Folder.REMOTE_RECHARGES;
import static com.idear.fimpe.properties.PropertiesHelper.TECHNOLOGIC_PROVIDER_ID;

public class RemoteRechargeSendService {

    private Logger logger = LoggerFactory.getLogger(RemoteRechargeSendService.class);
    private RemoteRechargeRepository remoteRechargeRepository;
    private RemoteRechargeFilesGenerator remoteRechargeFilesGenerator;
    private FimpeCommand fimpeCommand;

    public RemoteRechargeSendService(RemoteRechargeRepository remoteRechargeRepository,
                                     RemoteRechargeFilesGenerator remoteRechargeFilesGenerator) {

        this.remoteRechargeRepository = remoteRechargeRepository;
        this.remoteRechargeFilesGenerator = remoteRechargeFilesGenerator;
        fimpeCommand = new FimpeCommand();
    }

    public int send() {

        try {
            logger.info(" ----------- Inicia el envio de archivos de RECARGA REMOTA ------------ ");
            List<RemoteRechargeTransaction> remoteRechargeUnRequestedList = remoteRechargeRepository.getUnrequestedRemoteRecharge();
            List<RemoteRechargeTransaction> remoteRechargeConfirmedList = remoteRechargeRepository.getConfirmedRemoteRecharge();

            if(remoteRechargeUnRequestedList.size() > 0 || remoteRechargeConfirmedList.size() > 0) {

                RemoteRechargeNumberControl remoteRechargeNumberControl = new RemoteRechargeNumberControl();
                remoteRechargeNumberControl.setRequests(remoteRechargeUnRequestedList);
                remoteRechargeNumberControl.setConfirmations(remoteRechargeConfirmedList);
                remoteRechargeNumberControl.setCutFoil(remoteRechargeRepository.getCutFoil() + 1);
                remoteRechargeNumberControl.setGenerationDateTime(LocalDateTime.now());
                remoteRechargeNumberControl.setTechnologicProviderId(TECHNOLOGIC_PROVIDER_ID);
                remoteRechargeNumberControl.calculate();

                //Generar los archivos
                remoteRechargeFilesGenerator.generateFiles(remoteRechargeNumberControl);
                //Enviarlos a FIMPE
                fimpeCommand.setFileDAT(remoteRechargeFilesGenerator.getDataFile());
                fimpeCommand.setFileCC(remoteRechargeFilesGenerator.getNumberControlFile());
                fimpeCommand.setRouteId(REMOTE_RECHARGES.getPath());
                fimpeCommand.uploadFiles();

                //Actualizar el envio
                remoteRechargeRepository.updateAsSentRemoteRecharge(remoteRechargeNumberControl);
                //Actualizar el envio de las no confirmadas
                logger.info("Archvivos  de RECARGA REMOTA enviados con exito {}, {}",
                        remoteRechargeFilesGenerator.getDataFile().getFileName(),
                        remoteRechargeFilesGenerator.getNumberControlFile().getFileName());
                return 1;
            }
        } catch (SQLException | RemoteRechargeGeneratorException | FimpeException | FileManagerException e) {
            logger.error("Error al intentar enviar las RECARGAS REMOTAS", e);
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        return 0;
    }
}
