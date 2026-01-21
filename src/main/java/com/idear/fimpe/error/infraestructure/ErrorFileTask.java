package com.idear.fimpe.error.infraestructure;

import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.error.application.ErrorReportService;
import com.idear.fimpe.error.domain.ErrorBusStation;
import com.idear.fimpe.error.domain.ErrorCountersKilometersCash;
import com.idear.fimpe.error.domain.ErrorTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.idear.fimpe.enums.PrefixFile.*;

public class ErrorFileTask implements Runnable{

    private List<String> busErrorDescription;
    private ErrorReportService errorReportService;
    private Path errorFile;
    private String errorFileName;
    private Logger logger = LoggerFactory.getLogger(ErrorFileTask.class);

    public ErrorFileTask(Path errorFile, ErrorReportService errorReportService) {
        this.errorReportService = errorReportService;
        this.errorFile = errorFile;
        this.errorFileName = errorFile.getFileName().toString();
        busErrorDescription = new ArrayList<>();
        busErrorDescription.add("dispositivo");
        busErrorDescription.add("identificador de SAM");
        busErrorDescription.add("vehiculo");
    }

    @Override
    public void run() {
        try {
            Document document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(errorFile.toFile());

            document.getDocumentElement().normalize();
            NodeList nodosArchivo = document.getElementsByTagName("archivo");
            //itera los nodos archivo
            for (int i = 0; i < nodosArchivo.getLength(); i++) {
                Element archivo = (Element) nodosArchivo.item(i);
                String extension = archivo.getAttribute("extension");
                NodeList nodosErrores = archivo.getElementsByTagName("error");
                //itera los nodos error
                for (int j = 0; j < nodosErrores.getLength(); j++) {
                    Element error = (Element) nodosErrores.item(j);

                        //Obtiene el c1 y el c2
                        String nodeC1 = error.getElementsByTagName("c1").item(0).getTextContent();
                        String nodeC2 = error.getElementsByTagName("c2").item(0).getTextContent();
                        String errorDescription = formatNodeC2(nodeC2);
                        //Extraemos el folio de corte en el archivo
                        Long cutFoil = extractCutFoilFromFileName(errorFileName);

                    if (extension.equalsIgnoreCase("DAT")) {

                        //Si es de debitos o recargas extrae el consecutivo y el id de la tarjeta
                        if(errorFileName.startsWith(PrefixFile.RECHARGE.getPrefix()) ||
                                errorFileName.startsWith(PrefixFile.DEBIT.getPrefix())) {

                            //Si es de debitos o recargas extrae el consecutivo y el id de la tarjeta
                            String foil = extractFoil(nodeC1);

                            //Si es un folio de QR o Transaccion bancaria
                            long cardFoil = 0;
                            String idCard = foil;

                            //Si es un folio de tarjeta
                            if(foil.length() == 20){
                                cardFoil = getCardFoil(foil);
                                idCard = getIdCard(foil);
                            }

                            //Si es un folio de QR de debito
                            if(foil.length() == 14){
                                idCard = foil.substring(8, 14);
                            }

                            //Filtra entre error de autobus o de transaccion
                            if (isBusError(errorDescription)) {
                                if (errorReportService.notExistOnList(errorDescription)) {

                                    ErrorBusStation errorBusStation = new ErrorBusStation(
                                            cutFoil, idCard, cardFoil, errorDescription, errorFile.getFileName().toString());

                                    errorReportService.addbusError(errorBusStation);
                                }
                            } else {
                                //Si es un error de transaccion
                                ErrorTransaction errorTransaction = new ErrorTransaction(
                                        cutFoil, idCard, cardFoil,  errorDescription, errorFile.getFileName().toString());

                                errorReportService.addCardError(errorTransaction);
                            }
                        }else{
                            //Si es de kilometros, efectivo o contadores o recargas extrae la ruta y el numero economico
                            String foil = extractFoil(nodeC1);
                            String routeDescription = getRouteDescription(foil);
                            String economicNumber = getEconomicNumber(foil);

                            String fileType =   errorFileName.startsWith(CASH.getPrefix()) ? CASH.getFullName():
                                    errorFileName.startsWith(KILOMETERS.getPrefix()) ? KILOMETERS.getFullName() :
                                            errorFileName.startsWith(COUNTERS.getPrefix()) ? COUNTERS.getFullName(): "SIN TIPO";

                            ErrorCountersKilometersCash errorBusCash = new ErrorCountersKilometersCash(
                                    fileType, economicNumber, routeDescription, errorDescription, errorFileName);

                            errorReportService.addCountersKilometersCashError(errorBusCash);
                        }
                    }
                }
            }
        } catch (NullPointerException | SAXException | ParserConfigurationException | IOException e) {
            logger.error("Error al intentar extraer informacion del archivo  " + errorFile.toString(), e);
        }
    }

