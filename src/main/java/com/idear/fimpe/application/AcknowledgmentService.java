package com.idear.fimpe.application;

import com.idear.fimpe.cash.application.CashAcknowledgmentService;
import com.idear.fimpe.cash.infraestructure.CashSQLServerRepository;
import com.idear.fimpe.cet.application.CETAcknowlegmentService;
import com.idear.fimpe.cet.infraestructure.CETSQLServerRepository;
import com.idear.fimpe.counters.application.CountersAcknowledgmentService;
import com.idear.fimpe.counters.infraestructure.CountersSQLServerRepository;
import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.helpers.mail.Mail;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.fimpetransport.FimpeCommand;
import com.idear.fimpe.fimpetransport.FimpeException;
import com.idear.fimpe.kilometers.application.KilometersAcknowledgmentService;
import com.idear.fimpe.kilometers.infraestructure.KilometersSQLServerRepository;
import com.idear.fimpe.lam.application.LAMAcknowledgmentService;
import com.idear.fimpe.lam.domain.LAMTransaction;
import com.idear.fimpe.lam.infraestructure.FileReader;
import com.idear.fimpe.lam.infraestructure.LAMSQLServerRespository;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.remoterecharge.application.RemoteRechargeAcknowledgmentService;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeTransaction;
import com.idear.fimpe.remoterecharge.infraestructure.RemoteRechargeSQLServerRepository;
import com.idear.fimpe.torniquete.application.TorniqueteAcknowledgmentService;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteSQLServerRepository;
import com.idear.fimpe.vrt.application.VRTAcknowledgmentService;
import com.idear.fimpe.vrt.infraestructure.VRTSQLServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.idear.fimpe.enums.Device.*;
import static com.idear.fimpe.enums.ExtentionFile.*;
import static com.idear.fimpe.enums.Folder.*;
import static com.idear.fimpe.enums.PrefixFile.*;
import static com.idear.fimpe.properties.PropertiesHelper.DOWNLOAD_ATTEMPTS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class AcknowledgmentService {

    private final Logger logger = LoggerFactory.getLogger(AcknowledgmentService.class);
    private final FimpeCommand fimpeCommand;
    private final CETAcknowlegmentService cetAcknowlegmentService;
    private final VRTAcknowledgmentService vrtAcknowledgmentService;
    private final TorniqueteAcknowledgmentService torniqueteAcknowledgmentService;
    private final LAMAcknowledgmentService lamAcknowledgmentService;
    private final RemoteRechargeAcknowledgmentService remoteRechargeAcknowledgmentService;
    private final CashAcknowledgmentService cashAcknowledgmentService;
    private final CountersAcknowledgmentService countersAcknowledgmentService;
    private final KilometersAcknowledgmentService kilometersAcknowledgmentService;
    private final CommonRepository commonRepository;
    private List<LAMTransaction> lamTransactions;
    private List<RemoteRechargeTransaction> remoteRechargeTransactions;

    public AcknowledgmentService(CommonRepository commonRepository) {
        cetAcknowlegmentService = new CETAcknowlegmentService(new CETSQLServerRepository());
        vrtAcknowledgmentService = new VRTAcknowledgmentService(new VRTSQLServerRepository());
        torniqueteAcknowledgmentService = new TorniqueteAcknowledgmentService(new TorniqueteSQLServerRepository());
        lamAcknowledgmentService = new LAMAcknowledgmentService(new LAMSQLServerRespository());
        remoteRechargeAcknowledgmentService = new RemoteRechargeAcknowledgmentService(new RemoteRechargeSQLServerRepository());
        cashAcknowledgmentService = new CashAcknowledgmentService(new CashSQLServerRepository());
        countersAcknowledgmentService = new CountersAcknowledgmentService(new CountersSQLServerRepository());
        kilometersAcknowledgmentService = new KilometersAcknowledgmentService(new KilometersSQLServerRepository());


        fimpeCommand = new FimpeCommand();
        this.commonRepository = commonRepository;

        lamTransactions = new ArrayList<>();
        remoteRechargeTransactions = new ArrayList<>();
    }

    public void downloadAndProcess() {
        for (int i = 1; i <= DOWNLOAD_ATTEMPTS; i++) {
            try {
                logger.info("Vuelta numero {} de descarga de acuses", i);
                logger.info("Inicia la peticion de descarga");
                fimpeCommand.downloandFimpeResponses();
                waitForDownloadProcess();
                int filesToProccess = FileManager.listRegularFiles(DOWNLOAD.getPath()).size();
                if (filesToProccess > 0) {
                    logger.info("Archivos a procesar {} ", filesToProccess);
                    processFiles();
                }
                logger.info("Espera por 10 minutos ...");
                TimeUnit.MINUTES.sleep(10);
            } catch (FileManagerException | InterruptedException ex) {
                logger.error("", ex);
            }
        }
    }

    public void waitForDownloadProcess() {
        //Obtenemos la carpeta de descarga
        Path downloadFolder = Paths.get(DOWNLOAD.getPath());
        FileSystem fileSystem = downloadFolder.getFileSystem();

        //Generamos un servicio que estara vigilando los eventos
        //los archivos dentro de la carpeta
        try (WatchService watchService = fileSystem.newWatchService()) {

            //Registramos que el servicio solo nos avise cuando se cree un archivo
            WatchKey watchKey = downloadFolder.register(watchService, ENTRY_CREATE);

            //Tomamos el tiempo de inicio y le sumamos un minuto
            LocalTime nowPlusOneMinute = LocalTime.now().plusMinutes(5);

            //Si todavia no pasa el minuto
            while (LocalTime.now().isBefore(nowPlusOneMinute)) {

                //Revisamos si hemos tenido eventos
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    //Si se creo un archivo en la carpeta
                    if (kind == ENTRY_CREATE) {
                        //Recalculamos el minuto de tolerancia
                        nowPlusOneMinute = LocalTime.now().plusMinutes(5);
                    }
                    watchKey.reset();
                }
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * Consigue los archivos descargados en la carpeta download
     * los separa por extension (.OK y .ERR) y tambien separa las listas de acciones con el prefijo (LN)
     * por ultimo manda a procesar cada lista y envia un correo notificando cuantos archivos fueron procesados
     */
    public void processFiles() {

        try {
            Mail mail = new Mail();
            List<Path> files = FileManager.listRegularFiles(DOWNLOAD.getPath());
            List<Path> successfulFiles = new ArrayList<>();
            List<Path> errorFiles = new ArrayList<>();
            List<Path> lamFiles = new ArrayList<>();
            List<Path> remoteRechargeFiles = new ArrayList<>();

            int filesProceced;
            //Separa los archivos tipo ERROR, OK, LAM
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                if (fileName.contains(SUCCESSFUL_ACK.getExtention())) {
                    successfulFiles.add(file);
                } else if (fileName.contains(ExtentionFile.ERROR_ACK.getExtention())) {
                    errorFiles.add(file);
                } else if (fileName.contains(ACTION_LIST.getPrefix()) &&
                        (fileName.contains(NUMBER_CONTROL.getExtention()) ||
                                fileName.contains(DATA.getExtention()))) {
                    //Si es un LN_FOLIO_FECHA_PROVEEDOR.DAT o LN_FOLIO_FECHA_PROVEEDOR.CC
                    lamFiles.add(file);
                } else if (fileName.contains(REMOTE_RECHARGE.getPrefix()) &&
                        (fileName.contains(NUMBER_CONTROL.getExtention()) ||
                                fileName.contains(DATA.getExtention()))) {
                    //Si es un LR_FOLIO_FECHA_PROVEEDOR.DAT o LR_FOLIO_FECHA_PROVEEDOR.CC
                    remoteRechargeFiles.add(file);
                }
            }
            //En caso de tener archivos, los procesa y envia correo
            if (!successfulFiles.isEmpty()) {
                logger.info("Procesando archivos .OK");
                filesProceced = processSuccessfulFiles(successfulFiles);
                mail.sendMail("ACUSES DE PROCESAMIENTO EXITOSO FIMPE " + PropertiesHelper.PROJECT_NAME,
                        filesProceced + " acuses de procesamiento exitoso recibidos ");
            }
            if (!errorFiles.isEmpty()) {
                logger.info("Procesando archivos .ERR");
                filesProceced = processErrorFiles(errorFiles);
                mail.sendMail("ACUSES PROCESADOS CON ERROR FIMPE " + PropertiesHelper.PROJECT_NAME,
                        filesProceced + " acuses de procesamiento con error procesados");
            }
            if (!lamFiles.isEmpty()) {
                logger.info("Procesando archivos LAM");
                lamTransactions = processLAMFiles(lamFiles);
            }
            if (!remoteRechargeFiles.isEmpty()) {
                logger.info("Procesando archivos de RECARGA REMOTA");
                remoteRechargeTransactions = processRemoteRechargeFiles(remoteRechargeFiles);
            }

        } catch (FileManagerException e) {
            logger.error("", e);
        }
    }

    /**
     * Procesa los archivos con extension .OK
     * Primero extrae el folio de corte del nombre de archivo
     * busca el folio para saber a que dispositivo pertenece
     * por ultimo envia al servicio del dispositivo el archivo para que sea procesado
     *
     * @param files Lista de archivos a procesar
     * @return El numero de archivos procesados
     */
    private int processSuccessfulFiles(List<Path> files) {
        int filesProceced = 0;
        for (Path fileToProcess : files) {
            String fileToProcessName = fileToProcess.getFileName().toString();
            try {
                Long folioCut = getFolioCut(fileToProcess);

                //Se revisan primero los archivos de listas de accion o de efectivo
                //Ya que su folio de corte podria repetirse con el de dispositivos
                if (fileToProcessName.startsWith(ACTION_LIST.getPrefix())) {
                    filesProceced += lamAcknowledgmentService.processSuccessfulFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(REMOTE_RECHARGE.getPrefix())) {
                    filesProceced += remoteRechargeAcknowledgmentService.processSuccessfulFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(CASH.getPrefix())) {
                    filesProceced += cashAcknowledgmentService.processSuccesFulFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(KILOMETERS.getPrefix())) {
                    filesProceced += kilometersAcknowledgmentService.processSuccesFulFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(COUNTERS.getPrefix())) {
                    filesProceced += countersAcknowledgmentService.processSuccesFulFile(fileToProcess, folioCut);
                    continue;
                }


                //Si es un archivo de recargas o debitos
                String deviceName = commonRepository.getDeviceName(folioCut);

                if (deviceName.equalsIgnoreCase(CET.getName())) {
                    filesProceced += cetAcknowlegmentService.processSuccessfulFile(fileToProcess, folioCut);

                }
                if (deviceName.equalsIgnoreCase(VRT.getName())) {
                    filesProceced += vrtAcknowledgmentService.processSuccessfulFile(fileToProcess, folioCut);

                }
                if (deviceName.equalsIgnoreCase(TORNIQUETE.getName())) {
                    filesProceced += torniqueteAcknowledgmentService.processSuccessfulFile(fileToProcess, folioCut);
                }
                logger.info("Procesando el archivo {}, {} de {}", fileToProcessName, filesProceced, files.size());
            } catch (NumberFormatException ex) {
                logger.error("Error el intentar extraer el folio de corte en el archivo {} ",
                        fileToProcessName, ex);
            }
        }
        return filesProceced;
    }

    /**
     * Procesa los archivos con extension .ERR
     * Primero extrae el folio de corte del nombre de archivo
     * busca el folio para saber a que dispositivo pertenece
     * por ultimo envia al servicio del dispositivo el archivo para que sea procesado
     *
     * @param files Lista de archivos a procesar
     * @return El numero de archivos procesados
     */
    private int processErrorFiles(List<Path> files) {
        int filesProceced = 0;
        for (Path fileToProcess : files) {
            String fileToProcessName = fileToProcess.getFileName().toString();
            try {
                Long folioCut = getFolioCut(fileToProcess);

                //Se revisan primero los archivos de listas de accion o de efectivo
                //Ya que su folio de corte podria repetirse con el de dispositivos
                if (fileToProcessName.startsWith(ACTION_LIST.getPrefix())) {
                    filesProceced += lamAcknowledgmentService.processErrorFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(REMOTE_RECHARGE.getPrefix())) {
                    filesProceced += remoteRechargeAcknowledgmentService.processErrorFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(CASH.getPrefix())) {
                    filesProceced += cashAcknowledgmentService.processErrorFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(KILOMETERS.getPrefix())) {
                    filesProceced += kilometersAcknowledgmentService.processErrorFile(fileToProcess, folioCut);
                    continue;
                }

                if (fileToProcessName.startsWith(COUNTERS.getPrefix())) {
                    filesProceced += countersAcknowledgmentService.processErrorFile(fileToProcess, folioCut);
                    continue;
                }

                //Si es un archivo de debitos o recargas
                String deviceName = commonRepository.getDeviceName(folioCut);

                if (deviceName.equalsIgnoreCase(CET.getName())) {
                    filesProceced += cetAcknowlegmentService.processErrorFile(fileToProcess, folioCut);

                }

                if (deviceName.equalsIgnoreCase(VRT.getName())) {
                    filesProceced += vrtAcknowledgmentService.processErrorFile(fileToProcess, folioCut);

                }

                if (deviceName.equalsIgnoreCase(TORNIQUETE.getName())) {
                    filesProceced += torniqueteAcknowledgmentService.processErrorFile(fileToProcess, folioCut);

                }
                logger.info("Procesando el archivo {}, {} de {}", fileToProcessName, filesProceced, files.size());
            } catch (NumberFormatException ex) {
                logger.error("Error el intentar extraer el folio de corte en el archivo {}",
                        fileToProcessName, ex);
            }
        }
        return filesProceced;
    }

    private List<LAMTransaction> processLAMFiles(List<Path> lamFiles) {
        List<Path> datFiles = separateFilesForExtention(lamFiles, DATA);
        List<Path> numberControlFiles = separateFilesForExtention(lamFiles, NUMBER_CONTROL);
        String dataFileName;
        String numberControlFileName;

        for (Path numberControlFile : numberControlFiles) {
            try {
                FileManager.deleteFolderContent(INPUT.getPath());
                FileManager.deleteFolderContent(OUTPUT.getPath());
                numberControlFileName = numberControlFile.getFileName().toString();

                for (Path datFile : datFiles) {
                    dataFileName = datFile.getFileName().toString();
                    if (compareNameWithoutExtention(numberControlFileName, dataFileName)) {
                        numberControlFile = FileManager.moveFile(numberControlFile, INPUT.getPath());
                        datFile = FileManager.moveFile(datFile, INPUT.getPath());

                        fimpeCommand.descryptFile();
                        FileManager.folderContainsFiles(OUTPUT.getPath(), 1);
                        Path datFileDecrypt = FileManager.listRegularFiles(OUTPUT.getPath()).get(0);
                        List<LAMTransaction> transactions = FileReader.getLAMRequestFromFile(datFileDecrypt);
                        lamAcknowledgmentService.processLAMFile(transactions);
                        lamTransactions = transactions;
                        String folder = FileManager.createFolder(LAM.getPath(), OK_ACK.getPath());
                        FileManager.moveFile(datFileDecrypt, folder);
                    }
                }
            } catch (FileManagerException e) {
                logger.error("Error al intentar borrar la carpeta", e);
            } catch (FimpeException e) {
                logger.error("Error al intentar desencriptar los archivos", e);
            }
        }
        return lamTransactions;
    }

    private List<RemoteRechargeTransaction> processRemoteRechargeFiles(List<Path> remoteRechargeFiles) {

        List<Path> datFiles = separateFilesForExtention(remoteRechargeFiles, DATA);
        List<Path> numberControlFiles = separateFilesForExtention(remoteRechargeFiles, NUMBER_CONTROL);
        String dataFileName;
        String numberControlFileName;

        for (Path numberControlFile : numberControlFiles) {
            try {
                FileManager.deleteFolderContent(INPUT.getPath());
                FileManager.deleteFolderContent(OUTPUT.getPath());
                numberControlFileName = numberControlFile.getFileName().toString();

                for (Path datFile : datFiles) {
                    dataFileName = datFile.getFileName().toString();
                    if (compareNameWithoutExtention(numberControlFileName, dataFileName)) {
                        numberControlFile = FileManager.moveFile(numberControlFile, INPUT.getPath());
                        //El archivo es desencriptado pero no hacemos nada con el despues, como guardarlo
                        datFile = FileManager.moveFile(datFile, INPUT.getPath());

                        fimpeCommand.descryptFile();
                        FileManager.folderContainsFiles(OUTPUT.getPath(), 1);
                        Path datFileDecrypt = FileManager.listRegularFiles(OUTPUT.getPath()).get(0);
                        List<RemoteRechargeTransaction> transactions =
                                com.idear.fimpe.remoterecharge.infraestructure.FileReader.getRemoteRechargeRequestsFromFile(datFileDecrypt);
                        remoteRechargeAcknowledgmentService.processRemoteRechargeRequests(transactions);
                        remoteRechargeTransactions = transactions;

                        String folder = FileManager.createFolder(REMOTE_RECHARGES.getPath(), OK_ACK.getPath());
                        FileManager.moveFile(datFileDecrypt, folder);
                    }
                }
            } catch (FileManagerException e) {
                logger.error("Error al intentar borrar la carpeta", e);
            } catch (FimpeException e) {
                logger.error("Error al intentar desencriptar los archivos", e);
            }
        }
        return remoteRechargeTransactions;
    }

    private List<Path> separateFilesForExtention(List<Path> filesToSeparate, ExtentionFile extentionFile) {
        List<Path> separatesFiles = new ArrayList<>();
        for (Path file : filesToSeparate) {
            if (file.getFileName().toString().contains(extentionFile.getExtention())) {
                separatesFiles.add(file);
            }
        }
        return separatesFiles;
    }

    private boolean compareNameWithoutExtention(String fileOne, String fileTwo) {
        return fileOne.split("\\.")[0].equals(fileTwo.split("\\.")[0]);
    }

    /**
     * Extrae el folio del nombre del archivo
     * El archivo debera tener el siguiente formato D_1810538750_20201130130428_47_C01_BEA3.OK
     * siendo 1810538750 el folio a extraer
     *
     * @param fileToProcess Archivo
     * @return Folio extraido del nombre del archivo
     * @throws NumberFormatException Si el folio a extraer no es un numero
     */
    private Long getFolioCut(Path fileToProcess) throws NumberFormatException {
        return Long.valueOf(fileToProcess.getFileName().toString().split("_")[1]);
    }
}
