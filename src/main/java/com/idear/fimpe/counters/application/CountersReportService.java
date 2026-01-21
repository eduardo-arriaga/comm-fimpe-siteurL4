package com.idear.fimpe.counters.application;

import com.idear.fimpe.cash.infraestructure.CashReportExcel;
import com.idear.fimpe.counters.domain.CountersReportRecord;
import com.idear.fimpe.counters.domain.CountersSQLRepository;
import com.idear.fimpe.counters.infraestructure.CountersReportExcel;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CountersReportService {

    private CountersSQLRepository countersSQLRepository;
    private Workbook workbook;
    private CountersReportExcel countersReportExcel;
    private List<CountersReportRecord> countersReportRecords;

    public CountersReportService(CountersSQLRepository countersSQLRepository, Workbook workbook){
        this.countersSQLRepository = countersSQLRepository;
        this.workbook = workbook;
    }

    public void createReport(){
        LocalDateTime starDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));

        countersReportRecords = countersSQLRepository.getCountersReportRecord(starDate, endDate);
        countersReportExcel = new CountersReportExcel(countersReportRecords, workbook);
        countersReportExcel.createTable();
    }

    public String getReportName() {
        return CashReportExcel.REPORT_NAME;
    }

    public int getCountersReportRecordsSize() {
        return countersReportRecords.size();
    }
}
