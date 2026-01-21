package com.idear.fimpe.error.infraestructure;

import com.idear.fimpe.error.domain.ErrorCountersKilometersCash;
import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CountersKilometersCashReportExcel {

    private List<ErrorCountersKilometersCash> errorCountersKilometersCashList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Errores de Efectivo-Pasajeros-Kilometros-Contadores";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(CountersKilometersCashReportExcel.class);

    public CountersKilometersCashReportExcel(List<ErrorCountersKilometersCash> errorCountersKilometersCashList, Workbook workbook) {
        this.errorCountersKilometersCashList = errorCountersKilometersCashList;
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
            logger.error("Error al intentar generar el reporte errores de Efectivo-Pasajeros-Kilometros-Contadores");
        }
        excelManager.autoSize(ErrorCountersKilometersCash.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Errores de Efectivo-Pasajeros-Kilometros-Contadores",
                ErrorCountersKilometersCash.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : ErrorCountersKilometersCash.getHeaders()) {
            cell = row.createCell(ErrorCountersKilometersCash.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (ErrorCountersKilometersCash errorCountersKilometersCash : errorCountersKilometersCashList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < errorCountersKilometersCash.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(errorCountersKilometersCash.getOrderValues().get(i));
            }
        }
    }
}
