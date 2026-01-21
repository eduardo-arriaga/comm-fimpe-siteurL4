package com.idear.fimpe.vrt.infraestructure;

import com.idear.fimpe.excel.ExcelManager;
import com.idear.fimpe.excel.ExcelSheetRowLimitException;
import com.idear.fimpe.vrt.domain.VRTReportRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VRTExcelReport {

    private List<VRTReportRecord> vrtReportRecordList;
    private Sheet sheet;
    private Workbook workbook;
    public static final String REPORT_NAME = "Reporte envios VRT";
    private ExcelManager excelManager;
    private static Logger logger = LoggerFactory.getLogger(VRTExcelReport.class);

    public VRTExcelReport(List<VRTReportRecord> vrtReportRecordList, Workbook workbook) {
        this.vrtReportRecordList = vrtReportRecordList;
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
            logger.error("Error al intentar generar el reporte de envios de VRT");
        }
        excelManager.autoSize(VRTReportRecord.getHeaders().size());
    }

    private void createHeaders() {
        excelManager.addTableTitle("Envios VRT", VRTReportRecord.getHeaders().size());
    }

    private void createRecords() throws ExcelSheetRowLimitException {
        Row row = sheet.createRow(excelManager.getIndexRow());
        Cell cell;
        for (String header : VRTReportRecord.getHeaders()) {
            cell = row.createCell(VRTReportRecord.getHeaders().indexOf(header));
            cell.setCellValue(header);
            cell.setCellStyle(excelManager.getHeaderStyle());
        }

        for (VRTReportRecord vrtReportRecord : vrtReportRecordList) {
            row = sheet.createRow(excelManager.getIndexRow());

            for (int i = 0; i < vrtReportRecord.getOrderValues().size(); i++) {
                cell = excelManager.getCell(row, i, excelManager.getContentStyle());
                cell.setCellValue(vrtReportRecord.getOrderValues().get(i));
            }
        }
    }
}
