package com.idear.fimpe.vrt.infraestructure;

import com.idear.fimpe.enums.ExtentionFile;
import com.idear.fimpe.enums.Folder;
import com.idear.fimpe.enums.OperationType;
import com.idear.fimpe.enums.PrefixFile;
import com.idear.fimpe.helpers.dates.DateHelper;
import com.idear.fimpe.vrt.domain.ProductSale;
import com.idear.fimpe.vrt.domain.VRTFilesGenerator;
import com.idear.fimpe.vrt.domain.VRTNumberControl;
import com.idear.fimpe.vrt.domain.VRTTransaction;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class VRTFilesGeneratorXML implements VRTFilesGenerator {

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    private DocumentBuilder documentBuilder;
    private Document document;
    private DOMSource domSource;
    private StreamResult streamResult;
    private DecimalFormat formatForCurrency;

    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    private Path fileNumberControl;
    private Path fileData;

    public VRTFilesGeneratorXML() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        formatForCurrency = (DecimalFormat) numberFormat;
        formatForCurrency.applyPattern("#0.00");
        dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        timeFormatter = DateTimeFormatter.ofPattern("HHmm");
    }

    @Override
    public void generateFiles(VRTNumberControl vrtNumberControl) throws VRTFilesGeneratorXMLException {
        String fileNumberControlName = generateFileName(vrtNumberControl, PrefixFile.RECHARGE, ExtentionFile.NUMBER_CONTROL);
        String fileDataName = generateFileName(vrtNumberControl, PrefixFile.RECHARGE, ExtentionFile.DATA);
        try {
            fileNumberControl = generateRechargeSaleNumberControl(vrtNumberControl, fileNumberControlName);
            fileData = generateRechargeSaleData(vrtNumberControl, fileDataName);
        } catch (TransformerException | ParserConfigurationException e) {
            throw new VRTFilesGeneratorXMLException("Error al intentar generar los archivos para VRT", e);
        }
    }

    private Path generateRechargeSaleData(VRTNumberControl vrtNumberControl, String fileDataName) throws ParserConfigurationException, TransformerException {

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element rootElement = document.createElement("archivo");
        Attr attrRecarga = document.createAttribute("tipo");
        attrRecarga.setValue("ventarecargas");
        rootElement.setAttributeNode(attrRecarga);

        document.appendChild(rootElement);
        for (VRTTransaction vrtTransaction : vrtNumberControl.getVrtTransactions()) {

            if(vrtTransaction.getOperationType().equals(OperationType.SELL_OK_VRT)){
                //Nodo de venta
                Element venta = document.createElement("venta");
                rootElement.appendChild(venta);
                //Folio de transaccion
                Element folio = document.createElement("c1");
                folio.appendChild(document.createTextNode(vrtTransaction.getTransactionFoil()));
                venta.appendChild(folio);

                // Fecha de la transacción
                Element fecha = document.createElement("c2");
                fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(vrtTransaction.getTransactionDate())));
                venta.appendChild(fecha);

                //UID Tarjeta
                Element uidTarjeta = document.createElement("c3");
                uidTarjeta.appendChild(document.createTextNode(vrtTransaction.getSerialCard()));
                venta.appendChild(uidTarjeta);

                // Monto de la transacción
                Element monto = document.createElement("c4");
                String strMonto = String.valueOf(formatForCurrency.format(vrtTransaction.getTransactionAmmount()));
                monto.appendChild(document.createTextNode(strMonto));
                venta.appendChild(monto);

                // UID dispositivo
                Element uidDispositivo = document.createElement("c5");
                uidDispositivo.appendChild(document.createTextNode(vrtNumberControl.getDeviceIdFimpe()));
                venta.appendChild(uidDispositivo);

                Element perfil = document.createElement("c8");
                perfil.appendChild(document.createTextNode(vrtTransaction.getProfile()));
                venta.appendChild(perfil);

                Element listaProductos = document.createElement("c11");
                venta.appendChild(listaProductos);

                //Agrega los productos
                listaProductos.appendChild(createNodeProduct(vrtTransaction.getProductCreditId()));
                listaProductos.appendChild(createNodeProduct(vrtTransaction.getProductMoneyId()));

                // ID SAM
                Element idSAM = document.createElement("c17");
                idSAM.appendChild(document.createTextNode(vrtTransaction.getSamId()));
                venta.appendChild(idSAM);

            }else {
                //Crea el nodo de recarga
                Element recarga = document.createElement("recarga");
                rootElement.appendChild(recarga);
                //Folio de transaccion
                Element folio = document.createElement("c1");
                folio.appendChild(document.createTextNode(vrtTransaction.getTransactionFoil()));
                recarga.appendChild(folio);

                // Fecha de la transacción
                Element fecha = document.createElement("c2");
                recarga.appendChild(fecha);
                fecha.appendChild(document.createTextNode(DateHelper.getDateFormatFull(vrtTransaction.getTransactionDate())));
                recarga.appendChild(fecha);

                // UID Tarjeta
                Element uidTarjeta = document.createElement("c3");
                uidTarjeta.appendChild(document.createTextNode(vrtTransaction.getSerialCard()));
                recarga.appendChild(uidTarjeta);

                // Producto
                Element producto = document.createElement("c4");
                producto.appendChild(document.createTextNode(vrtTransaction.getProductId().getFimpeValue()));
                recarga.appendChild(producto);

                // Monto de la transacción
                Element monto = document.createElement("c5");
                String strMonto = String.valueOf(formatForCurrency.format(vrtTransaction.getTransactionAmmount()));
                monto.appendChild(document.createTextNode(strMonto));
                recarga.appendChild(monto);

                // Saldo inicial
                Element saldoInicial = document.createElement("c10");
                String strSaldoInicial
                        = String.valueOf(formatForCurrency.format(vrtTransaction.getInitialBalance()));

                saldoInicial.appendChild(document.createTextNode(strSaldoInicial));
                recarga.appendChild(saldoInicial);

                // Saldo final
                Element saldoFinal = document.createElement("c11");
                String strSaldoFinal
                        = String.valueOf(formatForCurrency.format(vrtTransaction.getFinalBalance()));
                saldoFinal.appendChild(document.createTextNode(strSaldoFinal));
                recarga.appendChild(saldoFinal);

                // UID dispositivo
                Element uidDispositivo = document.createElement("c12");
                uidDispositivo.appendChild(document.createTextNode(vrtNumberControl.getDeviceIdFimpe()));
                recarga.appendChild(uidDispositivo);

                // ID SAM
                Element idSAM = document.createElement("c13");
                idSAM.appendChild(document.createTextNode(vrtTransaction.getSamId()));
                recarga.appendChild(idSAM);

                // Conscutivo SAM
                Element consecutivoSAM = document.createElement("c14");
                consecutivoSAM.appendChild(document.createTextNode(vrtTransaction.getSamTransactionCounter())
                );
                recarga.appendChild(consecutivoSAM);

                // Número transacción en tarjeta
                Element transaccionTarjeta = document.createElement("c15");
                transaccionTarjeta.appendChild(document.createTextNode(String.valueOf(vrtTransaction.getCardTransactionCounter()))
                );
                recarga.appendChild(transaccionTarjeta);

                // Tipo recarga
                Element tipoDebito = document.createElement("c18");
                tipoDebito.appendChild(document.createTextNode(vrtTransaction.getRechargeType()));
                recarga.appendChild(tipoDebito);

                Element idEstacion = document.createElement("c21");
                idEstacion.appendChild(document.createTextNode(String.valueOf(vrtNumberControl.getStationId())));
                recarga.appendChild(idEstacion);
            }
        }

        Transformer transformer = transformerFactory.newTransformer();
        domSource = new DOMSource(document);
        File file = new File(Folder.XML.getPath() + fileDataName);
        streamResult = new StreamResult(file);
        transformer.transform(domSource, streamResult);
        return file.toPath();
    }

    private Path generateRechargeSaleNumberControl(VRTNumberControl vrtNumberControl, String fileNumberControlName)
            throws TransformerException, ParserConfigurationException {

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
        idColecta.appendChild(document.createTextNode(String.valueOf(vrtNumberControl.getCutId())));
        cifrasControl.appendChild(idColecta);

        // Fecha de colecta
        Element fechaColecta = document.createElement("c2");
        fechaColecta.appendChild(document.createTextNode(DateHelper.getDateFormatFull(vrtNumberControl.getCutDate())));
        cifrasControl.appendChild(fechaColecta);

        //Fecha inicial de corte
        Element fechaInicioCorte = document.createElement("c3");
        fechaInicioCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(vrtNumberControl.getInitialCutDate())));
        cifrasControl.appendChild(fechaInicioCorte);

        //Fecha final de corte
        Element fechaFinalCorte = document.createElement("c4");
        fechaFinalCorte.appendChild(document.createTextNode(DateHelper.getDateFormatFull(vrtNumberControl.getFinalCutDate())));
        cifrasControl.appendChild(fechaFinalCorte);

        //Identificador del operador
        Element idOperador = document.createElement("c5");
        idOperador.appendChild(document.createTextNode(vrtNumberControl.getEurId()));
        cifrasControl.appendChild(idOperador);

        //Identificador de ruta
        Element idRuta = document.createElement("c6");
        idRuta.appendChild(document.createTextNode(vrtNumberControl.getRouteId()));
        cifrasControl.appendChild(idRuta);

        //Numero total de registros
        Element registrosTotales = document.createElement("c7");
        registrosTotales.appendChild(document.createTextNode(String.valueOf(vrtNumberControl.getTotalRecords())));
        cifrasControl.appendChild(registrosTotales);

        //Monto total de registros
        String strMonto = String.valueOf(formatForCurrency.format(vrtNumberControl.getTotalAmmountRecords()));
        Element montoRegistrosTotales = document.createElement("c8");
        montoRegistrosTotales.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotales);

        //Total de registros venta
        Element registrosTotalesVenta = document.createElement("c9");
        registrosTotalesVenta.appendChild(document.createTextNode(String.valueOf(vrtNumberControl.getTotalCardSales())));
        cifrasControl.appendChild(registrosTotalesVenta);

        //Monto total de registros venta
        strMonto = String.valueOf(formatForCurrency.format(vrtNumberControl.getTotalAmmountCardSales()));
        Element montoRegistrosTotalesVenta = document.createElement("c10");
        montoRegistrosTotalesVenta.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotalesVenta);

        //Numero total de registros de recarga
        Element registrosTotalesRecarga = document.createElement("c11");
        registrosTotalesRecarga.appendChild(document.createTextNode(String.valueOf(vrtNumberControl.getTotalRecharges())));
        cifrasControl.appendChild(registrosTotalesRecarga);

        //Monto total de registros de recargas
        strMonto = String.valueOf(formatForCurrency.format(vrtNumberControl.getTotalAmmountRecharges()));
        Element montoRegistrosTotalesRecargas = document.createElement("c12");
        montoRegistrosTotalesRecargas.appendChild(document.createTextNode(strMonto));
        cifrasControl.appendChild(montoRegistrosTotalesRecargas);

        Element totalViajes = document.createElement("c13");
        totalViajes.appendChild(document.createTextNode(String.valueOf(vrtNumberControl.getTotalBPDs())));
        cifrasControl.appendChild(totalViajes);

        Element idProveedorTecnologico = document.createElement("c14");
        idProveedorTecnologico.appendChild(document.createTextNode(vrtNumberControl.getTechnologicProviderId()));
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

    private Node createNodeProduct(ProductSale productSale){
        Element producto = document.createElement("producto");

        Element productoId = document.createElement("c12");
        productoId.appendChild(document.createTextNode(
                productSale.getProductId().getFimpeValue()));
        producto.appendChild(productoId);

        Element fechaInicialProducto = document.createElement("c13");
        fechaInicialProducto.appendChild(document.createTextNode(
                productSale.getStartProductValidity().format(dateFormatter)));
        producto.appendChild(fechaInicialProducto);

        Element fechaFinalProducto = document.createElement("c14");
        fechaFinalProducto.appendChild(document.createTextNode(
                productSale.getEndProductValidity().format(dateFormatter)));
        producto.appendChild(fechaFinalProducto);

        Element horaInicialProducto = document.createElement("c15");
        horaInicialProducto.appendChild(document.createTextNode(
                productSale.getStartProductValidityDuringDay().format(timeFormatter)));
        producto.appendChild(horaInicialProducto);

        Element horaFinalProducto = document.createElement("c16");
        horaFinalProducto.appendChild(document.createTextNode(
                productSale.getEndProductValidityDuringDay().format(timeFormatter)));
        producto.appendChild(horaFinalProducto);

        return producto;
    }
    @Override
    public Path getNumberControlFile() {
        return fileNumberControl;
    }

    @Override
    public Path getDataFile() {
        return fileData;
    }

    private String generateFileName(VRTNumberControl vrtNumberControl, PrefixFile prefixFile, ExtentionFile extentionFile) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(prefixFile.getPrefix()).append("_")
                .append(String.valueOf(vrtNumberControl.getCutId())).append("_")
                .append(DateHelper.getDateFormatFull(vrtNumberControl.getCutDate())).append("_")
                .append(vrtNumberControl.getEurId()).append("_")
                .append(vrtNumberControl.getRouteId())
                .append(extentionFile.getExtention());
        return fileName.toString();
    }
}
