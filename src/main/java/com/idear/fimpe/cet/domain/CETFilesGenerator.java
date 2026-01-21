package com.idear.fimpe.cet.domain;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.cet.infraestructure.CETFilesGeneratorXMLException;

import java.nio.file.Path;

public interface CETFilesGenerator {

    void generateFiles(CETNumberControl cetNumberControl, PrefixFile prefixFile) throws CETFilesGeneratorXMLException;

    Path getNumberControlFile();

    Path getDataFile();
}
