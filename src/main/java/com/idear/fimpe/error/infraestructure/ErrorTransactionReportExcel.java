package com.idear.fimpe.error.infraestructure;

import com.idear.fimpe.error.domain.ErrorTransaction;
import com.idear.fimpe.excel.ExcelManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ErrorTransactionReportExcel {

    private List<ErrorTransaction> errorTransactionList;
    private Sheet sheet;
    private Workbook workbook;
    public  String REPORT_NAME = "Errores Transacciones";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(ErrorTransactionReportExcel.class);

    public ErrorTransactionReportExcel(List<ErrorTransaction> errorTransactionList, Workbook workbook, int index) {
        this.errorTransactionList = errorTransactionList;
        excelManager = new ExcelManager(workbook);
        this.workbook = workbook;
        REPORT_NAME += "-" + index;
    }

    public void createTable() {
        sheet = workbook.createSheet(REPORT_NAME);
        excelManager.setSheet(sheet);
        createHeaders();
        createRecords();
        excelManager.autoSize(ErrorTransaction.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Errores de Transacciones", ErrorTransaction.getHeaders().size());
    }

    private void createRecords() {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : ErrorTransaction.getHeaders()) {
            cell = row.createCell(ErrorTransaction.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (ErrorTransaction errorTransaction : errorTransactionList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < errorTransaction.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(errorTransaction.getOrderValues().get(i));
            }
        }
    }
}
