package com.idear.fimpe.cet.infraestructure;

import com.idear.fimpe.cet.domain.CETReportRecord;
import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CETReportExcel {
    private List<CETReportRecord> cetReportRecordList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Reporte envios CET";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(CETReportExcel.class);

    public CETReportExcel(List<CETReportRecord> cetReportRecordList, Workbook workbook) {
        this.cetReportRecordList = cetReportRecordList;
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
           logger.error("Error al intentar generar el reporte de envios del CET");
        }
        excelManager.autoSize(CETReportRecord.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Envios CET", CETReportRecord.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : CETReportRecord.getHeaders()) {
            cell = row.createCell(CETReportRecord.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (CETReportRecord cetReportRecord : cetReportRecordList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < cetReportRecord.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(cetReportRecord.getOrderValues().get(i));
            }
        }
    }
}
