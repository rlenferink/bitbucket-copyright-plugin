package com.roylenferink.bitbucket.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

/**
 * Configure logging. For details, see: http://logback.qos.ch/manual/configuration.html
 * 
 * @author Roy Lenferink
 * 
 */
public class PluginLoggerFactory {

    private static PluginLoggerFactory instance = null;

    private LoggerContext context;
    private final String homeDir;

    private PluginLoggerFactory() {
        homeDir = new File(".").getAbsolutePath();

        // Assumes LSF4J is bound to logback
        context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // store the home dir to use for relative paths
        context.putProperty("bitbucket.home", homeDir);

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("logback.xml");
        try {
            configurator.doConfigure(is);
        } catch (JoranException e) {
            System.err.println("Error configuring logging framework" + e);
        }
    }

    public static PluginLoggerFactory getInstance() {
        if (instance == null)
            instance = new PluginLoggerFactory();

        return instance;
    }

    public Logger getLoggerForThis(Object obj) {
        String className = obj.getClass().toString();
        if (className.startsWith("class ")) {
            className = className.replaceFirst("class ", "");
        }

        return context.getLogger(className);

    }
}
