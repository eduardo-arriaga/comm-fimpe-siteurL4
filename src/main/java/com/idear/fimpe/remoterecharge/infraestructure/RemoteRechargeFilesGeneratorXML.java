package com.idear.fimpe.remoterecharge.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.properties.PropertiesHelper;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeFilesGenerator;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeGeneratorException;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeNumberControl;
import com.idear.fimpe.remoterecharge.domain.RemoteRechargeTransaction;
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

import static com.idear.fimpe.enums.ExtentionFile.DATA;
import static com.idear.fimpe.enums.ExtentionFile.NUMBER_CONTROL;
import static com.idear.fimpe.enums.PrefixFile.REMOTE_RECHARGE;

public class RemoteRechargeFilesGeneratorXML implements RemoteRechargeFilesGenerator {

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private DOMSource domSource;
    private StreamResult streamResult;
    private Path fileNumberControl;
    private Path fileData;
    private DecimalFormat formatForCurrency;

    public RemoteRechargeFilesGeneratorXML(){
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        formatForCurrency = (DecimalFormat) numberFormat;
        formatForCurrency.applyPattern("#0.00");
    }

    @Override
    public void generateFiles(RemoteRechargeNumberControl remoteRechargeNumberControl) throws RemoteRechargeGeneratorException {
        String numberControlFileName = generateFileName(remoteRechargeNumberControl, REMOTE_RECHARGE, NUMBER_CONTROL);
        String dataFileName = generateFileName(remoteRechargeNumberControl, REMOTE_RECHARGE, DATA);

        try {
            fileNumberControl = generateNumberControlFile(remoteRechargeNumberControl, numberControlFileName);
            fileData = generateDataFile(remoteRechargeNumberControl, dataFileName);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new RemoteRechargeGeneratorException("Error al intentar generar los archivos RECARGA REMOTA", e);
        }
    }

    private Path generateDataFile(RemoteRechargeNumberControl remoteRechargeNumberControl,
                                  String dataFileName) throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element fileNode = document.createElement("archivo");
        Attr attributeType = document.createAttribute("tipo");
        attributeType.setValue("lapr");
        fileNode.setAttributeNode(attributeType);
        document.appendChild(fileNode);

        Element requests = document.createElement("solicitudes");
        if(remoteRechargeNumberControl.getRequests().size() > 0)
            fileNode.appendChild(requests);

        for (RemoteRechargeTransaction remoteRechargeTransaction : remoteRechargeNumberControl.getRequests()) {
            Element rechargeNode = document.createElement("recarga");
            requests.appendChild(rechargeNode);

            //Serial de la tarjeta
            Element cardId = document.createElement("c1");
            cardId.appendChild(document.createTextNode(remoteRechargeTransaction.getCardId()));
            rechargeNode.appendChild(cardId);

            //Fecha de peticion
            Element registerDate = document.createElement("c2");
            registerDate.appendChild(
                    document.createTextNode(
                            DateHelper.getDateFormatFull(
                                    remoteRechargeTransaction.getRegisterDateTime())));

            rechargeNode.appendChild(registerDate);

            //Numero de accion aplicada en el producto
            Element actionNumberAppliedToProduct = document.createElement("c3");
            actionNumberAppliedToProduct.appendChild(
                    document.createTextNode(
                            String.valueOf(remoteRechargeTransaction.getActionNumberAppliedToProduct())));
            rechargeNode.appendChild(actionNumberAppliedToProduct);

            // Monto de la recarga
            Element amount = document.createElement("c4");
            amount.appendChild(document.createTextNode(formatForCurrency.format(remoteRechargeTransaction.getAmount())));
            rechargeNode.appendChild(amount);

            //Producto
            Element productId = document.createElement("c5");
            productId.appendChild(document.createTextNode(remoteRechargeTransaction.getProductId()));
            rechargeNode.appendChild(productId);

            //Tipo de recarga
            Element rechargeType = document.createElement("c6");
            rechargeType.appendChild(document.createTextNode(remoteRechargeTransaction.getRechargeType()));
            rechargeNode.appendChild(rechargeType);
        }

        Element confirmations = document.createElement("confirmaciones");
        if(remoteRechargeNumberControl.getConfirmations().size() > 0)
            fileNode.appendChild(confirmations);

