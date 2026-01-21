package com.idear.fimpe.torniquete.domain;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteFilesGeneratorXMLException;

import java.nio.file.Path;

public interface TorniqueteFilesGenerator {

    void generateFiles(TorniquteNumberControl torniqueteNumberControl, PrefixFile prefixFile) throws TorniqueteFilesGeneratorXMLException;

    Path getNumberControlFile();

    Path getDataFile();
}
