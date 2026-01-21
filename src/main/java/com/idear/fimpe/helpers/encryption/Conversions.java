/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idear.fimpe.helpers.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author rperez
 */
public class Conversions {
    
    //
    //  Funciones comunes
    //
    public static byte[] byteArrayReverse(byte[] array) {
        byte[] data = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            data[i] = array[array.length - (1 + i)];
        }
        return data;
    }

    /**
     * Encripta una cadena de texto con el algoritmo SHA1 y retorna un arreglo
     * de bytes con la cadena encriptada.
     *
     * @param sLlave Clave que utiliza el algoritmo SHA1 para encriptar.
     * @return md.digest() Arreglo de bytes con la cadena encriptada.
     * @throws UnsupportedEncodingException Si el codificador de caracteres no
     * es soportado.
     * @throws NoSuchAlgorithmException Si un algoritmo criptográfico particular
     * es solicitado pero no está disponible en ese entorno.
     */
    public static byte[] getHashSHA1(String sLlave) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(sLlave.getBytes("iso-8859-1"), 0, sLlave.length());
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
           throw ex;
        }
    }

    /**
     * Convierte un byte a un entero con signo.
     *
     * @param b Byte a convertir.
     * @return b & 0xFF El entero con signo.
     */
    public static int byteToInt(byte b) {
        return b & 0xFF;
    }

    // Esta función se encarga de regresar el arreglo de bytes acomodado como entero positivo
    // por el tipo de arquitectura se invierten los bytes para el acomodo
    /**
     * Convierte un arreglo de bytes a un valor numérico.
     *
     * @param arByte Arreglo a convertir.
     * @return Retorna el valor numérico resultado de la conversión.
     */
    public static long byteArrayToLong(byte[] arByte) {
        long resp = 0;
        for (int i = 0; i < arByte.length; i++) {
            resp += ((long) arByte[i] & 0xFFL) << (i * 8);
        }
        return resp;
    }

    public static long byteArrayToNumber(byte[] arByte) {
        long resp = 0;
        for (int i = 0; i < arByte.length; i++) {
            resp += (long) arByte[i] << (long) (i * 8) & (i == 7 ? (long) 127 : (long) 255) << (i * 8);
        }
        if (arByte.length > 7 && arByte[7] < 0) {
            resp = (resp * -1) + 1;
        }
        return resp;
    }

    public static long byteArrayToLongInv(byte[] arByte) {
        long resp = 0;
        for (int i = 0; i < arByte.length; i++) {
            resp = (resp << 8) + (arByte[i] & 0xFF);
        }
        return resp;
    }

    /**
     * Convierte un valor tipo Long a array de bytes
     *
     * @param longNumber
     * @return Retorna el valor numérico resultado de la conversión.
     */
    public static byte[] longToByteArray(long longNumber) {
        byte[] array = new byte[8];
        for (int i = 0; i < 8; i++) {
            array[i] = (byte) ((longNumber >> (i * 8)) & 0xFF);
        }
        return array;
    }

    /**
     * Convierte un arreglo de bytes a una cadena hexadecimal. El metodo retorna
     * la cadena hexadecimal.
     *
     * @param b Arreglo de bytes a convertir.
     * @return result Cadena hexadecimal resultado de la conversión del arreglo
     * de bytes.
     */
    public static String getHexString(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }
        for (int i = 0; i < b.length; i++) {
            if (i != 0) {
                result += ", ";
            }
            result += "0x" + Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1).toUpperCase();
        }
        return result;
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Convierte un arreglo de bytes a una cadena hexadecimal. El metodo retorna
     * la cadena hexadecimal.
     *
     * @param b Arreglo de bytes a convertir.
     * @return result Cadena hexadecimal resultado de la conversión del arreglo
     * de bytes.
     */
    public static String hexArryToString(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }
        for (int i = 0; i < b.length; i++) {
            if (i != 0) {
                result += "";
            }
            result += Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * Convierte un arreglo de bytes a una cadena hexadecimal. El metodo retorna
     * la cadena hexadecimal.
     *
     * @param b Arreglo de bytes a convertir.
     * @return result Cadena hexadecimal resultado de la conversión del arreglo
     * de bytes.
     */
    public static String getHexStringLocationId(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1).toUpperCase();
        }
        return result;
    }

    public static String getHexAddress(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }
        for (int i = 0; i < b.length; i++) {
            if (i != 0) {
                result += ":";
            }
            result += Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1).toUpperCase();
        }
        return result;
    }

    public static String getDecimalAddress(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }
        for (int i = 0; i < b.length; i++) {
            if (i != 0) {
                result += ".";
            }
            result += Integer.toString(b[i] & 0xFF);
        }
        return result;
    }

    /**
     * Convierte un byte a una cadena hexadecimal. El método retorna la cadena
     * hexadecimal.
     *
     * @param b Byte a convertir.
     * @return Cadena hexadecimal resultado de la conversión del byte.
     */
    public static String getHexString(byte b) {
        return "0x" + Integer.toString((b & 0xff) + 0x100, 16).substring(1) + " ";
    }

    public static byte[] stringToByteArray(String string) {
        byte[] data = string.trim().getBytes();
        return data;
    }

    public static byte[] invertByteArray(byte[] array) {
        byte[] data = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            data[array.length - (1 + i)] = array[i];
        }
        return data;
    }

    /**
     * This function convert a HEX string in a byte array.
     *
     * @param s HEX string.
     * @return HEX string convert a array byte.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[(data.length - 1) - (i / 2)] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * This function convert a HEX string in a byte array inverted.
     *
     * @param s HEX string.
     * @return HEX string convert a array byte.
     */
    public static byte[] hexStringToByteArrayInv(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] hexStringToByteArrayFill(String s, int length) {
        int len = s.length();
        byte[] data = new byte[(length >= len / 2) ? length : len / 2];
        int i;
        for (i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        while (i / 2 < length) {
            data[i / 2] = 0;
            i += 2;
        }
        return data;
    }

    public static byte[] hexStringToByteArrayFillInv(String s, int length) {
        int len = s.length();
        byte[] data = new byte[(length >= len / 2) ? length : len / 2];
        int start = (length >= len / 2) ? length - len / 2 : 0;
        for (int i = 0; i < len; i += 2) {
            data[start + i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        for (int i = 0; i < length - len / 2; i++) {
            data[i] = 0;
        }
        return data;
    }

    public static String getCodeCard(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1).toUpperCase();
        }
        return result;
    }

    public static int byteArrayToInt(byte[] arByte) {
        long resp = 0;
        for (int i = 0; i < arByte.length; i++) {
            resp += (long) ((arByte[i] << (i * 8)) & (255 << (i * 8)));
        }
        return (int) resp;
    }

    public static short byteArrayToShort(byte[] arByte) {
        int resp = 0;
        for (int i = 0; i < arByte.length; i++) {
            resp += (short) ((arByte[i] << (i * 8)) & (255 << (i * 8)));
        }
        return (short) resp;
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64(String bytes) {
        return Base64.getDecoder().decode(bytes);
    }
}
