package com.idear.fimpe.error.infraestructure;

import com.idear.fimpe.error.domain.ErrorBusStation;
import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ErrorBusStationReportExcel {

    private List<ErrorBusStation> errorBusStationList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Errores Autobuses-Estaciones";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(ErrorBusStationReportExcel.class);

    public ErrorBusStationReportExcel(List<ErrorBusStation> errorBusStationList, Workbook workbook) {
        this.errorBusStationList = errorBusStationList;
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
            logger.error("Error al intentar generar el reporte errores de autobuses y estaciones");
        }
        excelManager.autoSize(ErrorBusStation.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Errores de Autbobuses y Estaciones", ErrorBusStation.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : ErrorBusStation.getHeaders()) {
            cell = row.createCell(ErrorBusStation.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (ErrorBusStation errorBusStation : errorBusStationList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < errorBusStation.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(errorBusStation.getOrderValues().get(i));
            }
        }
    }
}
