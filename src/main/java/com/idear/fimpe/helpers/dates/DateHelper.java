package com.idear.fimpe.helpers.dates;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateHelper {

    /**
     * Retorna el dia anterior con la la hora en 23:59:59
     * @return Una fecha establecida en el dia anterior a media noche
     */
    public static LocalDateTime getYesterdayMidnight(){
        LocalTime midnight = LocalTime.of(23, 59, 59);
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return LocalDateTime.of(today, midnight).minusDays(1);
    }

    /**
     * Convierte la hora, minutos y segundos de una fecha a cero
     * @param dateToConvert Fecha a convertir
     * @return Fecha convertida
     */
    public static LocalDateTime convertDateToZeroTime(LocalDateTime dateToConvert){
        LocalTime zeroTime = LocalTime.of(0,0,0);
        LocalDate date = dateToConvert.toLocalDate();
        return LocalDateTime.of(date, zeroTime);
    }

    public static String getDateFormatFull(LocalDateTime date) {

        String result = "";
        DecimalFormat decimalFormat = new DecimalFormat("00");

        result += String.valueOf(date.getYear());
        result += String.valueOf(decimalFormat.format(date.getMonthValue()));
        result += String.valueOf(decimalFormat.format(date.getDayOfMonth()));
        result += String.valueOf(decimalFormat.format(date.getHour()));
        result += String.valueOf(decimalFormat.format(date.getMinute()));
        result += String.valueOf(decimalFormat.format(date.getSecond()));
        return result;
    }

    public static String getDateFormatFull(LocalDate date) {

        String result = "";
        DecimalFormat decimalFormat = new DecimalFormat("00");

        result += String.valueOf(date.getYear());
        result += decimalFormat.format(date.getMonthValue());
        result += decimalFormat.format(date.getDayOfMonth());
        return result;
    }

    /**
     * Convierte la hora, minutos y segundos de una fecha a cero
     * @return Fecha convertida
     */
    public static LocalDateTime getLocalDateFromHourMinuteSecond(LocalDateTime localDateTimeToTransform,
                                                                 int hour, int minute, int second){
        LocalTime time = LocalTime.of(hour,minute,second);
        LocalDate date = localDateTimeToTransform.toLocalDate();
        return LocalDateTime.of(date, time);
    }

    public static String getDateNowFormatted(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-YYYY");
        return formatter.format(LocalDateTime.now());
    }

    public static String getDateFormatted(LocalDateTime dateTimeToFormatter){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-YYYY hh:mm:ss");
        return formatter.format(dateTimeToFormatter);
    }
}
