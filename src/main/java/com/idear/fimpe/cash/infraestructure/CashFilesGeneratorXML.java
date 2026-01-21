package com.idear.fimpe.cash.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.cash.domain.CashFilesGenerator;
import com.idear.fimpe.cash.domain.CashNumberControl;
import com.idear.fimpe.cash.domain.CashTransaction;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CashFilesGeneratorXML implements CashFilesGenerator {

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private DOMSource domSource;
    private StreamResult streamResult;
    private DecimalFormat formatForCurrency;

    private Path fileNumberControl;
    private Path fileData;

    public CashFilesGeneratorXML() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        formatForCurrency = (DecimalFormat) numberFormat;
        formatForCurrency.applyPattern("#0.00");
    }

    @Override
    public void generateFiles(CashNumberControl cashNumberControl, PrefixFile prefixFile) throws CashFilesGeneratorException {

        String fileNumberControlName = generateFileName(cashNumberControl, prefixFile, ExtentionFile.NUMBER_CONTROL);
        String fileDataName = generateFileName(cashNumberControl, prefixFile, ExtentionFile.DATA);
        try {
            fileNumberControl = generateNumberControl(cashNumberControl, fileNumberControlName);
            fileData = generateFileData(cashNumberControl, fileDataName);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new CashFilesGeneratorException("Error al intentar generar los pares de archivos " + fileNumberControlName);
        }
    }

    @Override
    public Path getNumberControlFile() {
        return fileNumberControl;
    }

    @Override
    public Path getDataFile() {
        return fileData;
    }

    private Path generateFileData(CashNumberControl cashNumberControl, String fileDataName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("efectivo");
        rootElement.setAttributeNode(attrDebito);

        document.appendChild(rootElement);
        for (CashTransaction cashTransaction : cashNumberControl.getCashTransactions()) {
            Element efectivo = document.createElement("efectivo");
            rootElement.appendChild(efectivo);


            //Vehiculo
            Element vehiculo = document.createElement("c1");
            vehiculo.appendChild(document.createTextNode(cashTransaction.getVehicleId()));
            efectivo.appendChild(vehiculo);

            // Fecha
            Element fecha = document.createElement("c2");
            efectivo.appendChild(fecha);
            fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cashTransaction.getDate())));
            efectivo.appendChild(fecha);

            // Monto
            Element monto = document.createElement("c3");
            monto.appendChild(document.createTextNode(formatForCurrency.format(cashTransaction.getAmount())));
            efectivo.appendChild(monto);

            // Pasajeros
            Element pasajeros = document.createElement("c4");
            pasajeros.appendChild(document.createTextNode(String.valueOf(cashTransaction.getTotalPassengers())));
            efectivo.appendChild(pasajeros);

        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + fileDataName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private Path generateNumberControl(CashNumberControl cashNumberControl, String fileNumberControlName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        //Nodo raiz <archivo>
        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("efectivo");
        rootElement.setAttributeNode(attrDebito);
        document.appendChild(rootElement);

        //Nodo <versionlayout>
        Element versionLayout = document.createElement("versionlayout");
        versionLayout.appendChild(document.createTextNode("1.0"));

        rootElement.appendChild(versionLayout);

        //Nodo <cifrascontrol>
        Element cifrasControl = document.createElement("cifrascontrol");
        rootElement.appendChild(cifrasControl);

        //Nodo <c1>
        Element idColecta = document.createElement("c1");
        idColecta.appendChild(document.createTextNode(String.valueOf(cashNumberControl.getCutId())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cashNumberControl.getCutDate())));
        cifrasControl.appendChild(fechaColecta);

        //Fecha inicial de corte
        Element fechaInicioCorte = document.createElement("c3");
        fechaInicioCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cashNumberControl.getInitialCutDate())));
        cifrasControl.appendChild(fechaInicioCorte);

        //Fecha final de corte
        Element fechaFinalCorte = document.createElement("c4");
        fechaFinalCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cashNumberControl.getFinalCutDate())));
        cifrasControl.appendChild(fechaFinalCorte);

        //Identificador del operador
        Element idOperador = document.createElement("c5");
        idOperador.appendChild(document.createTextNode(cashNumberControl.getIdSIR()));
        cifrasControl.appendChild(idOperador);

        //Identificador de ruta
        Element idRuta = document.createElement("c6");
        idRuta.appendChild(document.createTextNode(cashNumberControl.getRouteIdDescription()));
        cifrasControl.appendChild(idRuta);

        //Numero total de registros
        Element registrosTotales = document.createElement("c7");
        registrosTotales.appendChild(document.createTextNode(String.valueOf(cashNumberControl.getTotalRecords())));
        cifrasControl.appendChild(registrosTotales);

        //Monto total de registros
        String strMonto = String.valueOf(formatForCurrency.format(cashNumberControl.getTotalAmmountRecords()));
        Element montoRegistrosTotales = document.createElement("c8");
        montoRegistrosTotales.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotales);

        //Total de pasajeros
        Element pasajerosTotales = document.createElement("c9");
        pasajerosTotales.appendChild(document.createTextNode(String.valueOf(cashNumberControl.getTotalPassengers())));
        cifrasControl.appendChild(pasajerosTotales);

        Element idProveedorTecnologico = document.createElement("c10");
        idProveedorTecnologico.appendChild(document.createTextNode(cashNumberControl.getTechnologicalProvider()));
        cifrasControl.appendChild(idProveedorTecnologico);


        Element llaveAES = document.createElement("llaveAES");
        llaveAES.appendChild(document.createTextNode("LLAVE DE SESION EN BASE 64"));
        rootElement.appendChild(llaveAES);

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + fileNumberControlName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private String generateFileName(CashNumberControl cashNumberControl, PrefixFile prefixFile, ExtentionFile extentionFile) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(String.valueOf(cashNumberControl.getCutId())).append("_")
                .append(DateHelper.getDateFormatFull(cashNumberControl.getCutDate())).append("_")
                .append(cashNumberControl.getIdSIR()).append("_")
                .append(cashNumberControl.getRouteIdDescription())
                .append(extentionFile.getExtention());
        return fileName.toString();
    }
}
