package com.idear.fimpe.application;

import com.idear.fimpe.cash.application.CashSendService;
import com.idear.fimpe.cash.infraestructure.CashFilesGeneratorXML;
import com.idear.fimpe.cash.infraestructure.CashSQLServerRepository;
import com.idear.fimpe.cet.application.CETSendService;
import com.idear.fimpe.cet.infraestructure.CETFilesGeneratorXML;
import com.idear.fimpe.cet.infraestructure.CETSQLServerRepository;
import com.idear.fimpe.counters.application.CountersSendService;
import com.idear.fimpe.counters.infraestructure.CountersFilesGeneratorXML;
import com.idear.fimpe.counters.infraestructure.CountersSQLServerRepository;
import com.idear.fimpe.helpers.mail.Mail;;
import com.idear.fimpe.database.SQLServerCommonRepository;
import com.idear.fimpe.kilometers.application.KilometersSendService;
import com.idear.fimpe.kilometers.infraestructure.KilometersFilesGeneratorXML;
import com.idear.fimpe.kilometers.infraestructure.KilometersSQLServerRepository;
import com.idear.fimpe.lam.application.LAMSendService;
import com.idear.fimpe.lam.infraestructure.LAMFilesGeneratorXML;
import com.idear.fimpe.lam.infraestructure.LAMSQLServerRespository;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.remoterecharge.application.RemoteRechargeSendService;
import com.idear.fimpe.remoterecharge.infraestructure.RemoteRechargeFilesGeneratorXML;
import com.idear.fimpe.remoterecharge.infraestructure.RemoteRechargeSQLServerRepository;
import com.idear.fimpe.torniquete.application.TorniqueteSendService;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteFilesGeneratorXML;
import com.idear.fimpe.torniquete.infraestructure.TorniqueteSQLServerRepository;
import com.idear.fimpe.vrt.application.VRTSendService;
import com.idear.fimpe.vrt.infraestructure.VRTFilesGeneratorXML;
import com.idear.fimpe.vrt.infraestructure.VRTSQLServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendService {

    private int filesSent = 0;
    private Logger logger = LoggerFactory.getLogger(SendService.class);

    public void send() {
        //Envios CET
        CETSendService cetSendService = new CETSendService(
                new CETSQLServerRepository(),
                new SQLServerCommonRepository(),
                new CETFilesGeneratorXML());
        filesSent += cetSendService.executeSend();

        //Envios VRT
        VRTSendService vrtSendService = new VRTSendService(
                new VRTSQLServerRepository(),
                new SQLServerCommonRepository(),
                new VRTFilesGeneratorXML());
        filesSent += vrtSendService.send();

        //Envios Torniquetes/garitas
        TorniqueteSendService torniqueteSendService = new TorniqueteSendService(
                new TorniqueteSQLServerRepository(),
                new SQLServerCommonRepository(),
                new TorniqueteFilesGeneratorXML());
        filesSent += torniqueteSendService.executeSend();

        //Envios LAM
        LAMSendService lamSendService = new LAMSendService(
                new LAMSQLServerRespository(),
                new LAMFilesGeneratorXML());
        filesSent += lamSendService.send();

        //Envios de recargas remotas
        RemoteRechargeSendService remoteRechargeSendService = new RemoteRechargeSendService(
                new RemoteRechargeSQLServerRepository(),
                new RemoteRechargeFilesGeneratorXML());
        filesSent += remoteRechargeSendService.send();

        //Envio de archivos de efectivo
        CashSendService cashSendService = new CashSendService(
                new CashSQLServerRepository(),
                new CashFilesGeneratorXML());
        filesSent += cashSendService.executeSend();

        //Envio de archivos de kilometros
        KilometersSendService kilometersSendService =
                new KilometersSendService(
                        new KilometersSQLServerRepository(),
                        new KilometersFilesGeneratorXML());
        filesSent += kilometersSendService.executeSend();

        //Envio de archivos de contadores
        CountersSendService countersSendService =
                new CountersSendService(
                        new CountersSQLServerRepository(),
                        new CountersFilesGeneratorXML());
        filesSent += countersSendService.executeSend();

        Mail mail = new Mail();
        mail.sendMail("ENVIO FIMPE " + PropertiesHelper.PROJECT_NAME, filesSent + " pares de archivos enviados correctamente");
        logger.info("ENVIO FIMPE MMP {} pares de archivos enviados correctamente", filesSent);
    }
}
