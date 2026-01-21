package com.idear.fimpe.error.domain;

import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ErrorNumberControlReportExcel {

    private List<ErrorNumberControl> errorNumberControlList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Errores de Control de Cifras";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(ErrorNumberControlReportExcel.class);

    public ErrorNumberControlReportExcel(List<ErrorNumberControl> errorNumberControlList, Workbook workbook) {
        this.errorNumberControlList = errorNumberControlList;
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
            logger.error("Error al intentar generar el reporte errores de control de cifras");
        }
        excelManager.autoSize(ErrorNumberControl.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Errores de Control de Cifras", ErrorNumberControl.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : ErrorNumberControl.getHeaders()) {
            cell = row.createCell(ErrorNumberControl.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (ErrorNumberControl numberControl : errorNumberControlList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < numberControl.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(numberControl.getOrderValues().get(i));
            }
        }
    }
}
