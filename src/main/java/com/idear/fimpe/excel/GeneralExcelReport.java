package com.idear.fimpe.excel;

import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.torniquete.application.TorniqueteReportService;
import com.idear.fimpe.unanswered.application.UnansweredReportService;
import com.idear.fimpe.vrt.application.VRTReportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class GeneralExcelReport {
    private ExcelManager excelManager;
    private Workbook workbook;
    private Sheet sheet;
    private VRTReportService vrtReportService;
    private TorniqueteReportService torniqueteReportService;
    private UnansweredReportService unansweredReportService;
    private String REPORT_NAME = "Reporte General";

    public GeneralExcelReport(Workbook workbook) {
        this.workbook = workbook;
    }

    public void createReport() {

        excelManager = new ExcelManager(workbook);
        sheet = workbook.createSheet(REPORT_NAME);
        excelManager.setSheet(sheet);

        //Crear encabezado
        excelManager.addTableTitle("Reporte general del dia " + DateHelper.getDateNowFormatted(), 4);

        //crear tabla transacciones
        createCardTransactionsTable();
        //crear tabla de archivos
        createResumesTable();

        excelManager.autoSize(4);
    }

    private void createCardTransactionsTable() {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell = row.createCell(0);
        cell.setCellValue("");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(1);
        cell.setCellValue("Exitosos (transacciones)");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(2);
        cell.setCellValue("Errores (transacciones)");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(3);
        cell.setCellValue("Pendientes (archivos)");
        cell.setCellStyle(excelManager.getHeaderStyle());

        createVRTRecord();
        createTorniqueteRecord();
    }


    private void createVRTRecord() {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell = row.createCell(0);
        cell.setCellValue("VRT");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(1);
        cell.setCellValue(vrtReportService.getRecordsOk()); //exitosos recargas y debitos
        cell.setCellStyle(excelManager.getContentStyle());

        cell = row.createCell(2);
        cell.setCellValue(vrtReportService.getRecordsError());//errores recargas y debitos
        cell.setCellStyle(excelManager.getContentStyle());

        cell = row.createCell(3);
        cell.setCellValue(unansweredReportService.getUnansweredRecordsVRT());//archivos pendientes
        cell.setCellStyle(excelManager.getContentStyle());
    }

    private void createTorniqueteRecord() {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell = row.createCell(0);
        cell.setCellValue("TORNIQUETE");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(1);
        cell.setCellValue(torniqueteReportService.getRecordsOk()); //exitosos recargas y debitos
        cell.setCellStyle(excelManager.getContentStyle());

        cell = row.createCell(2);
        cell.setCellValue(torniqueteReportService.getRecordsError());//errores recargas y debitos
        cell.setCellStyle(excelManager.getContentStyle());

        cell = row.createCell(3);
        cell.setCellValue(unansweredReportService.getUnansweredRecordsTorniquete());//archivos pendientes
        cell.setCellStyle(excelManager.getContentStyle());
    }

    private void createResumesTable() {
        Row row = sheet.createRow(excelManager.getIndexRow());//Genera una fila extra en blanco
        row = sheet.createRow(excelManager.getIndexRow());
        Cell cell = row.createCell(0);
        cell.setCellValue("");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(1);
        cell.setCellValue("Exitosos (archivos)");
        cell.setCellStyle(excelManager.getHeaderStyle());

        cell = row.createCell(2);
        cell.setCellValue("Pendientes (archivos)");
        cell.setCellStyle(excelManager.getHeaderStyle());
    }

    public void setVrtReportService(VRTReportService vrtReportService) {
        this.vrtReportService = vrtReportService;
    }

    public void setTorniqueteReportService(TorniqueteReportService torniqueteReportService) {
        this.torniqueteReportService = torniqueteReportService;
    }

    public void setUnansweredReportService(UnansweredReportService unansweredReportService) {
        this.unansweredReportService = unansweredReportService;
    }

    public String getReportName() {
        return REPORT_NAME;
    }
}
