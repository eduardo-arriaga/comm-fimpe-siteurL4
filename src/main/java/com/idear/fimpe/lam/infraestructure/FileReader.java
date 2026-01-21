package com.idear.fimpe.lam.infraestructure;

import com.idear.fimpe.lam.domain.LAMTransaction;
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

    public static List<LAMTransaction> getLAMRequestFromFile(Path file){
        List<LAMTransaction> lamTransactions = new ArrayList<>();
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(file.toFile().getPath()));
            document.getDocumentElement().normalize();
            NodeList requestNodes = document.getElementsByTagName("tarjeta");
            for (int i = 0; i < requestNodes.getLength(); i++) {
                Element cardNode = (Element) requestNodes.item(i);
                String serialCard = cardNode.getElementsByTagName("c1").item(0).getTextContent();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime registerLocalDateTime = LocalDateTime.parse(
                        cardNode.getElementsByTagName("c2").item(0).getTextContent(),
                        dateTimeFormatter);
                Long cardTransactionCounter = Long.valueOf(cardNode.getElementsByTagName("c3").item(0).getTextContent());
                Integer action = Integer.valueOf(cardNode.getElementsByTagName("c4").item(0).getTextContent());

                LAMTransaction lamTransaction = new LAMTransaction(serialCard, registerLocalDateTime, cardTransactionCounter, action);
                lamTransactions.add(lamTransaction);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            logger.error("Error al intentar leer el contenido del archivo LAM", e);
        }
        return  lamTransactions;
    }
}
