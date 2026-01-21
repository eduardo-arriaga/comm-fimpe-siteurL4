package com.idear.fimpe.torniquete.infraestructure;

import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import com.idear.fimpe.torniquete.domain.TorniqueteReportRecord;
import com.idear.fimpe.vrt.domain.VRTReportRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TorniqueteExcelReport {

    private List<TorniqueteReportRecord> torniqueteReportRecordList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Reporte envios TorniqueteGarita";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(TorniqueteExcelReport.class);

    public TorniqueteExcelReport(List<TorniqueteReportRecord> torniqueteReportRecordList, Workbook workbook) {
        this.torniqueteReportRecordList = torniqueteReportRecordList;
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
            logger.error("Error al intentar generar el reporte de envios de Torniquete-Garita");
        }
        excelManager.autoSize(VRTReportRecord.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Envios Torniquetes-Garitas", TorniqueteReportRecord.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : TorniqueteReportRecord.getHeaders()) {
            cell = row.createCell(TorniqueteReportRecord.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (TorniqueteReportRecord torniqueteReportRecord : torniqueteReportRecordList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < torniqueteReportRecord.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(torniqueteReportRecord.getOrderValues().get(i));
            }
        }
    }
}
