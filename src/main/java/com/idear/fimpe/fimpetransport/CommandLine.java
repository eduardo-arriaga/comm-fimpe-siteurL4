package com.idear.fimpe.fimpetransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CommandLine {
    private static Process process;
    private static Logger logger = LoggerFactory.getLogger(CommandLine.class);

    public static ArrayList<String> execCommand(String command){
        ArrayList<String> response = new ArrayList<>();
        try {
            process = Runtime.getRuntime().exec("cmd /c " + command);

            try {
                process.waitFor(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                logger.error("", ex);
            }
            InputStream inputStreamError = process.getErrorStream();
            InputStream inputStreamOk = process.getInputStream();
            BufferedReader bufferedReaderError = new BufferedReader(new InputStreamReader(inputStreamError));
            BufferedReader bufferedReaderOk = new BufferedReader(new InputStreamReader(inputStreamOk));

            int index = 0;

            //Si la respuesta fue exitosa, la lee hasta donde las lineas empiezan a ser null
            while (bufferedReaderOk.ready()) {
                response.add(bufferedReaderOk.readLine());
                if (response.get(index) == null) {
                    response.remove(index);
                    break;
                }
                index++;
            }
            //Si la respuesta fue negativa, la lee hasta donde las lineas empiezan a ser null
            while (bufferedReaderError.ready()) {
                response.add(bufferedReaderError.readLine());
                if (response.get(index) == null) {
                    response.remove(index);
                    break;
                }
                index++;
            }

        } catch (IOException ex) {
            logger.error("", ex);
        }
        process.destroy();
        return response;
    }
}
