package com.idear.fimpe.fimpetransport;

import com.idear.fimpe.helpers.files.FileManager;
import com.idear.fimpe.helpers.files.FileManagerException;
import com.idear.fimpe.properties.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;

import static com.idear.fimpe.enums.Folder.*;

public class FimpeCommand {

    private Path fileCC, fileDAT;
    private String routeId;
    private Logger logger = LoggerFactory.getLogger(FimpeCommand.class);
    private final String TOOL_PATH = "transporte.jar";
    private final String KEY_PUBLIC_FILE = KEYPAIR.getPath() + PropertiesHelper.KEY_PUBLIC_FILE_NAME;
    private final String KEY_PRIVATE_FILE = KEYPAIR.getPath() + "key.private";
    private final String URL_FIMPE_UPLOAD = "https://cam.fimpe.org/transporte/upload";
    private final String URL_FIMPE_DOWNLOAD = "https://cam.fimpe.org/transporte/download";

    /**
     * Establece el nombre del archivo .CC
     *
     * @param fileCC Nombre del archivo a establecer
     */
    public void setFileCC(Path fileCC) {
        this.fileCC = fileCC;
    }

    /**
     * Establece el nombre del archivo .DAT
     *
     * @param fileDAT Nombre del archivo a establecer
     */
    public void setFileDAT(Path fileDAT) {
        this.fileDAT = fileDAT;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /**
     * Encripta un archivo .DAT
     *
     * @throws FimpeException Si no genera la llave tira una excepcion
     */
    private String encryptFile() throws FimpeException {
        ArrayList<String> response;
        response = CommandLine.execCommand("java -jar " + TOOL_PATH + " encrypt " + KEY_PUBLIC_FILE);
        if (response.size() > 0 && existsMessageOnResponse(response, "Llave de sesion para", 1)) {
            return getKeyGenerated(response.get(response.size() - 1));//toma la ultima linea de la respuesta
        } else {
            try {
                //Elimina cualquier rastro del par de archivos que no se procesaron con exito
                FileManager.deleteFile(fileDAT);
                FileManager.deleteFile(fileCC);
                throw new FimpeException("Falla al intentar encriptar");
            } catch (FileManagerException ex) {
                logger.error("Los archivos que se intentaron borrar ya no existen", ex);
                throw new FimpeException("Falla al intentar encriptar");
            }
        }
    }

    public void descryptFile() throws FimpeException {
        ArrayList<String> response = CommandLine.execCommand("java -jar " + TOOL_PATH + " decrypt " + KEY_PRIVATE_FILE);
        if (response.size() > 0) {
            if (!existsMessageOnResponse(response, "Descifrando archivo", 1)) {
                throw new FimpeException("Archivo LAM no decifrado");
            }
        }
    }

    /**
     * Extrae la clave generada de una linea de texto
     *
     * @param line La linea que contiene la clave generada
     * @return La clave generada
     */
    private String getKeyGenerated(String line) {
        String response = null;
        int start = line.lastIndexOf("Llave:") + 6;
        response = line.substring(start, line.length());
        return response;
    }

    /**
     * Sube los archivos al servidor de FIMPE para su validación
     *
     * @throws FimpeException Si falla el envio de por lo menos un archivo,
     *                        entonces lanza la excepcion
     */
    private void uploadFilesCommand() throws FimpeException {

        ArrayList<String> response = CommandLine.execCommand(
                "java -jar " + TOOL_PATH + " upload " + PropertiesHelper.KEY_PRIVATE + " " + URL_FIMPE_UPLOAD);

        if (response.size() > 0) {
            if (!existsMessageOnResponse(response, "enviado correctamente", 2)) {
                try {
                    //Mover archivos corruptos a la carpeta de corruptos
                    FileManager.deleteFile(fileCC);
                    FileManager.deleteFile(fileDAT);
                    FileManager.deleteFolderContent(INPUT.getPath());
                    throw new FimpeException("Falla al intentar enviar");
                } catch (FileManagerException ex) {
                    logger.error("Los archivos que se intentaron borrar ya no existen", ex);
                    throw new FimpeException("Falla al intentar enviar");
                }
            }
        } else {
            throw new FimpeException("Falla al intentar enviar");
        }
    }

    public void downloandFimpeResponses() {
        //ArrayList<String> response = CommandLine.execCommand("java -jar " + TOOL_PATH + " download " + FileProperties.KEY_PRIVATE + " " + URL_FIMPE_DOWNLOAD);
        ArrayList<String> response = CommandLine.execCommand("descarga.bat");
        for (String responseElement : response) {
            logger.debug(responseElement);
        }
    }

    /**
     * Ejecuta el proceso completo de Fimpe, desde mover archivos, encriptar,
     * extraer la llave generada en el archivo de control de cifras, borrar y
     * subir archivos
     */
    public void uploadFiles() throws FimpeException, FileManagerException {
//        try {
//            FileManager.deleteFolderContent(INPUT.getPath());
//            FileManager.deleteFolderContent(OUTPUT.getPath());
//            FileManager.deleteFolderContent(UPLOAD.getPath());
//            //Mueve e archivo .DAT de /xml/ a /cifrado/input
//            fileDAT = FileManager.moveFile(fileDAT, INPUT.getPath());
//            //Encripta el archivo .DAT que se encuentre en /cifrado/input
//            //y extrae la clave generada en la ultima linea de la respuesta de encriptacion
//            String keyGenerated = encryptFile();
//            //Inserta la clave generada al archvio .CC que esta en /xml/
//            FileManager.insertKeyGeneratedToDocument(keyGenerated, fileCC);
//            //Mueve el archivo .DAT de /cifrado/output a /upload/
//            fileDAT = FileManager.moveFile(OUTPUT.getPath(), UPLOAD.getPath());
//            //Mueve el archivo .CC de /xml/ a /upload/
//            fileCC = FileManager.moveFile(fileCC, UPLOAD.getPath());
//            logger.info("Ejecutando envio a FIMPE ");
//            //Sube los archivos que se encuentran en /upload/
//            uploadFilesCommand();
//            logger.info("Fin de de envio a FIMPE ");
//            //Mueve el archivo .DAT de cifrado/input sin cifrar a la carpeta dat_files
//            FileManager.moveFile(INPUT.getPath(), FileManager.createFolder(routeId, DAT_FILES.getPath()));
//            //Mueve los archivos procesados
//            FileManager.moveFile(fileDAT, FileManager.createFolder(routeId, SENT_FILES.getPath()));
//            //Mueve los archivos procesados
//            FileManager.moveFile(fileCC, FileManager.createFolder(routeId, SENT_FILES.getPath()));
//        } catch (FimpeException | FileManagerException ex) {
//            FileManager.deleteFolderContent(XML.getPath());
//            throw ex;
//        }
    }
    /**
     * Revisa si en una lista de respuestas existe el emensaje requerido
     *
     * @param response Lista de respuestas
     * @param message  Mensaje a buscar
     * @return Si el mensaje existe en la lista
     */
    private boolean existsMessageOnResponse(ArrayList<String> response, String message, int times) {
        int auxTimes = 0;
        for (String responseRow : response) {
            if (responseRow.contains(message)) {
                auxTimes++;
                if (auxTimes == times)
                    return true;
            }
        }
        return false;
    }
}
