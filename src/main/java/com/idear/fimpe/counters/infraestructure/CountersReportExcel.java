package com.idear.fimpe.counters.infraestructure;


import com.idear.fimpe.counters.domain.CountersReportRecord;
import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CountersReportExcel {

    private List<CountersReportRecord> countersReportRecords;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Reporte Contadores de Subidas y Bajadas";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(CountersReportExcel.class);

    public CountersReportExcel(List<CountersReportRecord> countersReportRecords, Workbook workbook) {
        this.countersReportRecords = countersReportRecords;
        this.workbook = workbook;
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
            logger.error("Error al intentar generar el reporte de envios de Contadores");
        }
        excelManager.autoSize(CountersReportRecord.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Envios Contadores de Subidas y Bajadas", CountersReportRecord.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : CountersReportRecord.getHeaders()) {
            cell = row.createCell(CountersReportRecord.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (CountersReportRecord cashReportRecord : countersReportRecords) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < cashReportRecord.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(cashReportRecord.getOrderValues().get(i));
            }
        }
    }
}
