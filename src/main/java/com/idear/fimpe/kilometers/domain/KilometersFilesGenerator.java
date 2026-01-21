package com.idear.fimpe.kilometers.domain;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.kilometers.infraestructure.KilometersFileGeneratorException;

import java.nio.file.Path;

public interface KilometersFilesGenerator {

    void generateFiles(KilometersNumberControl countersNumberControl, PrefixFile prefixFile) throws KilometersFileGeneratorException;

    Path getNumberControlFile();

    Path getDataFile();
}
