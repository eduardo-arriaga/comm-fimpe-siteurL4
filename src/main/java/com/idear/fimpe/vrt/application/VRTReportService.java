package com.idear.fimpe.vrt.application;

import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.vrt.domain.VRTReportRecord;
import com.idear.fimpe.vrt.domain.VRTRepository;
import com.idear.fimpe.vrt.infraestructure.VRTExcelReport;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VRTReportService {

    private VRTRepository vrtRepository;
    private Workbook workbook;
    private List<VRTReportRecord> vrtReportRecordList;

    public VRTReportService(VRTRepository vrtRepository, Workbook workbook) {
        this.vrtRepository = vrtRepository;
        this.workbook = workbook;
        vrtReportRecordList = new ArrayList<>();
    }

    public void createReport(){
        LocalDateTime startDate = DateHelper.getLocalDateFromHourMinuteSecond(LocalDateTime.now(), 0,0,0);
        LocalDateTime endDate = DateHelper.getLocalDateFromHourMinuteSecond(LocalDateTime.now(), 23, 59,59);

        vrtReportRecordList = vrtRepository.getVRTReportRecord();
        vrtRepository.collectVRTReportRecordInfo(startDate, endDate, vrtReportRecordList);

        VRTExcelReport vrtExcelReport = new VRTExcelReport(vrtReportRecordList, workbook);
        vrtExcelReport.createTable();
    }

    public int getRecordsOk() {
        int result = 0;
        for (VRTReportRecord vrtReportRecord : vrtReportRecordList) {
            result += vrtReportRecord.getSellRecordsOk();
            result += vrtReportRecord.getRechargeRecordsOk();
        }
        return result;
    }

    public int getRecordsError() {
        int result = 0;
        for (VRTReportRecord vrtReportRecord : vrtReportRecordList) {
            result += vrtReportRecord.getSellRecordsError();
            result += vrtReportRecord.getRechargeRecordsError();
        }
        return result;
    }

    public String getReportName() {
        return VRTExcelReport.REPORT_NAME;
    }
}
