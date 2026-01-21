package com.idear.fimpe.unanswered.application;

import com.idear.fimpe.unanswered.domain.UnansweredRecord;
import com.idear.fimpe.unanswered.domain.UnansweredRepository;
import com.idear.fimpe.unanswered.infraestructure.UnansweredReportExcel;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UnansweredReportService {

    private Logger logger = LoggerFactory.getLogger(UnansweredReportService.class);
    private UnansweredRepository unansweredRepository;
    private Workbook workbook;

    private List<UnansweredRecord> unansweredRecordsCET;
    private List<UnansweredRecord> unansweredRecordsVRT;
    private List<UnansweredRecord> unansweredRecordsTorniquete;
    private List<UnansweredRecord> unansweredRecordsCash;
    private List<UnansweredRecord> unansweredRecordsKilometers;
    private List<UnansweredRecord> unansweredRecordsCounters;

    public UnansweredReportService(UnansweredRepository unansweredRepository, Workbook workbook) {
        this.workbook = workbook;
        this.unansweredRepository = unansweredRepository;
        unansweredRecordsCET = new ArrayList<>();
        unansweredRecordsVRT = new ArrayList<>();
        unansweredRecordsTorniquete = new ArrayList<>();
        unansweredRecordsCash = new ArrayList<>();
        unansweredRecordsKilometers = new ArrayList<>();
        unansweredRecordsCounters = new ArrayList<>();
    }

    public void createReport() {
        try {
            unansweredRecordsCET = unansweredRepository.getUnansweredRecordsCET();
            unansweredRecordsVRT = unansweredRepository.getUnansweredRecordsVRT();
            unansweredRecordsTorniquete = unansweredRepository.getUnansweredRecordsTorniquete();
            unansweredRecordsCash = unansweredRepository.getUnansweredRecordsCash();
            unansweredRecordsCounters = unansweredRepository.getUnansweredRecordsCounters();
            unansweredRecordsKilometers = unansweredRepository.getUnansweredRecordsKilometers();

            UnansweredReportExcel unansweredReportExcel = new UnansweredReportExcel(
                    unansweredRecordsCET, unansweredRecordsVRT, unansweredRecordsTorniquete,
                    unansweredRecordsCash, unansweredRecordsCounters,
                    unansweredRecordsKilometers, workbook);

            unansweredReportExcel.createTable();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public int getUnansweredRecordsCET(){
        return unansweredRecordsCET.size();
    }

    public int getUnansweredRecordsVRT(){
        return unansweredRecordsVRT.size();
    }
    public int getUnansweredRecordsTorniquete(){
        return unansweredRecordsTorniquete.size();
    }

    public int getUnansweredRecordsCash(){
        return unansweredRecordsCash.size();
    }

    public int getUnansweredRecordsKilometers(){
        return unansweredRecordsKilometers.size();
    }

    public int getUnansweredRecordsCounters(){
        return unansweredRecordsCounters.size();
    }

    public String getReportName() {
        return UnansweredReportExcel.REPORT_NAME;
    }
}
