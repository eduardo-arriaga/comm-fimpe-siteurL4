package com.idear.fimpe.remoterecharge.domain;

import java.sql.SQLException;
import java.util.List;

public interface RemoteRechargeRepository {

    void insertRequests(List<RemoteRechargeTransaction> requests) throws SQLException;

    Long getCutFoil() throws SQLException;

    List<RemoteRechargeTransaction> getUnrequestedRemoteRecharge() throws SQLException;

    List<RemoteRechargeTransaction> getConfirmedRemoteRecharge() throws SQLException;

    void updateAsSentRemoteRecharge(RemoteRechargeNumberControl remoteRechargeNumberControl) throws SQLException;

    void updateSuccessfulAckReceived(Long folioCut) throws SQLException;

    void updateErrorAckReceived(Long folioCut) throws SQLException;
}
