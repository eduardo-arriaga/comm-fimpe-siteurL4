package com.idear.fimpe.torniquete.application;

import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.torniquete.domain.TorniqueteReportRecord;
import com.idear.fimpe.torniquete.domain.TorniqueteRepository;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteExcelReport;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TorniqueteReportService {
    private TorniqueteRepository torniqueteRepository;
    private Workbook workbook;
    private List<TorniqueteReportRecord> torniqueteReportRecordList;

    public TorniqueteReportService(TorniqueteRepository torniqueteRepository, Workbook workbook) {
        this.torniqueteRepository = torniqueteRepository;
        this.workbook = workbook;
        this.torniqueteReportRecordList = new ArrayList<>();
    }

    public void createReport(){
        LocalDateTime startDate = DateHelper.getLocalDateFromHourMinuteSecond(LocalDateTime.now(), 0,0,0);
        LocalDateTime endDate = DateHelper.getLocalDateFromHourMinuteSecond(LocalDateTime.now(), 23, 59,59);

        torniqueteReportRecordList = torniqueteRepository.getTorniqueteReportRecord();
        torniqueteRepository.collectTorniqueteRecordsReport(startDate, endDate, torniqueteReportRecordList);

        TorniqueteExcelReport torniqueteExcelReport = new TorniqueteExcelReport(torniqueteReportRecordList, workbook);
        torniqueteExcelReport.createTable();
    }

    public int getRecordsOk() {
        int result = 0;
        for (TorniqueteReportRecord torniqueteReportRecord : torniqueteReportRecordList) {
            result += torniqueteReportRecord.getDebitTorniqueteRecordsOk();
        }
        return result;
    }

    public int getRecordsError() {
        int result = 0;
        for (TorniqueteReportRecord torniqueteReportRecord : torniqueteReportRecordList) {
            result += torniqueteReportRecord.getDebitTorniqueteRecordsError();
        }
        return result;
    }


    public String getReportName() {
        return TorniqueteExcelReport.REPORT_NAME;
    }


}
