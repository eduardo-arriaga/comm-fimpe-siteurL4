package com.idear.fimpe.cet.application;

import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.cet.domain.CETRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static com.idear.fimpe.enums.Folder.*;

public class CETAcknowlegmentService {

    private CETRepository cetRepository;
    private final Logger logger = LoggerFactory.getLogger(CETAcknowlegmentService.class);

    public CETAcknowlegmentService(CETRepository cetRepository){
        this.cetRepository = cetRepository;
    }

    /**
     * Procesa los acuses .OK para el CET
     * actualiza en base de datos todas las transacciones que tengan el mismo folio
     * y almacena el archivo en ok_files/[routeId]
     * @param file Archivo a procesar
     * @param folioCut Folio de corte
     * @return Archivos procesados
     */
    public int processSuccessfulFile(Path file, Long folioCut){
        try{
            String fileName = file.getFileName().toString();
            LocalDateTime receivedDate = LocalDateTime.now();
            String routeId = fileName.split("_")[4];
            cetRepository.updateSuccessfulAckReceived(folioCut, receivedDate, fileName);
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

/**
 * Procesa los acuses .ERR para el CET
 * actualiza en base de datos todas las transacciones que tengan el mismo folio
 * y almacena el archivo en error_files/[routeId]
 * @param file Archivo a procesar
 * @param folioCut Folio de corte
 * @return Archivos procesados
 * */
    public int processErrorFile(Path file, Long folioCut){
        try{
            String routeId = file.getFileName().toString().split("_")[4];
            LocalDateTime receivedDate = LocalDateTime.now();
            cetRepository.updateErrorAckReceived(folioCut, receivedDate);
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
