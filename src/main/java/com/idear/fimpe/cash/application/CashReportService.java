package com.idear.fimpe.cash.application;

import com.idear.fimpe.cash.domain.CashReportRecord;
import com.idear.fimpe.cash.domain.CashSQLRepository;
import com.idear.fimpe.cash.infraestructure.CashReportExcel;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CashReportService {

    private CashSQLRepository cashSQLRepository;
    private Workbook workbook;
    private CashReportExcel cashReportExcel;
    private List<CashReportRecord> cashReportRecords;

    public CashReportService(CashSQLRepository cashSQLRepository, Workbook workbook){
        this.cashSQLRepository = cashSQLRepository;
        this.workbook = workbook;
    }

    public void createReport(){
        LocalDateTime starDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));

        cashReportRecords = cashSQLRepository.getCashReportRecord(starDate, endDate);
        cashReportExcel = new CashReportExcel(cashReportRecords, workbook);
        cashReportExcel.createTable();
    }

    public int getCashReportRecordsSize(){
        return cashReportRecords.size();
    }

    public String getReportName() {
        return CashReportExcel.REPORT_NAME;
    }
}
