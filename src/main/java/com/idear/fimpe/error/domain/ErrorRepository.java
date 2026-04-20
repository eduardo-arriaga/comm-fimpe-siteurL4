package com.idear.fimpe.error.domain;

import java.util.List;

public interface ErrorRepository {
    void getBusInfo(ErrorBusStation errorBusStation);
    void getStationInfo(ErrorBusStation errorBusStation);
    void getBusTransactionInfo(ErrorTransaction errorTransaction);
    void getStationTransactionInfo(ErrorTransaction errorTransaction);
    void getStationTransactionQRInfo(ErrorTransaction errorTransaction);
    void getStationTransactionBancariaInfo(ErrorTransaction errorTransaction);
    void updateCetTransacctionsWithCardErrorWithoutCatalog(List<ErrorTransaction> cetTransactions);
    void updateStationTransacctionsWithCardErrorWithoutCatalog(List<ErrorTransaction> stationTransactions);
}
