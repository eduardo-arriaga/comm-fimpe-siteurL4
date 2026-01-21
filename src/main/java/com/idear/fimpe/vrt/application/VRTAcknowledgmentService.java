package com.idear.fimpe.vrt.application;

import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.vrt.domain.VRTRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Folder.ERROR_ACK;
import static com.idear.fimpe.enums.Folder.OK_ACK;

public class VRTAcknowledgmentService {
    private VRTRepository vrtRepository;
    private Logger logger = LoggerFactory.getLogger(VRTAcknowledgmentService.class);

    public VRTAcknowledgmentService(VRTRepository vrtRepository) {
        this.vrtRepository = vrtRepository;
    }

    public int processSuccessfulFile(Path file, Long folioCut){
        try{
            String fileName = file.getFileName().toString();
            LocalDateTime receivedDate = LocalDateTime.now();
            String routeId = fileName.split("_")[4];
            vrtRepository.updateSuccessfulAckReceived(folioCut, receivedDate, fileName);
            String folder = FileManager.createFolder(routeId, OK_ACK.getPath());
            FileManager.moveFile(file, folder);
            return 1;
        }catch (NumberFormatException ex){
            logger.error("Error al intentar con seguir el corredor por medio del nombre del archivo " + file.getFileName().toString(), ex);
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las transacciones con el folio " + folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }

    public int processErrorFile(Path file, Long folioCut){
        try{
            String routeId = file.getFileName().toString().split("_")[4];
            LocalDateTime receivedDate = LocalDateTime.now();
            vrtRepository.updateErrorAckReceived(folioCut, receivedDate);
            String folder = FileManager.createFolder(routeId, ERROR_ACK.getPath());
            FileManager.moveFile(file, folder);
            return 1;
        }catch (NumberFormatException ex){
            logger.error("Error al intentar con seguir el corredor por medio del nombre del archivo " + file.getFileName().toString(), ex);
        } catch (SQLException ex) {
            logger.error("Error al intentar actualizar las transacciones con el folio " + folioCut, ex);
        } catch (FileManagerException ex) {
            logger.error("", ex);
        }
        return 0;
    }
}
