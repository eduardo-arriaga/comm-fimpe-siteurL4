package com.idear.fimpe.cash.infraestructure;

import com.idear.fimpe.cash.domain.CashReportRecord;
import com.idear.fimpe.cet.domain.CETReportRecord;
import com.idear.fimpe.cet.infraestructure.CETReportExcel;
import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CashReportExcel {

    private List<CashReportRecord> cashReportRecordList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Reporte Efectivo y Pasajeros";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(CETReportExcel.class);

    public CashReportExcel(List<CashReportRecord> cashReportRecordList, Workbook workbook) {
        this.cashReportRecordList = cashReportRecordList;
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
            logger.error("Error al intentar generar el reporte de envios de Efectivo");
        }
        excelManager.autoSize(CETReportRecord.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Envios Efectivo y Pasajeros", CashReportRecord.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : CashReportRecord.getHeaders()) {
            cell = row.createCell(CashReportRecord.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (CashReportRecord cashReportRecord : cashReportRecordList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < cashReportRecord.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(cashReportRecord.getOrderValues().get(i));
            }
        }
    }
}
