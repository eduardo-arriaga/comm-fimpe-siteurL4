package com.idear.fimpe.error.domain;

public interface ErrorRepository {
    void getBusInfo(ErrorBusStation errorBusStation);
    void getStationInfo(ErrorBusStation errorBusStation);
    void getBusTransactionInfo(ErrorTransaction errorTransaction);
    void getStationTransactionInfo(ErrorTransaction errorTransaction);
    void getStationTransactionQRInfo(ErrorTransaction errorTransaction);
    void getStationTransactionBancariaInfo(ErrorTransaction errorTransaction);
}
