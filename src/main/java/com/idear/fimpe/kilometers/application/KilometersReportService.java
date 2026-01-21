package com.idear.fimpe.kilometers.application;

import com.idear.fimpe.kilometers.domain.KilometersReportRecord;
import com.idear.fimpe.kilometers.domain.KilometersSQLRepository;
import com.idear.fimpe.kilometers.infraestructure.KilometersReportExcel;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class KilometersReportService {

    private KilometersSQLRepository kilometersSQLRepository;
    private Workbook workbook;
    private KilometersReportExcel kilometersReportExcel;

    private List<KilometersReportRecord> kilometersReportRecords;

    public KilometersReportService(KilometersSQLRepository kilometersSQLRepository, Workbook workbook){
        this.kilometersSQLRepository = kilometersSQLRepository;
        this.workbook = workbook;
    }

    public void createReport(){
        LocalDateTime starDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));

       kilometersReportRecords =
                kilometersSQLRepository.getKilometersReportRecord(starDate, endDate);
        kilometersReportExcel = new KilometersReportExcel(kilometersReportRecords, workbook);
        kilometersReportExcel.createTable();
    }

    public String getReportName() {
        return KilometersReportExcel.REPORT_NAME;
    }

    public int getKilometersReportRecordsSize() {
        return kilometersReportRecords.size();
    }
}
