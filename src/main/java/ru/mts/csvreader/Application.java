package ru.mts.csvreader;

import org.apache.log4j.Logger;
import ru.mts.csvreader.config.ConfigLoader;
import ru.mts.csvreader.config.Configuration;
import ru.mts.csvreader.config.xml.XmlConfigLoader;
import ru.mts.csvreader.manager.FileManagerLauncher;

import java.io.File;
import java.util.Arrays;

/**
 * @author Nezhinsky Konstantin
 * @version 1.0
 * <p>
 * The application.
 * <p>
 * The argument is a configuration file.
 * <p>
 * Format of the call:
 * Application config_file
 * <p>
 * Example:
 * Application CSVReader-config.xml
 */
public class Application {
    private final static Logger logger = Logger.getLogger(Application.class);

    public static void main(String[] args) {

        //If the config file is not specified, the program is terminated.
        if (args.length == 0) {
            logger.warn("The configuration file is not defined. The program will be stopped.");
            System.exit(1);
        }

        String configFileName = args[0];
        logger.info("Loading configuration from '" + configFileName + "'...");
        ConfigLoader configLoader = new XmlConfigLoader();

        try {
            Configuration configuration = configLoader.load(configFileName);
            if (isValid(configuration)) {
                Thread fileManager = new Thread(new FileManagerLauncher(configuration));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    fileManager.interrupt();
                    logger.info("The program was stopped.");
                }));
                fileManager.start();
            } else {
                logger.error("The configuration file contains invalid parameters.");
            }
        } catch (Exception ex) {
            logger.error("Load configuration failed: " + ex.getMessage());
        }
    }

    /**
     * The method checks the validity of the configuration.
     * <p>
     * Configuration is valid, if:
     * <li>Directory with source files (@code{sourceFolder}) exists;</li>
     * <li>Directories for processed, failed, and received files exist or can be created.</li>
     * <li>Field types match one of the values ({@link Configuration#TEXT_TYPE TEXT_TYPE},
     * {@link Configuration#INT_TYPE INT_TYPE}, {@link Configuration#DATE_TYPE DATE_TYPE},
     * {@link Configuration#FLOAT_TYPE FLOAT_TYPE}).</li>
     *
     * @param configuration Application configuration.
     * @return true if configuration is valid.
     */
    private static boolean isValid(Configuration configuration) {
        File sourceFolder = new File(configuration.getSourceFolder());
        File destFolder = new File(configuration.getArchiveFolder());
        File failedFolder = new File(configuration.getFailedFolder());
        File receivedFolder = new File(configuration.getReceivedFolder());
        if (!sourceFolder.isDirectory() & (destFolder.exists() && destFolder.isDirectory() || destFolder.mkdirs())
                & (failedFolder.exists() && failedFolder.isDirectory() || failedFolder.mkdirs())
                & (receivedFolder.exists() && receivedFolder.isDirectory() || receivedFolder.mkdirs())) {
            logger.debug("Can not find the paths specified in the configuration.");
            return false;
        }
        String[] fields = configuration.getFields().values().toArray(new String[configuration.getFields().size()]);
        for (String fieldType : fields)
            if (!(fieldType.equalsIgnoreCase(Configuration.INT_TYPE) || fieldType.equalsIgnoreCase(Configuration.TEXT_TYPE) ||
                    fieldType.equalsIgnoreCase(Configuration.FLOAT_TYPE) || fieldType.equalsIgnoreCase(Configuration.DATE_TYPE))) {
                logger.debug("Invalid data types are specified in the configuration.");
                return false;
            }
        fields = configuration.getFields().keySet().toArray(new String[configuration.getFields().size()]);
        String[] feldsForSend = configuration.getFieldsForSend().toArray(new String[configuration.getFieldsForSend().size()]);
        for (String aFeldsForSend : feldsForSend)
            if (Arrays.asList(fields).indexOf(aFeldsForSend) < 0) {
                logger.debug("Invalid field names for sending in the configuration.");
                return false;
            }
        return true;
    }


}
