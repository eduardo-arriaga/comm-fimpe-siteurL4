package com.idear.fimpe.helpers.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Mail {

    public final String MAIL_USER = "fimpetweet@bea.com.mx";
    public final String MAIL_PASSWORD = "A01DoidiwBIN";
    public final String MAIL_ADDRESSES_PATH = "addresses.txt";
    public final String MAIL_SERVER_DOMAIN = "mail.bea.com.mx";
    public final String MAIL_PORT = "26";

    private Logger logger = LoggerFactory.getLogger(Mail.class);

    /**
     * Envia un correo con un archivo adjunto
     *
     * @param subject Asunto del mensaje
     * @param body    Cuerpo del mensaje
     */
    public void sendMail(String subject, String body) {
        //Inicializa las propiedades del servidor del que se enviaran los datos
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", MAIL_SERVER_DOMAIN);
        properties.put("mail.smtp.port", MAIL_PORT);

        //Prepara una sesion con un usuario y constraseña validos
        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MAIL_USER, MAIL_PASSWORD);
                    }
                });

        try {
            //Prepara el mensaje con la sesion, remitente, destinatario,
            //asunto y cuerpo del mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MAIL_USER));
            //Agrega a los multiples destinos
            message.setRecipients(Message.RecipientType.TO, getAddresses(MAIL_ADDRESSES_PATH));
            message.setSubject(subject);
            message.setText(body);

            //Envia el mensaje
            Transport.send(message);
           logger.info("Correo enviado " + subject);
        } catch (MessagingException ex) {
            logger.info("Error al intentar enviar el correo con el mensaje " + subject, ex);
        }
    }

    /**
     * Envia un correo con un archivo adjunto
     *
     * @param subject  Asunto del mensaje
     * @param filePath Ruta de archivo
     * @param fileName Nombre de archivo
     */
    public void sendMail(String subject, String filePath, String fileName) {
        //Inicializa las propiedades del servidor del que se enviaran los datos
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", MAIL_SERVER_DOMAIN);
        properties.put("mail.smtp.port", MAIL_PORT);

        //Prepara una sesion con un usuario y constraseña validos
        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MAIL_USER, MAIL_PASSWORD);
                    }
                });

        try {
            //Prepara el mensaje con la sesion, remitente, destinatario,
            //asunto y cuerpo del mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MAIL_USER, true));
            //Agrega a los multiples destinos
            message.setRecipients(Message.RecipientType.TO, getAddresses(MAIL_ADDRESSES_PATH));
            message.setSubject(subject);
            //Adjunta el archivo
            Multipart multipart = atachFile(filePath, fileName);
            message.setContent(multipart);
            //Envia el mensaje
            Transport.send(message);
            logger.info("Correo enviado " + subject);
        } catch (MessagingException ex) {
            logger.error("Error al intentar enviar correo con el mensaje " + subject, ex);
        }
    }

    /**
     * Obtiene las lineas de un archivo dado
     *
     * @param path Ruta de archivo
     * @return Lineas del archivo
     */
    private InternetAddress[] getAddresses(String path) {
        ArrayList<InternetAddress> addresses = new ArrayList<>();
        try {
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String address;
            while ((address = bufferedReader.readLine()) != null) {
                addresses.add(new InternetAddress(address));
            }
            fileReader.close();
            bufferedReader.close();

        } catch (AddressException ex) {
            logger.info("La direccion de correo no es valida");
        } catch (IOException ex) {
            logger.info("Lista de direcciones de correo no encontrada");
        }
        return addresses.toArray(new InternetAddress[addresses.size()]);
    }

    /**
     * Prepara un archivo para adjuntarlo a un mensaje
     *
     * @param filePath Ruta del archivo
     * @param fileName Nombre del archivo
     * @return Un Objeto preparado para ser adjuntado al mensaje
     * @throws MessagingException
     */
    private Multipart atachFile(String filePath, String fileName) throws MessagingException {
        //Prepara y adjunta el archivo adjunto
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        Multipart multipart = new MimeMultipart();
        DataSource dataSource = new FileDataSource(filePath);
        mimeBodyPart.setDataHandler(new DataHandler(dataSource));
        mimeBodyPart.setFileName(fileName);
        multipart.addBodyPart(mimeBodyPart);
        return multipart;
    }
}
