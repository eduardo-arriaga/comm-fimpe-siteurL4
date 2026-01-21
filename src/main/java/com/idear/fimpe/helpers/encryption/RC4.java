/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idear.fimpe.helpers.encryption;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author rperez
 */
public class RC4 extends Crypt{
    public RC4() { }

    /*
     * Funciones de Encriptación.
     */
    
    /**
     * Encripta el contendido de un arreglo de bytes con el algoritmo RC4, utilizando la key recibida como parámetro y retorna un arreglo de bytes con la información encriptada.
     * @param source Arreglo con la información a encriptar.
     * @param key Clave utilizada por el algoritmo de encriptación. 
     * @return arSource Arreglo de bytes con la información encriptada. 
     */
    @Override
    public byte[] encrypt(byte[] source, String key)
    {
        try {
            // Se toma el Hash SHA1 que se genera con Clave y se asigna a un arreglo de bytes.
            byte[] arHashSHA1 = Conversions.getHashSHA1(key);

            byte[] arSource = rc4(source, arHashSHA1);

            // El arrego de bytes se encripta en base64 y se retorna como resultado de la función.

            //return new BASE64Encoder().encode(arSource).getBytes("UTF-8");
            return arSource;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            return null;
        }
    }

    // Encriptación especial para uso interno.
    /**
     * Encripta el contendido de un arreglo de bytes con el algoritmo RC4, utilizando una key predefinida y retorna un arreglo de bytes con la información encriptada.
     * @param source Arreglo de bytes con la información a encriptar.
     * @return arSource Arreglo de bytes con la información encriptada.
     */
    @Override
    public byte[] encrypt(byte[] source)
    {
        // El resultado de RC4 con los bytes de Source y el hash se asignan a un arreglo de bytes.
        byte[] arSource = rc4(source, hashSHA1);
        // El arrego de bytes se encripta en base64 y se retorna como resultado de la función.
        // return new BASE64Encoder().encode(arSource).getBytes("UTF-8");
        return arSource;
    }    

    /** 
     * Encripta un cadena de texto en base 64 utilizando la key recibida como parámetro y retorna un arreglo de bytes con la cadena encriptada. 
     * @param source Cadena de texto a encriptar.
     * @param key Clave utilizada por el algoritmo de encriptación.
     * @return Retorna un arreglo de bytes con la cadena encriptada.
     */
    @Override
    public byte[] encrypt(String source, String key)
    {
        try {
            return Base64.getEncoder().encode(encrypt(source.getBytes(), key));
        } catch (Exception ex) {
           
            return null;                        
        }
    }

    // Encriptación especial para uso interno.
    /**
     * Encripta una cadena de texto utilizando una key predefinida y retorna un arreglo de bytes con la cadena encriptada. 
     * @param source Cadena de texto a encriptar.
     * @return Retorna un arreglo de bytes con la cadena encriptada. 
     */
    @Override
    public byte[] encrypt(String source)
    {
        try {
            return Base64.getEncoder().encode(encrypt(source.getBytes()));
        } catch (Exception ex) {
            
            return null;            
        }
    }

    /*
     *     Funciones de Desencriptación
     */
    
    /**
     * Desencripta la información de un arreglo de bytes utilizando la key recibida como parámetro y retorna un arreglo de bytes con la información desencriptada.
     * @param source Arreglo de bytes con la información a encriptar. 
     * @param key Claver utilizada por el algoritmo para desencriptar. 
     * @return RC4(arSource, arHashSHA1) Algoritmo con la información desencriptada. 
     */@Override
    public byte[] decrypt(byte[] source, String key)
    {
        try {
            //Se crea una instancia de Base64Decoder y se desencripta Source en un arreglo de bytes.
            byte[] arSource = source;

            //Se toma el Hash SHA1 que se genera con Clave y se asigna a un arreglo de bytes.
            byte[] arHashSHA1 = Conversions.getHashSHA1(key);

            // Se regresa un arreglo de bytes del resultado del RC4 con los arreglos obtenidos anteriormente.
            return rc4(arSource, arHashSHA1);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            return null;
        }

    }

