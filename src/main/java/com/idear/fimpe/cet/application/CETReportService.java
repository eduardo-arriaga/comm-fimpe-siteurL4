package com.idear.fimpe.cet.application;

import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.cet.domain.CETReportRecord;
import com.idear.fimpe.cet.domain.CETRepository;
import com.idear.fimpe.cet.infraestructure.CETReportExcel;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CETReportService {
    private CETRepository cetRepository;
    private Workbook workbook;
    private List<CETReportRecord> cetReportRecordList;
    private CETReportExcel cetReportExcel;

    public CETReportService(CETRepository cetRepository, Workbook workbook) {
        this.cetRepository = cetRepository;
        this.workbook = workbook;
        cetReportRecordList = new ArrayList<>();
    }

    public void createReport() {
        LocalDateTime startLocalDate =
                DateHelper.getLocalDateFromHourMinuteSecond(LocalDateTime.now(), 0, 0, 0);
        LocalDateTime endLocalDate =
                DateHelper.getLocalDateFromHourMinuteSecond(LocalDateTime.now(), 23, 59, 59);

        cetReportRecordList = cetRepository.getCETReportRecords(startLocalDate, endLocalDate);
        cetRepository.collectCETReportRecordInfo(cetReportRecordList, startLocalDate, endLocalDate);

        cetReportExcel = new CETReportExcel(cetReportRecordList, workbook);
        cetReportExcel.createTable();
    }

    public int getRecordsOk() {
        int result = 0;
        for (CETReportRecord cetReportRecord : cetReportRecordList) {
            result += cetReportRecord.getDebitRecordsOk();
            result += cetReportRecord.getRechargeRecordsOk();
        }
        return result;
    }

    public int getRecordsError() {
        int result = 0;
        for (CETReportRecord cetReportRecord : cetReportRecordList) {
            result += cetReportRecord.getDebitRecordsError();
            result += cetReportRecord.getRechargeRecordsError();
        }
        return result;
    }


    public String getReportName() {
        return CETReportExcel.REPORT_NAME;
    }
}
