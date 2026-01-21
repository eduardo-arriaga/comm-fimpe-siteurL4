package com.idear.fimpe.remoterecharge.application;

import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeRepository;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static com.idear.fimpe.enums.Folder.*;

public class RemoteRechargeAcknowledgmentService {

    private RemoteRechargeRepository remoteRechargeRepository;
    private Logger logger = LoggerFactory.getLogger(RemoteRechargeAcknowledgmentService.class);

    public RemoteRechargeAcknowledgmentService(RemoteRechargeRepository remoteRechargeRepository) {
        this.remoteRechargeRepository = remoteRechargeRepository;
    }

    public int processSuccessfulFile(Path file, Long folioCut){
        try{
            remoteRechargeRepository.updateSuccessfulAckReceived(folioCut);
            String folder = FileManager.createFolder(REMOTE_RECHARGES.getPath(), OK_ACK.getPath());
            FileManager.moveFile(file, folder);
            return 1;
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las peticiones de RECARGA REMOTA con folio {} ", folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }

    public int processErrorFile(Path file, Long folioCut){
        try{
            remoteRechargeRepository.updateErrorAckReceived(folioCut);
            String folder = FileManager.createFolder(REMOTE_RECHARGES.getPath(), ERROR_ACK.getPath());
            FileManager.moveFile(file, folder);
            return 1;
        }catch (NumberFormatException ex){
            logger.error("Error al intentar conseguir el corredor por medio del nombre del archivo " + file.getFileName().toString(), ex);
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las peticiones de RECARGA REMOTA con folio {} ", folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }

    public int processRemoteRechargeRequests(List<RemoteRechargeTransaction> remoteRechargeTransactions){
        try {
            remoteRechargeRepository.insertRequests(remoteRechargeTransactions);
        } catch (SQLException e) {
            logger.error("Error al intentar guardar la lista de RECARGAS REMOTAS");
        }
        return 0;
    }
}
