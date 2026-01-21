package com.idear.fimpe.vrt.domain;

import com.idear.fimpe.vrt.infraestructure.VRTFilesGeneratorXMLException;

import java.nio.file.Path;

public interface VRTFilesGenerator {

    void generateFiles(VRTNumberControl vrtNumberControl) throws VRTFilesGeneratorXMLException;

    Path getNumberControlFile();

    Path getDataFile();
}
