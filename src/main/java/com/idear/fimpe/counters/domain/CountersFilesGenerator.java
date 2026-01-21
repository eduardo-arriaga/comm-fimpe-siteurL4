package com.idear.fimpe.counters.domain;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.counters.infraestructure.CountersFileGeneratorException;

import java.nio.file.Path;

public interface CountersFilesGenerator {

    void generateFiles(CountersNumberControl countersNumberControl, PrefixFile prefixFile) throws CountersFileGeneratorException;

    Path getNumberControlFile();

    Path getDataFile();
}