    private String getEconomicNumber(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return value.split("-")[3];
            } catch (ArrayIndexOutOfBoundsException ex) {
                return "SIN VALOR";
            }
        }
        return "SIN VALOR";
    }

    private String getRouteDescription(String value) {
        //MP-T03-P-UAP052
        if (value != null && !value.isEmpty()) {
            try {
                return value.split("-")[0] + "-" + value.split("-")[1];
            } catch (ArrayIndexOutOfBoundsException ex) {
                return "SIN VALOR";
            }
        }
        return "SIN VALOR";
    }

    private String extractFoil(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                String matchWord = "folio:";
                //"transaccion:2, folio:04221A2A165C800000C0, linea: 1"; Lo separa en tres strings
                String[] folioArray = value.split(",");
                String foilvalue = folioArray[1];
                int start = foilvalue.indexOf(matchWord) + matchWord.length();
                return foilvalue.substring(start);
            } catch (ArrayIndexOutOfBoundsException ex) {
                return "";
            }
        }
        return "";
    }

    private long getCardFoil(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                //Caso normal o esperado, que venga el id de la tarjeta y el consecutivo
                if(value.length() == 20){
                    return Long.parseLong(value.substring(14, 20), 16);
                }
                //En el caso de que solo venga el consecutivo
                if(value.length() == 6){
                    return  Long.parseLong(value, 16);
                }
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                return 0;
            }
        }
        return 0;
    }

    private String getIdCard(String value) {

        //Si es un idHoozie
        if(value.length() > 20)
            return value;

        //Si es una transaccion de tarjeta
        if (value != null && !value.isEmpty()) {
            try {
                return value.substring(0, 14);
            } catch (IndexOutOfBoundsException ex) {
                return "";
            }
        }
        return "";
    }

    /**
     * Da formato a la descripcion del error nodo c2
     *
     * @param value Informacion del nodo c2
     * @return El nodo c2 sin corchetes
     */
    private String formatNodeC2(String value) {

        StringBuilder valueToFormated = new StringBuilder(value);
        if (valueToFormated != null) {
            if (!valueToFormated.toString().equals("")) {
                valueToFormated = new StringBuilder(removeChar(valueToFormated, '['));
                valueToFormated = new StringBuilder(removeChar(valueToFormated, ']'));
            }
        }
        return valueToFormated.toString();
    }

    /**
     * Quita cualquier caracter de una cadena
     *
     * @param string Cadena a tratar
     * @param character Caracter a remover
     * @return Cadena sin los caracteres a remover
     */
    private String removeChar(StringBuilder string, char character) {
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == character) {
                string.deleteCharAt(i);
            }
        }
        return string.toString();
    }

    private boolean isBusError(String errorDescription) {
        return busErrorDescription.stream()
                .anyMatch(busError -> errorDescription.contains(busError));
    }

    private Long extractCutFoilFromFileName(String fileName) {
        return Long.valueOf(fileName.split("_")[1]);
    }
}
