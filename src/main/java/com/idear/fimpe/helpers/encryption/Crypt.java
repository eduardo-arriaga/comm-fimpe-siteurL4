/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idear.fimpe.helpers.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author rperez
 */
public abstract class Crypt {
    // SHA1 especial
    protected static final byte[] hashSHA1 = {
        (byte)0x41, (byte)0xa6, (byte)0x0, (byte)0xd0, (byte)0xc5, (byte)0xbc, (byte)0x7,
        (byte)0x34, (byte)0xd1, (byte)0x41, (byte)0xe8, (byte)0x11, (byte)0x52, (byte)0xab,
        (byte)0x92, (byte)0x7a, (byte)0xbf, (byte)0x76, (byte)0x9, (byte)0x80
    };

    /**
     * Encripta un cadena de texto utilizando la key recibida como parámetro y retorna la cadena encriptada. 
     * @param source
     * @param key Clave utilizada por el algoritmo de encriptación.
     * @return Retorna la cadena encriptada.
     */
    public String encryptString(String source, String key) 
    {
        return new String(encrypt(source, key));
    }

    /**
     * Enctipta una cadena de texto utilizando una key predefinida y retorna la cadena encriptada.
     * @param source Cadena de texto a encriptar.
     * @return Retorna la cadena encriptada. 
     */
    public String encryptString(String source)
    {
        return new String(encrypt(source));
    }

    /**
     * Encripta una cadena de texto utilizando la key recibida como parámetro y regresa un arreglo de bytes con la cadena encriptada.
     * @param source Cadena de texto a encriptar.
     * @param key Clave utilizada por el algoritmo de encriptación. 
     * @return Retorna un arreglo con la cadena de texto encriptada. 
     */
    public abstract byte[] encrypt(String source, String key);

    /**
     * Encripta una cadena de texto utilizando una key predefinida y retorna un arreglo de bytes con la cadena encriptada. 
     * @param source Cadena de texto a enctiptar. 
     * @return Retorna un arreglo con la cadena de texto encriptada. 
     */
    public abstract byte[] encrypt(String source);
    
    /**
     * Encripta el contendido de un arreglo de bytes utilizando una key predefinida y retorna un arreglo de bytes con la información encriptada.
     * @param source Arreglo de bytes con la información a encriptar.
     * @return Retorna un arreglo de bytes con la información encriptada.
     */
    public abstract byte[] encrypt(byte[] source);
    
    /**
     * Encripta el contendido de un arreglo de bytes utilizando la key recibida como parámetro y retorna un arreglo de bytes con la información encriptada.
     * @param source Arreglo de bytes con la información a encriptar. 
     * @param key Clave utilizada por el algoritmo de encriptación. 
     * @return Retorna un arreglo de bytes con la información encriptada. 
     */
    public abstract byte[] encrypt(byte[] source, String key);

    /**
     * Desencripta una cadena utilizando la key recibida como parámetro y retorna la cadena de texto desencriptada.
     * @param source Cadena de texto a desencriptar.
     * @param key Clave utilizada por el algoritmo para desenctiptar.
     * @return Retorna la cadena de texto desencriptada.
     */
    public String decryptString(String source, String key)
    {
        return new String(decrypt(source, key));
    }

    /**
     * Desencripta una cadena utilizando una key predefinida y retorna la cadena de texto desencriptada.
     * @param source Cadena a desencriptar.
     * @return Retorna la cadena de texto desencriptada. 
     */
    public String decryptString(String source) 
    {
        return new String(decrypt(source));
    }

    /**
     * Desencripta una cadena utilizando la key recibida como parámetro y retorna un arreglo de bytes con la cadena de texto desencriptada.
     * @param source Cadena a desencriptar.
     * @param key Clave utilizada por el algoritmo para desencriptar.
     * @return Retorna un arreglo de bytes con la cadena de texto desencriptada.
     */
    public abstract byte[] decrypt(String source, String key);
 
    /**
     * Desencripta una cadena utilizando una key predefinida y retorna un arreglo de bytes con la cadena de texto desencriptada.
     * @param source Cadena a desencriptar.
     * @return Retorna un arreglo de bytes con la cadena de texto desencriptada.
     */
    public abstract byte[] decrypt(String source);
  
    /**
     * Desencripta la información de un arreglo de bytes utilizando la key recibida como parámetro y retorna un arreglo de bytes con la información desencriptada.
     * @param source Arreglo de bytes con la información a desencriptar. 
     * @param key Clave utilizada por el algoritmo para desencriptar.
     * @return Retorna un arreglo de bytes con la información desencriptada.
     */
    public abstract byte[] decrypt(byte[] source, String key);
    
    /**
     * Desencripta la información de un arreglo de bytes y retorna un arreglo de bytes con la información desencriptada.
     * @param source Arreglo de bytes con la información a desencriptar.
     * @return Retorna un arreglo de bytes con la información desencriptada. 
     */
    public abstract byte[] decrypt(byte[] source);        
    
    public byte[] encryptFile(String fileName) throws IOException {
        return encryptFile(new File(fileName));
    }
    
    public byte[] encryptFile(String fileName, String key) throws IOException {
        return encryptFile(new File(fileName), key);
    }
    
    public byte[] encryptFile(File file) throws IOException {
        if (file == null || !file.exists() || file.length() > Integer.MAX_VALUE)
            throw new IOException(String.format("Error reading file: %s", file != null ? file.getName() : "NULL"));
        byte[] buffer = new byte[(int)file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(buffer, 0, buffer.length);
        return encrypt(buffer);
    }
    
    public byte[] encryptFile(File file, String key) throws IOException {
        if (file == null || !file.exists() || file.length() > Integer.MAX_VALUE)
            throw new IOException(String.format("Error reading file: %s", file != null ? file.getName() : "NULL"));
        byte[] buffer = new byte[(int)file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(buffer, 0, buffer.length);
        return encrypt(buffer, key);
    }

    public byte[] decryptFile(String fileName) throws IOException {
        return decryptFile(new File(fileName));
    }
        
    public byte[] decryptFile(String fileName, String key) throws IOException {
        return decryptFile(new File(fileName), key);
    }

    public byte[] decryptFile(File file) throws IOException {
        if (file == null || !file.exists() || file.length() > Integer.MAX_VALUE)
            throw new IOException(String.format("Error reading file: %s", file != null ? file.getName() : "NULL"));
        byte[] buffer = new byte[(int)file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(buffer, 0, buffer.length);
        return decrypt(buffer);
    }
    
    public byte[] decryptFile(File file, String key) throws IOException {
        if (file == null || !file.exists() || file.length() > Integer.MAX_VALUE)
            throw new IOException(String.format("Error reading file: %s", file != null ? file.getName() : "NULL"));
        byte[] buffer = new byte[(int)file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(buffer, 0, buffer.length);
        return decrypt(buffer, key);
    }
}
