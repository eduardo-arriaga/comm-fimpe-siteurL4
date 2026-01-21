package com.idear.fimpe.unanswered.infraestructure;

import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import com.idear.fimpe.unanswered.domain.UnansweredRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UnansweredReportExcel {
    private List<UnansweredRecord> unansweredRecordsCET;
    private List<UnansweredRecord> unansweredRecordsVRT;
    private List<UnansweredRecord> unansweredRecordsTorniquete;
    private List<UnansweredRecord> unansweredRecordsCash;
    private List<UnansweredRecord> unansweredRecordsCounters;
    private List<UnansweredRecord> unansweredRecordsKilometers;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Envios Pendientes De Respuesta";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(UnansweredReportExcel.class);

    public UnansweredReportExcel(List<UnansweredRecord> unansweredRecordsCET,
                                 List<UnansweredRecord> unansweredRecordsVRT,
                                 List<UnansweredRecord> unansweredRecordsTorniquete,
                                 List<UnansweredRecord> unansweredRecordsCash,
                                 List<UnansweredRecord> unansweredRecordsCounters,
                                 List<UnansweredRecord> unansweredRecordsKilometers,
                                 Workbook workbook) {

       this.unansweredRecordsCET = unansweredRecordsCET;
       this.unansweredRecordsVRT = unansweredRecordsVRT;
       this.unansweredRecordsTorniquete = unansweredRecordsTorniquete;
       this.unansweredRecordsCash = unansweredRecordsCash;
       this.unansweredRecordsCounters = unansweredRecordsCounters;
       this.unansweredRecordsKilometers = unansweredRecordsKilometers;
        excelManager = new ExcelManager(workbook);
        this.workbook = workbook;
    }

    public void createTable() {
        try {
            sheet = workbook.createSheet(REPORT_NAME);
            excelManager.setSheet(sheet);
            createHeaders();
            createRecords();
        } catch (ExcelSheetRowLimitException e) {
            logger.error("Error al intentar generar el reporte de Pendientes");
        }
        excelManager.autoSize(UnansweredRecord.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Envios Pendientes De Respuesta", UnansweredRecord.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell = null;
        for (String header : UnansweredRecord.getHeaders()) {
            cell = row.createCell(UnansweredRecord.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        createRecordFromUnansweredList(unansweredRecordsCET, row, cell);
        createRecordFromUnansweredList(unansweredRecordsVRT, row, cell);
        createRecordFromUnansweredList(unansweredRecordsTorniquete, row, cell);
        createRecordFromUnansweredList(unansweredRecordsCash, row, cell);
        createRecordFromUnansweredList(unansweredRecordsCounters, row, cell);
        createRecordFromUnansweredList(unansweredRecordsKilometers, row, cell);
    }

    private void createRecordFromUnansweredList(List<UnansweredRecord> unansweredRecords, Row row, Cell cell){
        for (UnansweredRecord unansweredRecord : unansweredRecords) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < unansweredRecord.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(unansweredRecord.getOrderValues().get(i));
            }
        }
    }
}
