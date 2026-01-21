package com.idear.fimpe.kilometers.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.kilometers.domain.KilometersFilesGenerator;
import com.idear.fimpe.kilometers.domain.KilometersNumberControl;
import com.idear.fimpe.kilometers.domain.KilometersTransaction;
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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class KilometersFilesGeneratorXML implements KilometersFilesGenerator {

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private DOMSource domSource;
    private StreamResult streamResult;
    private DecimalFormat formatForCurrency;

    private Path fileNumberControl;
    private Path fileData;

    public KilometersFilesGeneratorXML() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        formatForCurrency = (DecimalFormat) numberFormat;
        formatForCurrency.applyPattern("#0.00");
    }

    @Override
    public void generateFiles(KilometersNumberControl countersNumberControl, PrefixFile prefixFile) throws KilometersFileGeneratorException {

        String fileNumberControlName = generateFileName(countersNumberControl, prefixFile, ExtentionFile.NUMBER_CONTROL);
        String fileDataName = generateFileName(countersNumberControl, prefixFile, ExtentionFile.DATA);
        try {
            fileNumberControl = generateNumberControl(countersNumberControl, fileNumberControlName);
            fileData = generateFileData(countersNumberControl, fileDataName);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new KilometersFileGeneratorException("Error al intentar generar los pares de archivos " + fileNumberControlName);
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

    private Path generateFileData(KilometersNumberControl kilometersNumberControl, String fileDataName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("kilometros");
        rootElement.setAttributeNode(attrDebito);

        document.appendChild(rootElement);
        for (KilometersTransaction kilometersTransaction : kilometersNumberControl.getKilometersTransactions()) {
            Element efectivo = document.createElement("kilometros");
            rootElement.appendChild(efectivo);


            //Vehiculo
            Element vehiculo = document.createElement("c1");
            vehiculo.appendChild(document.createTextNode(kilometersTransaction.getVehicleId()));
            efectivo.appendChild(vehiculo);

            // Fecha
            Element fecha = document.createElement("c2");
            efectivo.appendChild(fecha);
            fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(kilometersTransaction.getDate())));
            efectivo.appendChild(fecha);

            // Vueltas
            Element vueltas = document.createElement("c3");
            vueltas.appendChild(document.createTextNode(formatCurrency(kilometersTransaction.getLaps())));
            efectivo.appendChild(vueltas);

            // Kilometros recorridos
            Element kilometrosRecorridos = document.createElement("c4");
            kilometrosRecorridos.appendChild(document.createTextNode(formatCurrency(kilometersTransaction.getTraveledKilometers())));
            efectivo.appendChild(kilometrosRecorridos);

            Element kilometrosAcumulados = document.createElement("c5");
            kilometrosAcumulados.appendChild(document.createTextNode(formatCurrency(kilometersTransaction.getAccumulatedKilometers())));
            efectivo.appendChild(kilometrosAcumulados);
        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + fileDataName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private Path generateNumberControl(KilometersNumberControl kilometersNumberControl, String fileNumberControlName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        //Nodo raiz <archivo>
        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("kilometros");
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
        idColecta.appendChild(document.createTextNode(String.valueOf(kilometersNumberControl.getCutId())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(kilometersNumberControl.getCutDate())));
        cifrasControl.appendChild(fechaColecta);

        //Fecha inicial de corte
        Element fechaInicioCorte = document.createElement("c3");
        fechaInicioCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(kilometersNumberControl.getInitialCutDate())));
        cifrasControl.appendChild(fechaInicioCorte);

        //Fecha final de corte
        Element fechaFinalCorte = document.createElement("c4");
        fechaFinalCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(kilometersNumberControl.getFinalCutDate())));
        cifrasControl.appendChild(fechaFinalCorte);

        //Identificador del operador
        Element idOperador = document.createElement("c5");
        idOperador.appendChild(document.createTextNode(kilometersNumberControl.getIdSIR()));
        cifrasControl.appendChild(idOperador);

        //Identificador de ruta
        Element idRuta = document.createElement("c6");
        idRuta.appendChild(document.createTextNode(kilometersNumberControl.getRouteIdDescription()));
        cifrasControl.appendChild(idRuta);

        //Numero total de registros
        Element registrosTotales = document.createElement("c7");
        registrosTotales.appendChild(document.createTextNode(String.valueOf(kilometersNumberControl.getTotalRecords())));
        cifrasControl.appendChild(registrosTotales);

        //Total de kilometros
        String totalKilometros = formatCurrency(kilometersNumberControl.getTotalKilometers());
        Element montoRegistrosTotales = document.createElement("c8");
        montoRegistrosTotales.appendChild(document.createTextNode(totalKilometros));
        cifrasControl.appendChild(montoRegistrosTotales);

        //Total de vueltas
        Element totalVueltas = document.createElement("c9");
        totalVueltas.appendChild(document.createTextNode(formatCurrency(kilometersNumberControl.getTotalLaps())));
        cifrasControl.appendChild(totalVueltas);

        Element idProveedorTecnologico = document.createElement("c10");
        idProveedorTecnologico.appendChild(document.createTextNode(kilometersNumberControl.getTechnologicalProvider()));
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

    private String formatCurrency(Double value){
        BigDecimal bigDecimal = new BigDecimal(value);
        return  String.format(Locale.US, "%.2f",bigDecimal);
    }

    private String generateFileName(KilometersNumberControl countersNumberControl, PrefixFile prefixFile, ExtentionFile extentionFile) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(countersNumberControl.getCutId()).append("_")
                .append(DateHelper.getDateFormatFull(countersNumberControl.getCutDate())).append("_")
                .append(countersNumberControl.getIdSIR()).append("_")
                .append(countersNumberControl.getRouteIdDescription())
                .append(extentionFile.getExtention());
        return fileName.toString();
    }
}