        for (RemoteRechargeTransaction remoteRechargeTransaction : remoteRechargeNumberControl.getConfirmations()) {
            Element rechargeNode = document.createElement("recarga");
            confirmations.appendChild(rechargeNode);

            //Serial de la tarjeta
            Element cardId = document.createElement("c1");
            cardId.appendChild(document.createTextNode(remoteRechargeTransaction.getCardId()));
            rechargeNode.appendChild(cardId);

            //Fecha de peticion
            Element registerDate = document.createElement("c2");
            registerDate.appendChild(
                    document.createTextNode(
                            DateHelper.getDateFormatFull(
                                    remoteRechargeTransaction.getRegisterDateTime())));

            rechargeNode.appendChild(registerDate);

            //Numero de accion aplicada en el producto
            Element actionNumberAppliedToProduct = document.createElement("c3");
            actionNumberAppliedToProduct.appendChild(
                    document.createTextNode(
                            String.valueOf(remoteRechargeTransaction.getActionNumberAppliedToProduct())));
            rechargeNode.appendChild(actionNumberAppliedToProduct);

            // Monto de la recarga
            Element amount = document.createElement("c4");
            amount.appendChild(document.createTextNode(formatForCurrency.format(remoteRechargeTransaction.getAmount())));
            rechargeNode.appendChild(amount);

            //Producto
            Element productId = document.createElement("c5");
            productId.appendChild(document.createTextNode(remoteRechargeTransaction.getProductId()));
            rechargeNode.appendChild(productId);

            //Tipo de recarga
            Element rechargeType = document.createElement("c6");
            rechargeType.appendChild(document.createTextNode(remoteRechargeTransaction.getRechargeType()));
            rechargeNode.appendChild(rechargeType);

            //SAM
            Element samId = document.createElement("c7");
            samId.appendChild(document.createTextNode(remoteRechargeTransaction.getSamId()));
            rechargeNode.appendChild(samId);
        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + dataFileName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private Path generateNumberControlFile(RemoteRechargeNumberControl remoteRechargeNumberControl,
                                           String numberControlFileName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        //Nodo raiz <archivo>
        Element fileNode = document.createElement("archivo");
        Attr attributeType = document.createAttribute("tipo");
        attributeType.setValue("lapr");
        fileNode.setAttributeNode(attributeType);
        document.appendChild(fileNode);

        //Nodo <versionlayout>
        Element versionLayout = document.createElement("versionlayout");
        versionLayout.appendChild(document.createTextNode("1.0"));

        fileNode.appendChild(versionLayout);

        //Nodo <cifrascontrol>
        Element numberControlNode = document.createElement("cifrascontrol");
        fileNode.appendChild(numberControlNode);

        //Folio de corte
        Element cutId = document.createElement("c1");
        cutId.appendChild(document.createTextNode(String.valueOf(remoteRechargeNumberControl.getCutFoil())));
        numberControlNode.appendChild(cutId);

        // Fecha de colecta
        Element cutDate = document.createElement("c2");
        cutDate.appendChild(document.createTextNode(
                DateHelper.getDateFormatFull(remoteRechargeNumberControl.getGenerationDateTime())));

        numberControlNode.appendChild(cutDate);

        //Numero total de registros
        Element totalRecords = document.createElement("c3");
        totalRecords.appendChild(document.createTextNode(String.valueOf(remoteRechargeNumberControl.getTotalRecords())));
        numberControlNode.appendChild(totalRecords);

        //Monto total de registros
        Element totalAmountRecords = document.createElement("c4");
        totalAmountRecords.appendChild(
                document.createTextNode(formatForCurrency.format(remoteRechargeNumberControl.getTotalRecordAmount())));
        numberControlNode.appendChild(totalAmountRecords);

        //Numero total de solicitudes
        Element totalRequests = document.createElement("c5");
        totalRequests.appendChild(
                document.createTextNode(String.valueOf(remoteRechargeNumberControl.getTotalRequestRecords())));
        numberControlNode.appendChild(totalRequests);

        //Monto total de solicitudes
        Element totalAmountRequests = document.createElement("c6");
        totalAmountRequests.appendChild(
                document.createTextNode(formatForCurrency.format(remoteRechargeNumberControl.getTotalRequestRecordAmount())));
        numberControlNode.appendChild(totalAmountRequests);

        //Numero total de confirmaciones
        Element totalConfirmations = document.createElement("c7");
        totalConfirmations.appendChild(
                document.createTextNode(String.valueOf(remoteRechargeNumberControl.getTotalConfirmationRecords())));
        numberControlNode.appendChild(totalConfirmations);

        //Monto total de confirmaciones
        Element totalAmountConfirmations = document.createElement("c8");
        totalAmountConfirmations.appendChild(
                document.createTextNode(
                        formatForCurrency.format(remoteRechargeNumberControl.getTotalConfirmationRecordAmount())));
        numberControlNode.appendChild(totalAmountConfirmations);

        //proveedor tecnologico
        Element technologicProviderId = document.createElement("c9");
        technologicProviderId.appendChild(document.createTextNode(PropertiesHelper.TECHNOLOGIC_PROVIDER_ID));
        numberControlNode.appendChild(technologicProviderId);

        Element aesKey = document.createElement("llaveAES");
        aesKey.appendChild(document.createTextNode("LLAVE DE SESION EN BASE 64"));
        fileNode.appendChild(aesKey);

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + numberControlFileName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private String generateFileName(RemoteRechargeNumberControl remoteRechargeNumberControl,
                                    PrefixFile prefixFile,
                                    ExtentionFile extentionFile) {

        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(remoteRechargeNumberControl.getCutFoil()).append("_")
                .append(DateHelper.getDateFormatFull(remoteRechargeNumberControl.getGenerationDateTime())).append("_")
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
