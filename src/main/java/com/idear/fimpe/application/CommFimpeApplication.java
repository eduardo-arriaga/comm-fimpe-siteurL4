package com.idear.fimpe.application;

import com.idear.fimpe.database.SQLServerCommonRepository;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.helpers.logger.LoggerManagerLog4J2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author rperez
 */
public class CommFimpeApplication {

    public static void main(String[] args) {
        try {
            PropertiesHelper.loadProperties("config.properties");
            LoggerManagerLog4J2.configure(PropertiesHelper.MAX_LOG_FILES, PropertiesHelper.MAX_SIZE_FILE_LOG);
            Logger logger = LoggerFactory.getLogger(CommFimpeApplication.class);
            logger.info("Inicia el programa version {}", PropertiesHelper.VERSION);
            SendService sendService = new SendService();
            AcknowledgmentService acknowledgmentService = new AcknowledgmentService(new SQLServerCommonRepository());
            ReportService reportService = new ReportService();
            boolean enviosEjecutado = false;
            boolean acusesEjecutado = false;
            boolean reporteEjecutado = false;

            if (args != null && args.length > 0) {
                if(args.length > 3){
                    logger.error("Se especificaron mas de tres argumentos, el programa no puede ejecutarse");
                    System.exit(3);
                }
                for (String command : args) {
                    if (command.equalsIgnoreCase("envios") && !enviosEjecutado) {
                        enviosEjecutado = true;
                        logger.info("Inicia el envio de transacciones");
                        sendService.send();
                    }
                    if (command.equalsIgnoreCase("acuses")&& !acusesEjecutado) {
                        acusesEjecutado = true;
                        logger.info("Inicia el proceso de acuses");
                        logger.info("Espera por {} hora(s)", PropertiesHelper.WAIT_TIME_TO_DOWNLOAD);
                        sleepProcess(PropertiesHelper.WAIT_TIME_TO_DOWNLOAD);
                        acknowledgmentService.downloadAndProcess();
                    }
                    if (command.equalsIgnoreCase("reporte") && !reporteEjecutado) {
                        reporteEjecutado = true;
                        logger.info("Inicia el proceso de generacion del reporte MiMacro");
                        reportService.createReport();
                    }
                }
            }else{
                logger.error("No se especificaron argumentos para la ejecucion");
            }
            logger.info("Termina el programa");
        } catch (IOException e) {
            System.exit(3);//Error al cargar el archivo de propiedades
        }
    }

    private static void sleepProcess(int hours) {
        try {
            Thread.sleep((long) hours * 60 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

