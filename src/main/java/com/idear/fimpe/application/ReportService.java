package com.idear.fimpe.application;

import com.idear.fimpe.cet.application.CETReportService;
import com.idear.fimpe.cet.infraestructure.CETSQLServerRepository;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.mail.Mail;
import com.idear.fimpe.database.SQLServerCommonRepository;
import com.idear.fimpe.error.application.ErrorReportService;
import com.idear.fimpe.error.infraestructure.ErrorSQLRepository;
import com.idear.fimpe.excel.GeneralExcelReport;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.torniquete.application.TorniqueteReportService;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteSQLServerRepository;
import com.idear.fimpe.unanswered.application.UnansweredReportService;
import com.idear.fimpe.unanswered.infraestructure.UnansweredSQLRepository;
import com.idear.fimpe.vrt.application.VRTReportService;
import com.idear.fimpe.vrt.infraestructure.VRTSQLServerRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

public class ReportService {

    private Logger logger = LoggerFactory.getLogger(ReportService.class);

    public void createReport() {
        try {
            logger.info("Generando reporte para CET");
            Workbook workbook = new HSSFWorkbook();
            CETReportService cetReportService = new CETReportService(new CETSQLServerRepository(), workbook);
            cetReportService.createReport();

            logger.info("Generando reporte para VRT");
            VRTReportService vrtReportService = new VRTReportService(new VRTSQLServerRepository(), workbook);
            vrtReportService.createReport();

            logger.info("Generando reporte para Torniquetes");
            TorniqueteReportService torniqueteReportService = new TorniqueteReportService(new TorniqueteSQLServerRepository(), workbook);
            torniqueteReportService.createReport();

            logger.info("Generando reporte de Pendientes");
            UnansweredReportService unansweredReportService = new UnansweredReportService(new UnansweredSQLRepository(), workbook);
            unansweredReportService.createReport();

            logger.info("Generando reporte de Errores");
            ErrorReportService errorReportService = new ErrorReportService(new SQLServerCommonRepository(),
                    new ErrorSQLRepository(), workbook);
            errorReportService.createReport();

            logger.info("Generando reporte de informe general");
            GeneralExcelReport generalExcelReport = new GeneralExcelReport(workbook);
            generalExcelReport.setVrtReportService(vrtReportService);
            generalExcelReport.setTorniqueteReportService(torniqueteReportService);
            generalExcelReport.setUnansweredReportService(unansweredReportService);
            generalExcelReport.createReport();

            String workBookNameXLS = "Reporte FIMPE-" + PropertiesHelper.PROJECT_NAME + DateHelper.getDateNowFormatted() + ".xls";
            String workBookNameZIP = "Reporte FIMPE-" + PropertiesHelper.PROJECT_NAME + DateHelper.getDateNowFormatted() + ".zip";

            FileOutputStream fileOutputStream = null;
            fileOutputStream = new FileOutputStream(Folder.REPORTS.getPath() + workBookNameXLS);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();

            FileManager.compressFileToRar(Folder.REPORTS.getPath() + workBookNameXLS, Folder.REPORTS.getPath() + workBookNameZIP);

            Mail mail = new Mail();
            mail.sendMail("Reporte de envios a FIMPE " + PropertiesHelper.PROJECT_NAME + " del dia " + DateHelper.getDateNowFormatted(),
                    Folder.REPORTS.getPath() + workBookNameZIP, workBookNameZIP);
            logger.info("Correo de reporte enviado correctamente");
        } catch (IOException | RuntimeException ex) {
            logger.error("Error al intentar generar el reporte de envios", ex);
        }
    }
}
