package com.idear.fimpe.remoterecharge.domain;

import java.nio.file.Path;

public interface RemoteRechargeFilesGenerator {

    void generateFiles(RemoteRechargeNumberControl remoteRechargeNumberControl) throws RemoteRechargeGeneratorException;

    Path getNumberControlFile();

    Path getDataFile();

}
