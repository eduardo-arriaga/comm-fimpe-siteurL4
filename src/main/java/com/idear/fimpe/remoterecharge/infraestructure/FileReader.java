package com.idear.fimpe.remoterecharge.infraestructure;

import com.idear.fimpe.remoterecharge.domain.RemoteRechargeTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    private static Logger logger = LoggerFactory.getLogger(FileReader.class);

    public static List<RemoteRechargeTransaction> getRemoteRechargeRequestsFromFile(Path file){
        List<RemoteRechargeTransaction> remoteRechargeTransactions = new ArrayList<>();
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(file.toFile().getPath()));
            document.getDocumentElement().normalize();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            NodeList requestNodes = document.getElementsByTagName("recarga");

            for (int i = 0; i < requestNodes.getLength(); i++) {

                Element cardNode = (Element) requestNodes.item(i);

                String serialCard = cardNode.getElementsByTagName("c1").item(0).getTextContent();

                LocalDateTime registerLocalDateTime = LocalDateTime.parse(
                        cardNode.getElementsByTagName("c2").item(0).getTextContent(),
                        dateTimeFormatter);

                Integer numberActionAppliedToProduct =
                        Integer.valueOf(cardNode.getElementsByTagName("c3").item(0).getTextContent());

                Float amount = Float.valueOf(cardNode.getElementsByTagName("c4").item(0).getTextContent());

                String productId = cardNode.getElementsByTagName("c5").item(0).getTextContent();

                RemoteRechargeTransaction remoteRechargeTransaction =
                        new RemoteRechargeTransaction(
                                serialCard, registerLocalDateTime,
                                numberActionAppliedToProduct, amount,
                                productId, "001");

                remoteRechargeTransactions.add(remoteRechargeTransaction);
            }
        } catch (SAXException | IOException | ParserConfigurationException | NullPointerException e) {
            logger.error("Error al intentar leer el contenido del archivo RECARGAS REMOTAS", e);
        }
        return  remoteRechargeTransactions;
    }
}
