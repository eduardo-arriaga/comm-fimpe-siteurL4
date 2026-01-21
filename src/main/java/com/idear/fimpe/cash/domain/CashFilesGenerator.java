package com.idear.fimpe.cash.domain;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.cash.infraestructure.CashFilesGeneratorException;

import java.nio.file.Path;

public interface CashFilesGenerator {

    void generateFiles(CashNumberControl cashNumberControl, PrefixFile prefixFile) throws CashFilesGeneratorException;

    Path getNumberControlFile();

    Path getDataFile();
}
