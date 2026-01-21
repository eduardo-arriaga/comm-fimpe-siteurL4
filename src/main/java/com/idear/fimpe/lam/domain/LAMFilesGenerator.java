package com.idear.fimpe.lam.domain;


import java.nio.file.Path;

public interface LAMFilesGenerator {

    void generateFiles(LAMNumberControl lamNumberControl) throws LAMFilesGeneratorException;

    Path getNumberControlFile();

    Path getDataFile();
}
