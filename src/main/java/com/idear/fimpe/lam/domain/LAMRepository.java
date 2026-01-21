package com.idear.fimpe.lam.domain;

import java.sql.SQLException;
import java.util.List;

public interface LAMRepository {

    List<LAMTransaction> getTransactionUnRequested();

    List<LAMTransaction> getTransactionConfirmed();

    Long getFolioCut() throws SQLException;

    void updateTransactionSend(LAMNumberControl lamNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut) throws SQLException;

    void updateErrorAckReceived(Long folioCut) throws SQLException;

    List<LAMTransaction> getAllLAMTransactions() throws SQLException;

    void insertLAMTransactions(List<LAMTransaction> lamTransactionsToInsert) throws SQLException;

    void deleteLAMTransactions(List<LAMTransaction> lamTransactionsToDelete) throws SQLException;
}
