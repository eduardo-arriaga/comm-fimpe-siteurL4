package com.idear.fimpe.fimpetransport;

public class FimpeException extends Exception {
    public FimpeException(String message) {
        super(message);
    }

    public FimpeException(String message, Throwable cause) {
        super(message, cause);
    }
}
