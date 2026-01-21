package com.idear.fimpe.lam.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.lam.domain.LAMFilesGenerator;
import com.idear.fimpe.lam.domain.LAMFilesGeneratorException;
import com.idear.fimpe.lam.domain.LAMNumberControl;
import com.idear.fimpe.lam.domain.LAMTransaction;
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

import static com.idear.fimpe.enums.ExtentionFile.*;
import static com.idear.fimpe.enums.PrefixFile.*;

public class LAMFilesGeneratorXML implements LAMFilesGenerator {

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private DOMSource domSource;
    private StreamResult streamResult;
    private Path fileNumberControl;
    private Path fileData;

    public LAMFilesGeneratorXML() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
    }

    @Override
    public void generateFiles(LAMNumberControl lamNumberControl) throws LAMFilesGeneratorException {
        String numberControlFileName = generateFileName(lamNumberControl, ACTION_LIST, NUMBER_CONTROL);
        String dataFileName = generateFileName(lamNumberControl, ACTION_LIST, DATA);

        try {
            fileNumberControl = generateNumberControlFile(lamNumberControl, numberControlFileName);
            fileData = generateDataFile(lamNumberControl, dataFileName);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new LAMFilesGeneratorException("Error al intentar generar los archivos LAM", e);
        }
    }

    private Path generateDataFile(LAMNumberControl lamNumberControl, String dataFileName) throws ParserConfigurationException, TransformerException {
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element archivo = document.createElement("archivo");
        Attr attrLam = document.createAttribute("tipo");
        attrLam.setValue("lam");
        archivo.setAttributeNode(attrLam);
        document.appendChild(archivo);

        Element solicitudes = document.createElement("solicitudes");
        if(lamNumberControl.getLamRequestTransactions().size() > 0)
            archivo.appendChild(solicitudes);

        for (LAMTransaction lamTransaction : lamNumberControl.getLamRequestTransactions()) {
            Element tarjeta = document.createElement("tarjeta");
            solicitudes.appendChild(tarjeta);

            //Serial de la tarjeta
            Element serialTarjeta = document.createElement("c1");
            serialTarjeta.appendChild(document.createTextNode(lamTransaction.getSerialCard()));
            tarjeta.appendChild(serialTarjeta);

            //Fecha de deteccion
            Element fecha = document.createElement("c2");
            fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(lamTransaction.getDateTimeDetection())));
            tarjeta.appendChild(fecha);

            //Consecutivo de la tarjeta
            Element consecutivoTarjeta = document.createElement("c3");
            consecutivoTarjeta.appendChild(document.createTextNode(lamTransaction.getCardTransactionCounter()));
            tarjeta.appendChild(consecutivoTarjeta);

            // Accion
            Element accion = document.createElement("c4");
            accion.appendChild(document.createTextNode(String.valueOf(lamTransaction.getAction().getKey())));
            tarjeta.appendChild(accion);
        }

        Element confirmaciones = document.createElement("confirmaciones");
        if(lamNumberControl.getLamConfirmationTransactions().size() > 0)
            archivo.appendChild(confirmaciones);

        for (LAMTransaction lamTransaction : lamNumberControl.getLamConfirmationTransactions()) {
            Element tarjeta = document.createElement("tarjeta");
            confirmaciones.appendChild(tarjeta);

            //Serial de la tarjeta
            Element serialTarjeta = document.createElement("c1");
            serialTarjeta.appendChild(document.createTextNode(lamTransaction.getSerialCard()));
            tarjeta.appendChild(serialTarjeta);

            //Fecha de deteccion
            Element fecha = document.createElement("c2");
            fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(lamTransaction.getDateTimeDetection())));
            tarjeta.appendChild(fecha);

            //Consecutivo de la tarjeta
            Element consecutivoTarjeta = document.createElement("c3");
            consecutivoTarjeta.appendChild(document.createTextNode(lamTransaction.getCardTransactionCounter()));
            tarjeta.appendChild(consecutivoTarjeta);

            // Accion
            Element accion = document.createElement("c4");
            accion.appendChild(document.createTextNode(String.valueOf(lamTransaction.getAction().getKey())));
            tarjeta.appendChild(accion);
        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + dataFileName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private Path generateNumberControlFile(LAMNumberControl lamNumberControl, String numberControlFileName) throws ParserConfigurationException, TransformerException {
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        //Nodo raiz <archivo>
        Element archivo = document.createElement("archivo");
        Attr attrLam = document.createAttribute("tipo");
        attrLam.setValue("lam");
        archivo.setAttributeNode(attrLam);
        document.appendChild(archivo);

        //Nodo <versionlayout>
        Element versionLayout = document.createElement("versionlayout");
        versionLayout.appendChild(document.createTextNode("1.0"));

        archivo.appendChild(versionLayout);

        //Nodo <cifrascontrol>
        Element cifrasControl = document.createElement("cifrascontrol");
        archivo.appendChild(cifrasControl);

        //Nodo <c1>
        Element idColecta = document.createElement("c1");
        idColecta.appendChild(document.createTextNode(String.valueOf(lamNumberControl.getFolioCut())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(lamNumberControl.getDateTimeSend())));
        cifrasControl.appendChild(fechaColecta);

        //Total de transacciones
        Element totalTransacciones = document.createElement("c3");
        totalTransacciones.appendChild(document.createTextNode(String.valueOf(lamNumberControl.getTotalRecords())));
        cifrasControl.appendChild(totalTransacciones);

        //proveedor tecnologico
        Element proveedorTecnologico = document.createElement("c4");
        proveedorTecnologico.appendChild(document.createTextNode(PropertiesHelper.TECHNOLOGIC_PROVIDER_ID));
        cifrasControl.appendChild(proveedorTecnologico);

        Element llaveAES = document.createElement("llaveAES");
        llaveAES.appendChild(document.createTextNode("LLAVE DE SESION EN BASE 64"));
        archivo.appendChild(llaveAES);

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + numberControlFileName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private String generateFileName(LAMNumberControl lamNumberControl, PrefixFile prefixFile, ExtentionFile extentionFile) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(String.valueOf(lamNumberControl.getFolioCut())).append("_")
                .append(DateHelper.getDateFormatFull(lamNumberControl.getDateTimeSend())).append("_")
                .append(PropertiesHelper.TECHNOLOGIC_PROVIDER_ID)
                .append(extentionFile.getExtention());
        return fileName.toString();
    }

    @Override
    public Path getNumberControlFile() {
        return fileNumberControl;
    }

    @Override
    public Path getDataFile() {
        return fileData;
    }
}