     /**
      * Encripta el contendido de un arreglo de bytes utilizando una key predefinida y retorna un arreglo de bytes con la información desencriptada.
      * @param source Arreglo de bytes con la información a desencriptar. 
      * @return RC4(arSource, hashSHA1) Arreglo de bytes con la información desencriptada. 
      */
    @Override
    public byte[] decrypt(byte[] source)
    {
        // Se crea una instancia de Base64Decoder y se desencripta Source en un arreglo de bytes.
        byte[] arSource = source;

        // Se retorna el arreglo de bytes del resultado del RC4 con los arreglos obtenidos anteriormente.
        return rc4(arSource, hashSHA1);
    }    

    /**
     * Desencripta una cadena utilizando la key recibida como parámetro y retorna un arreglo de bytes con la cadena de texto desencriptada.
     * @param source Cadena de texto a desencriptar.
     * @param key Clave utilizada por el algoritmo para desencriptar. 
     * @return decrypt(arSource, key) Arreglo con la información desencriptada. 
     */
    @Override
    public byte[] decrypt(String source, String key)
    {
        try {
            // Se crea una instancia de Base64Decoder y se desencripta Source en un arreglo de bytes.
            byte[] arSource = Base64.getDecoder().decode(source);

            // Se toma el Hash SHA1 que se genera con Clave y se asigna a un arreglo de bytes.
//            byte[] arHashSHA1 = Utils.getHashSHA1(key);

            // Se retorna el arreglo de bytes del resultado del RC4 con los arreglos obtenidos anteriormente.
            return decrypt(arSource, key);//RC4(arSource, arHashSHA1);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Desencripta una cadena utilizando una key predefinida y retorna un arreglo de bytes con la cadena de texto desencriptada.
     * @param source Cadena de texto a desencriptar. 
     * @return decrypt(arSource) Arreglo con la cadena de texto desencriptada. 
     */
    @Override
    public byte[] decrypt(String source)
    {
        try {
            //Se crea una instancia de Base64Decoder y se desencripta Source en un arreglo de bytes.            
            byte[] arSource = java.util.Base64.getDecoder().decode(source);

            // Se retorna el arreglo de bytes del resultado del RC4 con los arreglos obtenidos anteriormente.
            return decrypt(arSource); //RC4(arSource, hashSHA1);
        } catch (Exception ex) {
            return null;
        }
    }

    
    /*
        En esta función se utiliza byteToint para tomar los valores enteros de un byte,
        tambien se utiliza mod 256 como medida de prevención ya que  se maneja en tipos de datos enteros
    */
    /**
     * Encripa el contenido de un arreglo con el algoritmo RC4, utilizando la key recibida como parámetro y retorna un arreglo con la información encriptada. 
     * @param source Arreglo de bytes con la información a encriptar. 
     * @param key Clave utilizada por el algoritmo para encriptar.
     * @return source Arreglo de bytes con la información encriptada. 
     */
    private byte[] rc4(byte[] source, byte[] key)
    {
        int[] s = new int[256];
        int[] k = new int[256];
        byte bTemp;

        for (int i = 0; i < 256; i++)
        {
            s[i] = i;
            k[i] = Conversions.byteToInt(key[i % key.length]);
        }

        int j = 0;
        for (int i = 0; i < 256; i++)
        {
            j = (j + s[i] + k[i]) % 256;
            bTemp = (byte)(s[i] % 256);
            s[i] = s[j];
            s[j] = Conversions.byteToInt(bTemp);
        }

        int x = 0;
        j = 0;
        for (int i = 0; i < source.length; i++)
        {
            x = (x + 1) % 256;
            j = (j + s[x]) % 256;
            bTemp = (byte)(s[x] % 256);
            s[x] = s[j];
            s[j] = Conversions.byteToInt(bTemp);
            int t = (s[x] + s[j]) % 256;
            source[i] = (byte)((Conversions.byteToInt(source[i]) ^ s[t]) % 256);
        }

        return source;
    } 
}
