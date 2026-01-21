package com.idear.fimpe.excel;

public class ExcelSheetRowLimitException extends Exception {
    public ExcelSheetRowLimitException(String message) {
        super(message);
    }

    public ExcelSheetRowLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
