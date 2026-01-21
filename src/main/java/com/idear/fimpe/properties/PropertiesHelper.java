package com.idear.fimpe.properties;

import com.idear.fimpe.helpers.encryption.RC4;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesHelper {
    public static String VERSION = "1.0";
    public static String PROJECT_NAME = "L4";
    public static String DB_HOST;
    public static String DB_NAME;
    public static String DB_USER;
    public static String DB_PASSWORD;

    public static String DB_HOST_LP;
    public static String DB_NAME_LP;
    public static String DB_USER_LP;
    public static String DB_PASSWORD_LP;
    public static String MAX_LOG_FILES;
    public static String MAX_SIZE_FILE_LOG;
    public static String TECHNOLOGIC_PROVIDER_ID;
    public static String KEY_PRIVATE;
    public static String KEY_PUBLIC_FILE_NAME;
    public static int DOWNLOAD_ATTEMPTS;
    public static int MAX_TRANSACTIONS_PER_FILE = 1000;
    public static int WAIT_TIME_TO_DOWNLOAD;

    public static void loadProperties(String configPathFile) throws IOException {
        Properties properties = new Properties();
        //Abrir el recurso y cerrarlo despues de usado
        try (FileInputStream fileInputStream = new FileInputStream(configPathFile)) {
            properties.load(fileInputStream);
        }
        //Desencriptar las propiedades
        RC4 decrypter = new RC4();
        DB_HOST = decrypter.decryptString(properties.getProperty(PropertiesApp.DB_HOST.name()).trim());
        DB_NAME = decrypter.decryptString(properties.getProperty(PropertiesApp.DB_NAME.name()).trim());
        DB_USER = decrypter.decryptString(properties.getProperty(PropertiesApp.DB_USER.name()).trim());
        DB_PASSWORD = decrypter.decryptString(properties.getProperty(PropertiesApp.DB_PASSWORD.name()).trim());

        KEY_PRIVATE = decrypter.decryptString(properties.getProperty(PropertiesApp.KEY_PRIVATE.name()).trim());
        KEY_PUBLIC_FILE_NAME = decrypter.decryptString(properties.getProperty(PropertiesApp.KEY_PUBLIC_FILE_NAME.name()).trim());

        MAX_LOG_FILES = properties.getProperty(PropertiesApp.MAX_LOG_FILES.name(), "10").trim();
        MAX_SIZE_FILE_LOG = properties.getProperty(PropertiesApp.MAX_SIZE_FILE_LOG.name(), "10MB").trim();
        TECHNOLOGIC_PROVIDER_ID = properties.getProperty(PropertiesApp.TECHNOLOGIC_PROVIDER_ID.name()).trim();
        DOWNLOAD_ATTEMPTS = Integer.parseInt(properties.getProperty(PropertiesApp.DOWNLOAD_ATTEMPTS.name(), "10").trim());
        WAIT_TIME_TO_DOWNLOAD = Integer.parseInt(properties.getProperty(PropertiesApp.WAIT_TIME_TO_DOWNLOAD.name(), "2"));
    }

    //Nombre de las propiedades en el archivo
    enum PropertiesApp {
        DB_HOST,
        DB_NAME,
        DB_USER,
        DB_PASSWORD,
        KEY_PRIVATE,
        KEY_PUBLIC_FILE_NAME,
        MAX_LOG_FILES,
        MAX_SIZE_FILE_LOG,
        TECHNOLOGIC_PROVIDER_ID,
        DOWNLOAD_ATTEMPTS,
        WAIT_TIME_TO_DOWNLOAD;
    }
}
