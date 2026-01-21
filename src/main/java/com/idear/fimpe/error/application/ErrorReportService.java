package com.idear.fimpe.error.application;

import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.database.CommonRepository;
import com.idear.fimpe.error.domain.*;
import com.idear.fimpe.error.infraestructure.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.idear.fimpe.enums.Device.*;
import static com.idear.fimpe.enums.Folder.ERROR_ACK;

public class ErrorReportService {

    private ExecutorService executorService;
    private Logger logger = LoggerFactory.getLogger(ErrorReportService.class);
    private List<ErrorBusStation> errorBusStationList;
    private List<ErrorTransaction> errorTransactionList;
    private List<ErrorCountersKilometersCash> errorCountersKilometersCashList;
    private List<ErrorNumberControl> errorNumberControlList;
    private CommonRepository commonRepository;
    private ErrorRepository errorRepository;
    private Workbook workbook;
    private static int MAX_RECORDS = 65530;

    public ErrorReportService(CommonRepository commonRepository, ErrorRepository errorRepository,
                              Workbook workbook) {
        executorService = Executors.newFixedThreadPool(50);
        errorBusStationList = new ArrayList<>();
        errorTransactionList = new ArrayList<>();
        errorCountersKilometersCashList = new ArrayList<>();
        errorNumberControlList = new ArrayList<>();
        this.commonRepository = commonRepository;
        this.errorRepository = errorRepository;
        this.workbook = workbook;
    }

    public void createReport() {

        extractInfoFromErrorFiles();
        logger.info("Se recolectaron {} errores de autobus-estacion y {} de transacciones",
                errorBusStationList.size(), errorTransactionList.size());

        logger.info("Obteniendo informacion de los errores en base de datos");
        collectInfoFromRepositoy(errorBusStationList, errorTransactionList);
        logger.info("Termina de obtener informacion de los errores en base de datos");

        //Crea la tabla de errores de autobuses o estaciones
        ErrorBusStationReportExcel errorBusStationReportExcel = new ErrorBusStationReportExcel(errorBusStationList, workbook);
        errorBusStationReportExcel.createTable();
        //Crea la tabla de errores de transacciones de debitos y recargas
        //Revisar cuantos errores son para repartirlos entre las hojas
        List<ErrorTransactionReportExcel> errorTransactionReportExcelList = groupErrorTransaccions(errorTransactionList, workbook);
        for (ErrorTransactionReportExcel errorTransactionReportExcel : errorTransactionReportExcelList) {
            errorTransactionReportExcel.createTable();
        }

        CountersKilometersCashReportExcel countersKilometersCashReportExcel = new CountersKilometersCashReportExcel(errorCountersKilometersCashList, workbook);
        countersKilometersCashReportExcel.createTable();

        ErrorNumberControlReportExcel errorNumberControlReportExcel = new ErrorNumberControlReportExcel(errorNumberControlList, workbook);
        errorNumberControlReportExcel.createTable();
    }

    private List<ErrorTransactionReportExcel> groupErrorTransaccions(List<ErrorTransaction> errorTransactionList, Workbook workbook) {
        List<ErrorTransactionReportExcel> errorTransactionReportExcelList = new ArrayList<>();
        if (errorTransactionList.size() > MAX_RECORDS) {
            int totalSheet = errorTransactionList.size() / MAX_RECORDS;
            int totalSheetResiduo = errorTransactionList.size() % MAX_RECORDS;

            if (totalSheetResiduo > 0)
                totalSheet++;

            int indexLow = 0;
            int indexHigh = MAX_RECORDS;

            for (int i = 0; i < totalSheet; i++) {
                if (i == totalSheet - 1)
                    indexHigh = indexLow + totalSheetResiduo;

                List<ErrorTransaction> errorTransactionsAux = errorTransactionList.subList(indexLow, indexHigh);
                ErrorTransactionReportExcel errorTransactionReportExcel = new ErrorTransactionReportExcel(errorTransactionsAux, workbook, i);
                errorTransactionReportExcelList.add(errorTransactionReportExcel);
                indexLow = indexHigh;
                indexHigh += MAX_RECORDS;
            }
        } else {
            ErrorTransactionReportExcel errorTransactionReportExcel = new ErrorTransactionReportExcel(errorTransactionList, workbook, 1);
            errorTransactionReportExcelList.add(errorTransactionReportExcel);
        }
        return errorTransactionReportExcelList;
    }

    private void collectInfoFromRepositoy(List<ErrorBusStation> errorBusStationList,
                                          List<ErrorTransaction> errorTransactionList) {
        for (ErrorBusStation errorBusStation : errorBusStationList) {
            String deviceName = commonRepository.getDeviceName(errorBusStation.getCutFoil());
            if (deviceName.equals(CET.getName())) {
                errorRepository.getBusInfo(errorBusStation);
            }
            if (deviceName.equals(VRT.getName()) || deviceName.equals(TORNIQUETE.getName())) {
                errorRepository.getStationInfo(errorBusStation);
            }
        }

        for (ErrorTransaction errorTransaction : errorTransactionList) {
            String deviceName = commonRepository.getDeviceName(errorTransaction.getCutFoil());

            if (deviceName.equals(CET.getName())) {
                errorRepository.getBusTransactionInfo(errorTransaction);
            }
            if (deviceName.equals(VRT.getName()) || deviceName.equals(TORNIQUETE.getName())) {
                //Si el folio de tarjeta es 0 entonces es QR o bancaria
                if (errorTransaction.getCardFoil() == 0) {
                    //Si es igual a 6 es QR Debito, o si es un archivo de recarga es QR de VRT
                    if(errorTransaction.getCardId().length() == 6 || errorTransaction.getFileName().startsWith("R_"))
                        errorRepository.getStationTransactionQRInfo(errorTransaction);

                    //Es bancaria
                    else if(errorTransaction.getCardId().length() > 14){
                        errorRepository.getStationTransactionBancariaInfo(errorTransaction);
                    }
                } else {
                    errorRepository.getStationTransactionInfo(errorTransaction);
                }
            }
        }
    }

    private void extractInfoFromErrorFiles() {
        try {
            List<Path> errorFiles = FileManager.getRegularFilesCreatedToday(ERROR_ACK.getPath());
            logger.info("Se procesaran {} archivos de error", errorFiles.size());
            for (Path errorFile : errorFiles) {
                executorService.submit(new ErrorFileTask(errorFile, this));
            }
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (InterruptedException ex) {
            logger.error("Falla con los procesos simultaneos para extraer informacion de los archivos de error ", ex);
        }
    }

    public void addbusError(ErrorBusStation errorBusStation) {
        synchronized (this) {
            errorBusStationList.add(errorBusStation);
        }
    }

    public void addCardError(ErrorTransaction errorTransaction) {
        synchronized (this) {
            errorTransactionList.add(errorTransaction);
        }
    }

    public void addCountersKilometersCashError(ErrorCountersKilometersCash errorCountersKilometersCash) {
        synchronized (this) {
            errorCountersKilometersCashList.add(errorCountersKilometersCash);
        }
    }

    public void addErrorNumberControl(ErrorNumberControl errorNumberControl) {
        synchronized (this) {
            errorNumberControlList.add(errorNumberControl);
        }
    }

    /**
     * Revisa que el error no exista en la lista de errores de autobus
     *
     * @param errorDescription Descripcion de error a evaluar
     * @return Si no existe ya en la lista
     */
    public boolean notExistOnList(String errorDescription) {
        return !errorBusStationList
                .stream()
                .anyMatch(busError -> busError
                        .getErrorDescription()
                        .equalsIgnoreCase(errorDescription));
    }
}
