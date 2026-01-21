package com.idear.fimpe.torniquete.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.torniquete.domain.TorniqueteFilesGenerator;
import com.idear.fimpe.torniquete.domain.TorniqueteTransaction;
import com.idear.fimpe.torniquete.domain.TorniquteNumberControl;
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

public class TorniqueteFilesGeneratorXML implements TorniqueteFilesGenerator {
    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private DOMSource domSource;
    private StreamResult streamResult;
    private DecimalFormat formatForCurrency;
    private DecimalFormat formatNumber;

    private Path fileNumberControl;
    private Path fileData;

    public TorniqueteFilesGeneratorXML() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        formatForCurrency = (DecimalFormat) numberFormat;
        formatForCurrency.applyPattern("#0.00");
        formatNumber = new DecimalFormat("#000");
    }

    /**
     * Genera los archivos XML CC y DAT.
     * @param torniqueteNumberControl
     * @param prefixFile
     * @throws TorniqueteFilesGeneratorXMLException
     */
    @Override
    public void generateFiles(TorniquteNumberControl torniqueteNumberControl, PrefixFile prefixFile) throws TorniqueteFilesGeneratorXMLException {

        if (prefixFile.equals(PrefixFile.DEBIT)) {
            String fileNumberControlName = generateFileName(torniqueteNumberControl, PrefixFile.DEBIT, ExtentionFile.NUMBER_CONTROL);
            String fileDataName = generateFileName(torniqueteNumberControl, PrefixFile.DEBIT, ExtentionFile.DATA);
            try {
                fileNumberControl = generateDebitNumberControl(torniqueteNumberControl, fileNumberControlName);
                fileData = generateDebitData(torniqueteNumberControl, fileDataName);
            } catch (ParserConfigurationException | TransformerException e) {
                throw new TorniqueteFilesGeneratorXMLException("Error al intentar generar los pares de archivos " + fileNumberControlName);
            }
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

    /**
     * Genera archivo .DAT
     * @param torniquteNumberControl
     * @param debitFileDataName
     * @return Ubicacion del archivo .DAT
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    private Path generateDebitData(TorniquteNumberControl torniquteNumberControl, String debitFileDataName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("debito");
        rootElement.setAttributeNode(attrDebito);

        document.appendChild(rootElement);
        for (TorniqueteTransaction debitTransaction : torniquteNumberControl.getTorniqueteTransactions()) {
            Element debito = document.createElement("debito");
            rootElement.appendChild(debito);


            //Folio de transaccion
            Element folio = document.createElement("c1");
            folio.appendChild(document.createTextNode(debitTransaction.getTransactionFoil()));
            debito.appendChild(folio);

            // Fecha de la transacción
            Element fecha = document.createElement("c2");
            debito.appendChild(fecha);
            fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(debitTransaction.getTransactionDate())));
            debito.appendChild(fecha);

            // UID Tarjeta
            Element uidTarjeta = document.createElement("c3");
            uidTarjeta.appendChild(document.createTextNode(debitTransaction.getSerialCard()));
            debito.appendChild(uidTarjeta);

            // Producto
            Element producto = document.createElement("c4");
            producto.appendChild(document.createTextNode(debitTransaction.getProductId().getFimpeValue()));
            debito.appendChild(producto);

            // Monto de la transacción
            Element monto = document.createElement("c5");
            String strMonto = String.valueOf(formatForCurrency.format(debitTransaction.getTransactionAmount()));
            monto.appendChild(document.createTextNode(strMonto));
            debito.appendChild(monto);

            // Saldo inicial
            Element saldoInicial = document.createElement("c6");
            String strSaldoInicial
                    = String.valueOf(formatForCurrency.format(debitTransaction.getInitialBalance()));

            saldoInicial.appendChild(document.createTextNode(strSaldoInicial));
            debito.appendChild(saldoInicial);

            // Saldo final
            Element saldoFinal = document.createElement("c7");
            String strSaldoFinal
                    = String.valueOf(formatForCurrency.format(debitTransaction.getFinalBalance()));
            saldoFinal.appendChild(document.createTextNode(strSaldoFinal));
            debito.appendChild(saldoFinal);

            // UID dispositivo
            Element uidDispositivo = document.createElement("c8");
            uidDispositivo.appendChild(document.createTextNode(torniquteNumberControl.getSerie()));
            debito.appendChild(uidDispositivo);

            // ID SAM
            Element idSAM = document.createElement("c9");
            idSAM.appendChild(document.createTextNode(debitTransaction.getSamId()));
            debito.appendChild(idSAM);

            // Conscutivo SAM
            Element consecutivoSAM = document.createElement("c10");
            consecutivoSAM.appendChild(document.createTextNode(debitTransaction.getSamTransactionCounter()));
            debito.appendChild(consecutivoSAM);

            // Número transacción en tarjeta
            Element transaccionTarjeta = document.createElement("c11");
            transaccionTarjeta.appendChild(document.createTextNode(String.valueOf(debitTransaction.getCardTransactionCounter())));
            debito.appendChild(transaccionTarjeta);

            // Tipo débito
            Element tipoDebito = document.createElement("c14");
            tipoDebito.appendChild(document.createTextNode(debitTransaction.getDebitType()));
            debito.appendChild(tipoDebito);

            Element idEstacion = document.createElement("c17");
            idEstacion.appendChild(document.createTextNode(String.valueOf(torniquteNumberControl.getStationId())));
            debito.appendChild(idEstacion);

        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + debitFileDataName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    /**
     * Genera archivo .CC
     * @param torniqueteNumberControl
     * @param debitFileNumberControlName
     * @return Ubicacion del archivo .CC
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    private Path generateDebitNumberControl(TorniquteNumberControl torniqueteNumberControl, String debitFileNumberControlName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        //Nodo raiz <archivo>
        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("debito");
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
        idColecta.appendChild(document.createTextNode(String.valueOf(torniqueteNumberControl.getCutId())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(torniqueteNumberControl.getCutDate())));
        cifrasControl.appendChild(fechaColecta);

        //Fecha inicial de corte
        Element fechaInicioCorte = document.createElement("c3");
        fechaInicioCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(torniqueteNumberControl.getInitialCutDate())));
        cifrasControl.appendChild(fechaInicioCorte);

        //Fecha final de corte
        Element fechaFinalCorte = document.createElement("c4");
        fechaFinalCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(torniqueteNumberControl.getFinalCutDate())));
        cifrasControl.appendChild(fechaFinalCorte);

        //Identificador del operador
        Element idOperador = document.createElement("c5");
        idOperador.appendChild(document.createTextNode(torniqueteNumberControl.getEurId()));
        cifrasControl.appendChild(idOperador);

        //Identificador de ruta
        Element idRuta = document.createElement("c6");
        idRuta.appendChild(document.createTextNode(torniqueteNumberControl.getRouteId()));
        cifrasControl.appendChild(idRuta);

        //Numero total de registros
        Element registrosTotales = document.createElement("c7");
        registrosTotales.appendChild(document.createTextNode(String.valueOf(torniqueteNumberControl.getTotalRecords())));
        cifrasControl.appendChild(registrosTotales);

        //Monto total de registros
        String strMonto = String.valueOf(formatForCurrency.format(torniqueteNumberControl.getTotalAmmountRecords()));
        Element montoRegistrosTotales = document.createElement("c8");
        montoRegistrosTotales.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotales);

        Element idProveedorTecnologico = document.createElement("c9");
        idProveedorTecnologico.appendChild(document.createTextNode(torniqueteNumberControl.getTechnologicProviderId()));
        cifrasControl.appendChild(idProveedorTecnologico);

        Element totalViajes = document.createElement("c10");
        totalViajes.appendChild(document.createTextNode(String.valueOf(torniqueteNumberControl.getTotalBPDs())));
        cifrasControl.appendChild(totalViajes);

        Element llaveAES = document.createElement("llaveAES");
        llaveAES.appendChild(document.createTextNode("LLAVE DE SESION EN BASE 64"));
        rootElement.appendChild(llaveAES);

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + debitFileNumberControlName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    /**
     * Genera nombre de archivo
     * @param torniquteNumberControl
     * @param prefixFile
     * @param extentionFile
     * @return Cadena con el nombre del archivo, puede ser el CC o DAT
     */
    private String generateFileName(TorniquteNumberControl torniquteNumberControl, PrefixFile prefixFile, ExtentionFile extentionFile) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(String.valueOf(torniquteNumberControl.getCutId())).append("_")
                .append(DateHelper.getDateFormatFull(torniquteNumberControl.getCutDate())).append("_")
                .append(torniquteNumberControl.getEurId()).append("_")
                .append(torniquteNumberControl.getRouteId())
                .append(extentionFile.getExtention());
        return fileName.toString();
    }
}
