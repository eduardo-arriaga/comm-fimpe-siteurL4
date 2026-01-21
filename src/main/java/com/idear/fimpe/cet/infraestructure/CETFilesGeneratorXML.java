package com.idear.fimpe.cet.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.cet.domain.CETFilesGenerator;
import com.idear.fimpe.cet.domain.CETNumberControl;
import com.idear.fimpe.cet.domain.CETTransaction;
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

public class CETFilesGeneratorXML implements CETFilesGenerator {

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

    public CETFilesGeneratorXML() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        formatForCurrency = (DecimalFormat) numberFormat;
        formatForCurrency.applyPattern("#0.00");
        formatNumber = new DecimalFormat("#000");
    }

    @Override
    public void generateFiles(CETNumberControl cetNumberControl, PrefixFile prefixFile) throws CETFilesGeneratorXMLException {

        if (prefixFile.equals(PrefixFile.DEBIT)) {
            String fileNumberControlName = generateFileName(cetNumberControl, PrefixFile.DEBIT, ExtentionFile.NUMBER_CONTROL);
            String fileDataName = generateFileName(cetNumberControl, PrefixFile.DEBIT, ExtentionFile.DATA);
            try {
                fileNumberControl = generateDebitNumberControl(cetNumberControl, fileNumberControlName);
                fileData = generateDebitData(cetNumberControl, fileDataName);
            } catch (ParserConfigurationException | TransformerException e) {
                throw new CETFilesGeneratorXMLException("Error al intentar generar los pares de archivos " + fileNumberControlName);
            }
        } if(prefixFile.equals(PrefixFile.RECHARGE)){
            String fileNumberControlName = generateFileName(cetNumberControl, PrefixFile.RECHARGE, ExtentionFile.NUMBER_CONTROL);
            String fileDataName = generateFileName(cetNumberControl, PrefixFile.RECHARGE, ExtentionFile.DATA);
            try {
                fileNumberControl = generateRechargeNumberControl(cetNumberControl, fileNumberControlName);
                fileData = generateRechargeData(cetNumberControl, fileDataName);
            } catch (ParserConfigurationException | TransformerException e) {
                throw new CETFilesGeneratorXMLException("Error al intentar generar los pares de archivos " + fileNumberControlName);
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

    private Path generateDebitData(CETNumberControl cetNumberControl, String debitFileDataName)
            throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("debito");
        rootElement.setAttributeNode(attrDebito);

        document.appendChild(rootElement);
        for (CETTransaction debitTransaction : cetNumberControl.getCetTransactions()) {
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
            String strMonto = String.valueOf(formatForCurrency.format(debitTransaction.getTransactionAmmount()));
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
            uidDispositivo.appendChild(document.createTextNode(cetNumberControl.getDeviceId()));
            debito.appendChild(uidDispositivo);

            // ID SAM
            Element idSAM = document.createElement("c9");
            idSAM.appendChild(document.createTextNode(debitTransaction.getSamId()));
            debito.appendChild(idSAM);

            // Conscutivo SAM
            Element consecutivoSAM = document.createElement("c10");
            consecutivoSAM.appendChild(document.createTextNode(debitTransaction.getSamTransactionCounter())
            );
            debito.appendChild(consecutivoSAM);

            // Número transacción en tarjeta
            Element transaccionTarjeta = document.createElement("c11");
            transaccionTarjeta.appendChild(document.createTextNode(String.valueOf(debitTransaction.getCardTransactionCounter()))
            );
            debito.appendChild(transaccionTarjeta);

            // Tipo débito
            Element tipoDebito = document.createElement("c14");
            tipoDebito.appendChild(document.createTextNode(debitTransaction.getDebitType()));
            debito.appendChild(tipoDebito);

            Element idVehiculo = document.createElement("c17");
            idVehiculo.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getVehicleId())));
            debito.appendChild(idVehiculo);

        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + debitFileDataName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private Path generateDebitNumberControl(CETNumberControl cetNumberControl, String debitFileNumberControlName)
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
        idColecta.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getCutId())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cetNumberControl.getCutDate())));
        cifrasControl.appendChild(fechaColecta);

        //Fecha inicial de corte
        Element fechaInicioCorte = document.createElement("c3");
        fechaInicioCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cetNumberControl.getInitialCutDate())));
        cifrasControl.appendChild(fechaInicioCorte);

        //Fecha final de corte
        Element fechaFinalCorte = document.createElement("c4");
        fechaFinalCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cetNumberControl.getFinalCutDate())));
        cifrasControl.appendChild(fechaFinalCorte);

        //Identificador del operador
        Element idOperador = document.createElement("c5");
        idOperador.appendChild(document.createTextNode(cetNumberControl.getEurId()));
        cifrasControl.appendChild(idOperador);

        //Identificador de ruta
        Element idRuta = document.createElement("c6");
        idRuta.appendChild(document.createTextNode(cetNumberControl.getRouteIdDescription()));
        cifrasControl.appendChild(idRuta);

        //Numero total de registros
        Element registrosTotales = document.createElement("c7");
        registrosTotales.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getTotalRecords())));
        cifrasControl.appendChild(registrosTotales);

        //Monto total de registros
        String strMonto = String.valueOf(formatForCurrency.format(cetNumberControl.getTotalAmmountRecords()));
        Element montoRegistrosTotales = document.createElement("c8");
        montoRegistrosTotales.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotales);

        Element idProveedorTecnologico = document.createElement("c9");
        idProveedorTecnologico.appendChild(document.createTextNode(cetNumberControl.getTechnologicProviderId()));
        cifrasControl.appendChild(idProveedorTecnologico);

        Element totalViajes = document.createElement("c10");
        totalViajes.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getTotalBPDs())));
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

    private Path generateRechargeNumberControl(CETNumberControl cetNumberControl, String fileNumberControlName) throws ParserConfigurationException, TransformerException {
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        //Nodo raiz <archivo>
        Element rootElement = document.createElement("archivo");
        Attr attrDebito = document.createAttribute("tipo");
        attrDebito.setValue("ventarecargas");
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
        idColecta.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getCutId())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cetNumberControl.getCutDate())));
        cifrasControl.appendChild(fechaColecta);

        //Fecha inicial de corte
        Element fechaInicioCorte = document.createElement("c3");
        fechaInicioCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cetNumberControl.getInitialCutDate())));
        cifrasControl.appendChild(fechaInicioCorte);

        //Fecha final de corte
        Element fechaFinalCorte = document.createElement("c4");
        fechaFinalCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(cetNumberControl.getFinalCutDate())));
        cifrasControl.appendChild(fechaFinalCorte);

        //Identificador del operador
        Element idOperador = document.createElement("c5");
        idOperador.appendChild(document.createTextNode(cetNumberControl.getEurId()));
        cifrasControl.appendChild(idOperador);

        //Identificador de ruta
        Element idRuta = document.createElement("c6");
        idRuta.appendChild(document.createTextNode(cetNumberControl.getRouteIdDescription()));
        cifrasControl.appendChild(idRuta);

        //Numero total de registros
        Element registrosTotales = document.createElement("c7");
        registrosTotales.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getTotalRecords())));
        cifrasControl.appendChild(registrosTotales);

        //Monto total de registros
        String strMonto = String.valueOf(formatForCurrency.format(cetNumberControl.getTotalAmmountRecords()));
        Element montoRegistrosTotales = document.createElement("c8");
        montoRegistrosTotales.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotales);

        //Total de registros venta
        Element registrosTotalesVenta = document.createElement("c9");
        registrosTotalesVenta.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getTotalCardSales())));
        cifrasControl.appendChild(registrosTotalesVenta);

        //Monto total de registros venta
        strMonto = String.valueOf(formatForCurrency.format(cetNumberControl.getTotalAmmountCardSales()));
        Element montoRegistrosTotalesVenta = document.createElement("c10");
        montoRegistrosTotalesVenta.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotalesVenta);

        //Numero total de registros de recarga
        Element registrosTotalesRecarga = document.createElement("c11");
        registrosTotalesRecarga.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getTotalRecharges())));
        cifrasControl.appendChild(registrosTotalesRecarga);

        //Monto total de registros de recargas
        strMonto = String.valueOf(formatForCurrency.format(cetNumberControl.getTotalAmmountRecharges()));
        Element montoRegistrosTotalesRecargas = document.createElement("c12");
        montoRegistrosTotalesRecargas.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotalesRecargas);

        Element totalViajes = document.createElement("c13");
        totalViajes.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getTotalBPDs())));
        cifrasControl.appendChild(totalViajes);

        Element idProveedorTecnologico = document.createElement("c14");
        idProveedorTecnologico.appendChild(document.createTextNode(cetNumberControl.getTechnologicProviderId()));
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

    private Path generateRechargeData(CETNumberControl cetNumberControl, String fileDataName) throws ParserConfigurationException, TransformerException {
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement("archivo");
        Attr attrRecarga = document.createAttribute("tipo");
        attrRecarga.setValue("ventarecargas");
        rootElement.setAttributeNode(attrRecarga);

        document.appendChild(rootElement);
        for (CETTransaction rechargeTransaction : cetNumberControl.getCetTransactions()) {
            Element recarga = document.createElement("recarga");
            rootElement.appendChild(recarga);

            //Folio de transaccion
            Element folio = document.createElement("c1");
            folio.appendChild(document.createTextNode(rechargeTransaction.getTransactionFoil()));
            recarga.appendChild(folio);

            // Fecha de la transacción
            Element fecha = document.createElement("c2");
            recarga.appendChild(fecha);
            fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(rechargeTransaction.getTransactionDate())));
            recarga.appendChild(fecha);

            // UID Tarjeta
            Element uidTarjeta = document.createElement("c3");
            uidTarjeta.appendChild(document.createTextNode(rechargeTransaction.getSerialCard()));
            recarga.appendChild(uidTarjeta);

            // Producto
            Element producto = document.createElement("c4");
            producto.appendChild(document.createTextNode(rechargeTransaction.getProductId().getFimpeValue()));
            recarga.appendChild(producto);

            // Monto de la transacción
            Element monto = document.createElement("c5");
            String strMonto = String.valueOf(formatForCurrency.format(rechargeTransaction.getTransactionAmmount()));
            monto.appendChild(document.createTextNode(strMonto));
            recarga.appendChild(monto);

            // Saldo inicial
            Element saldoInicial = document.createElement("c10");
            String strSaldoInicial
                    = String.valueOf(formatForCurrency.format(rechargeTransaction.getInitialBalance()));

            saldoInicial.appendChild(document.createTextNode(strSaldoInicial));
            recarga.appendChild(saldoInicial);

            // Saldo final
            Element saldoFinal = document.createElement("c11");
            String strSaldoFinal
                    = String.valueOf(formatForCurrency.format(rechargeTransaction.getFinalBalance()));
            saldoFinal.appendChild(document.createTextNode(strSaldoFinal));
            recarga.appendChild(saldoFinal);

            // UID dispositivo
            Element uidDispositivo = document.createElement("c12");
            uidDispositivo.appendChild(document.createTextNode(cetNumberControl.getDeviceId()));
            recarga.appendChild(uidDispositivo);

            // ID SAM
            Element idSAM = document.createElement("c13");
            idSAM.appendChild(document.createTextNode(rechargeTransaction.getSamId()));
            recarga.appendChild(idSAM);

            // Conscutivo SAM
            Element consecutivoSAM = document.createElement("c14");
            consecutivoSAM.appendChild(document.createTextNode(rechargeTransaction.getSamTransactionCounter())
            );
            recarga.appendChild(consecutivoSAM);

            // Número transacción en tarjeta
            Element transaccionTarjeta = document.createElement("c15");
            transaccionTarjeta.appendChild(document.createTextNode(String.valueOf(rechargeTransaction.getCardTransactionCounter())));
            recarga.appendChild(transaccionTarjeta);

            // Tipo recarga
            Element tipoDebito = document.createElement("c18");
            tipoDebito.appendChild(document.createTextNode(rechargeTransaction.getRechargeType()));
            recarga.appendChild(tipoDebito);

            Element idVehiculo = document.createElement("c21");
            idVehiculo.appendChild(document.createTextNode(String.valueOf(cetNumberControl.getVehicleId())));
            recarga.appendChild(idVehiculo);
        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + fileDataName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private String generateFileName(CETNumberControl cetNumberControl, PrefixFile prefixFile, ExtentionFile extentionFile) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(String.valueOf(cetNumberControl.getCutId())).append("_")
                .append(DateHelper.getDateFormatFull(cetNumberControl.getCutDate())).append("_")
                .append(cetNumberControl.getEurId()).append("_")
                .append(cetNumberControl.getRouteIdDescription())
                .append(extentionFile.getExtention());
        return fileName.toString();
    }
}
