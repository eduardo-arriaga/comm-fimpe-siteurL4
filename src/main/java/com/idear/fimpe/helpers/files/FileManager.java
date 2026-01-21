package com.idear.fimpe.helpers.files;

import com.idear.fimpe.enums.Folder;
import net.lingala.zip4j.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileManager {

    private static Logger logger = LoggerFactory.getLogger(FileManager.class);
    private FileManager(){}
    /**
     * Mueve un archivo a la ruta especificada
     * @param file Archivo a mover
     * @param to Ruta especificada
     * @throws FileManagerException  Si el archivo a mover no se encuentra o si fallo al intentar mover el archivo
     */
    public static Path moveFile(Path file, String to)throws FileManagerException{
        try {
            Path target = Paths.get(to + file.getFileName().toString());
            Path result = Files.move(file,target, StandardCopyOption.REPLACE_EXISTING);
            if(result.equals(target)){
                return result;
            }else{
                throw new FileManagerException("El archivo no pudo ser movido");
            }
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("El archivo especificado no se encuentra");
        }
    }

    /**
     * Mueve un archivo a la ruta especificada
     * @param from Archivo a mover
     * @param to Ruta especificada
     * @throws FileManagerException  Si el archivo a mover no se encuentra o si fallo al intentar mover el archivo
     */
    public static Path moveFile(String from, String to)throws FileManagerException{
        try {
            Path folderFrom = Paths.get(from);
            List<Path> fileList = Files.walk(folderFrom)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.<Path>toList());

            if(fileList.size() > 1){
                throw new FileManagerException("La carpeta " + from + "contiene mas de un archivo");
            }else if(fileList.size() == 1){
                return moveFile(fileList.get(0), to);
            }else{
                throw new FileManagerException("La carpeta " + from + " no contiene archivos");
            }
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("La carpeta especificada no se encuentra");
        }
    }

    /**
     * Elimina el archivo especificado
     * @param file Archivo a eliminar
     * @throws FileManagerException Si el archivo a mover no se encuentra o si fallo al intentar eliminar el archivo
     */
    public static void deleteFile(Path file) throws FileManagerException{
        try {
            if(!Files.deleteIfExists(file)){
                throw new FileManagerException("El archivo no pudo ser eliminado");
            }
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("El archivo especificado no se encuentra");
        }
    }

    public static void deleteFolderContent(String folder)throws FileManagerException{
        try {
            Path folderFrom = Paths.get(folder);
            List<Path> filesList = Files.walk(folderFrom)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.<Path>toList());
            for (Path file : filesList) {
                Files.delete(file);
            }
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("La carpeta especificada no se encuentra");
        }
    }

    /**
     * Obtiene un documento con formato xml, busca, encuentra y reemplaza el contenido del
     * nodo llaveAES por la clave @key y posterieormente guarda el documento con los cambios
     * @param key Cadena a reemplazar en el documento
     * @param file Archivo al que se le insertara la llave
     */
    public static void insertKeyGeneratedToDocument(String key, Path file) throws FileManagerException {

        try {
            //Carga el archivo
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(Folder.XML.getPath() + file.getFileName().toString()));
            XPath xpath = XPathFactory.newInstance().newXPath();
            //Busca y reemplaza el valor del nodo
            NodeList nodes = (NodeList) xpath.evaluate("/archivo/llaveAES", document, XPathConstants.NODESET);
            nodes.item(0).setTextContent(key);
            //Guarda los cambios
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(new File(file.toAbsolutePath().toString())));

        } catch (TransformerException | XPathExpressionException | SAXException | ParserConfigurationException ex) {
            logger.error("",ex);
            throw new FileManagerException("Error al intentar insertar la llave en el archivo");
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("El archivo especificado no se encuentra");
        }
    }

    /**
     * Crea una carpeta en una carpeta contenedora
     * @param folder Nombre de la nueva carpeta
     * @param folderContainer Carpeta padre o contenedora
     * @return  Ruta de la nueva carpeta especificada
     * @throws FileManagerException Cuando la carpeta padre no existe
     */
    public static String createFolder(String folder, String folderContainer) throws FileManagerException {
        try {
            //Crea la carpeta de corredor si no existe
            Path newFolder = Paths.get(folderContainer + folder);
            Files.createDirectory(newFolder);
            //retorna la carpeta creada
            return newFolder.toString() + File.separator;
        } catch (FileAlreadyExistsException ex){
            return Paths.get(folderContainer + folder).toString() + File.separator;
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("Error al intentar crear la carpeta, la carpeta padre no existe");
        }
    }

    public static long getTotalFilesOnFolder(String folderPath)throws FileManagerException{
        try {
            Path folder = Paths.get(folderPath);
            return Files.walk(folder)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("Error al intentar consultar los archivos");
        }
    }

    public static List<Path> listRegularFiles(String folderPath)throws FileManagerException{
        try {
            return Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.<Path>toList());
        } catch (IOException ex) {
            logger.error("",ex);
            throw new FileManagerException("Error al intentar obtener los archivos de la ruta " + folderPath);
        }
    }

    /**
     * Obtiene la firma del archivo, que se encuentra en el nodo c3 del acuse
     *
     * @param file Archivo que contiene la firma
     * @return La firma extraida
     */
    public static String getOkSign(Path file) throws FileManagerException {
        try {
            String sign = null;
            //Carga el documento
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(file.toFile().getAbsolutePath()));
            document.getDocumentElement().normalize();
            NodeList llaveAesElement = document.getElementsByTagName("c3");
            sign = llaveAesElement.item(0).getTextContent();
            return sign;
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            logger.error("",ex);
            throw new FileManagerException("Error al intentar extraer la firma del acuse " + file.getFileName());
        }
    }

    public static List<Path> getRegularFilesCreatedToday(String folder){
        try {
            Path folderPath = Paths.get(folder);
            return Files.walk(folderPath)
                    .filter(wasCreatedToday())
                    .filter(Files::isRegularFile)
                    .collect(Collectors.<Path>toList());
        } catch (IOException ex) {
            logger.error("",ex);
        }
        return new ArrayList<>();
    }

    public static Predicate<Path> wasCreatedToday() {
        return new Predicate<Path>() {
            @Override
            public boolean test(Path t) {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(t, BasicFileAttributes.class);
                    FileTime fileTime = attributes.lastModifiedTime();
                    LocalDate now = LocalDate.now();
                    LocalDate pathDate = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault()).toLocalDate();
                    return pathDate.isEqual(now);
                } catch (IOException ex) {
                    logger.error("",ex);
                }
                return false;
            }
        };
    }

    public static boolean folderContainsFiles(String folder, int numberFiles) throws FileManagerException{
        if(FileManager.listRegularFiles(folder).size() == numberFiles){
            return true;
        }else{
            throw new FileManagerException("La carpeta no cuenta con los archivos esperados");
        }
    }

    /**
     * Obtiene el nombre del archivo por medio de la carpeta idCorredor y el folio de corte
     * @param idRoute
     * @param cutFoil
     * @return
     */
    public static String getFileNameSent(String idRoute, long cutFoil) {
        try {
            return Files.walk(Paths.get(Folder.SENT_FILES.getPath() + idRoute))
                    .filter(file -> file.toString().contains(String.valueOf(cutFoil)))
                    .findAny()
                    .get().getFileName().toString();
        } catch (IOException | NoSuchElementException ex) {
            logger.error("",ex);
        }
        return "Archivo no encontrado";
    }

    public static void compressFileToRar(String filetoCompress, String fileDestinyCompress) throws IOException {
        new ZipFile(fileDestinyCompress).addFile(filetoCompress);
    }
}
