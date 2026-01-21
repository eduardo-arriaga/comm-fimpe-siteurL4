package com.idear.fimpe.helpers.logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

/**
 * Logger basado en la librería log4j2 con la fachada de slf4j
 * @author rperez
 */
public class LoggerManagerLog4J2 {

    private LoggerManagerLog4J2(){
        //Para no poder contruir el objeto
    }
    /**
     * Configura el logger, define el patron de logueo, el nombre del archivo destino y el nivel de logueo
     * @param maxFilesToRolling indica el numero de archivos maximos creados para considerar reescribir el mas viejo
     * @param maxFileSize es el maximo en MB o K para considerar que el archivo esta lleno y pasar a escribir en otro
     */
    public static void configure(String maxFilesToRolling, String maxFileSize) {
        //Generamos el configurador
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.INFO);
        builder.setConfigurationName("DefaultLogger");

        //Nombre del archivo para el rolling file
        String fileName = "logs/mmp.log";

        //Patron del logueo
        String pattern = "%d{dd MMM yyyy HH:mm:ss} %t %-5p %m%n";
        LayoutComponentBuilder patternLayout = builder.newLayout("PatternLayout");
        patternLayout.addAttribute("pattern", pattern);

        //Genera el appender de consola
        configureConsole(builder, patternLayout);
        //Genera el appender del archivo cíclico
        configureRollingFile(builder, patternLayout, fileName, maxFilesToRolling, maxFileSize);

        //Creamos el logueador raiz
        RootLoggerComponentBuilder rootLooger = builder.newRootLogger(Level.INFO);
        //Agregamos los appender
        rootLooger.add(builder.newAppenderRef("Console"));
        rootLooger.add(builder.newAppenderRef("LogToRollingFile"));

        //Agregamos el logueador raiz al configurador
        builder.add(rootLooger);

        //configuramos
        Configurator.initialize(builder.build());
    }


    /**
     * Configura el appender ligado a la consola
     * @param builder Configurador general
     * @param patternLayout Patron de loggueo
     */
    private static void configureConsole(ConfigurationBuilder<BuiltConfiguration> builder, LayoutComponentBuilder patternLayout) {
        AppenderComponentBuilder console = builder.newAppender("Console", "Console")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        console.add(patternLayout);
        builder.add(console);
    }

    /**
     * Configura el appender ligado a la generación de archivos que se sobrescriben mediante políticas y estrategias
     * @param builder Configurador general
     * @param patternLayout Patron de logger
     * @param fileName Nombre del archivo destino
     * @param maxFilesToRolling índica el número de archivos máximos creados para considerar reescribir el más viejo
     * @param maxFileSize es el maximo en MB o K para considerar que el archivo está lleno y pasar a escribir en otro
     */
    private static void configureRollingFile(ConfigurationBuilder<BuiltConfiguration> builder,
                                      LayoutComponentBuilder patternLayout, String fileName,
                                      String maxFilesToRolling, String maxFileSize) {

        //Generamos el nodo de políticas y a su vez un nodo del tamaño maximo del archivo
        ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", maxFileSize));

        //Generamos el nodo de estrategia del maximo de archivos
        ComponentBuilder defaultRolloverStrategy = builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", maxFilesToRolling);

        //Creamos el appender y agregamos los nodos de politicas y estrategias
        AppenderComponentBuilder rollingFileAppender = builder.newAppender("LogToRollingFile", "RollingFile")
                .addAttribute("fileName", fileName)
                .addAttribute("filePattern", fileName + "-%d{MM-dd-yyyy}-%i.log")
                .add(patternLayout)
                .addComponent(triggeringPolicy)
                .addComponent(defaultRolloverStrategy);

        builder.add(rollingFileAppender);
    }
}
