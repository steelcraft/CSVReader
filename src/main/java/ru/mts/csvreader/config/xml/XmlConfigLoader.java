package ru.mts.csvreader.config.xml;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.mts.csvreader.config.ConfigException;
import ru.mts.csvreader.config.ConfigLoader;
import ru.mts.csvreader.config.Configuration;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Nezhinsky Konstantin
 * @version 1.0
 * <p>
 * Load configuration from XML-file.
 */
public class XmlConfigLoader implements ConfigLoader {
    private static final String SOURCE_FOLDER_TYPE = "source";
    private static final String ARCHIVE_FOLDER_TYPE = "archive";
    private static final String FAILED_FOLDER_TYPE = "failed";

    private Configuration configuration;

    private final Logger logger = Logger.getLogger(XmlConfigLoader.class);

    /**
     * The method load configuration from XML-file.
     *
     * @param fileName configuration file.
     * @return Configuration
     * @throws ConfigException an exception is thrown in the case of an unsuccessful download configuration.
     */
    public Configuration load(String fileName) throws ConfigException {
        InputStream configFile = XmlConfigLoader.class.getResourceAsStream("/" + fileName);
        if (configFile == null)
            throw new IllegalArgumentException("Source configuration is null!");
        this.configuration = new Configuration();
        try {
            setConfiguration(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile));
        } catch (Exception ex) {
            throw new ConfigException(ex);
        }
        logger.info("Configuration loaded successfully.");
        return configuration;
    }

    private void setConfiguration(Node xmlNode) {
        NodeList nodeList = xmlNode.getChildNodes().item(0).getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                parseNode(node);
            }
        }
    }

    private void parseNode(Node node) {
        NodeList nodeList = node.getChildNodes();
        if (node.getNodeName().equals("folders")) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    String typeFolder = childNode.getAttributes().getNamedItem("type").getNodeValue();
                    String pathFolder = childNode.getTextContent();
                    setFolder(typeFolder, pathFolder);
                }
            }
        }
        if (node.getNodeName().equals("files")) {
            ArrayList<String> masks = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    masks.add(childNode.getTextContent());
                }
            }
            this.configuration.setMasks(masks);
            logger.debug(masks.size() == 0 ? "File masks are not defined." : "File masks set: " + masks.toString());
        }
        if (node.getNodeName().equals("fields")) {
            Node title = node.getAttributes().getNamedItem("title");
            this.configuration.setTitle((title != null) && title.getNodeValue().toUpperCase().equals("TRUE"));
            Node splitAttr = node.getAttributes().getNamedItem("splitter");
            this.configuration.setDelimiter((splitAttr != null) && splitAttr.getNodeValue().length() > 0 ? splitAttr.getNodeValue().charAt(0) : ' ');

            LinkedHashMap<String, String> fields = new LinkedHashMap<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    String fieldType = childNode.getAttributes().getNamedItem("type").getNodeValue();
                    String fieldName = childNode.getTextContent();
                    fields.put(fieldName, fieldType);
                }
            }
            this.configuration.setFields(fields);
            logger.debug(fields.size() == 0 ? "Fields settings are not defined." : "Fields settings defined: title='"
                    + this.configuration.hasTitle() + "', splitter='" + this.configuration.getDelimiter() + "', fields: " + fields.toString());
        }

        if (node.getNodeName().equals("send")) {
            Node receivedFolder = node.getAttributes().getNamedItem("folder");
            this.configuration.setReceivedFolder(receivedFolder.getNodeValue());
            logger.debug("Folder for received files: \"" + this.configuration.getReceivedFolder() + "\".");
            Node receivedFileName = node.getAttributes().getNamedItem("pattern");
            this.configuration.setReceivedFileNamePattern(receivedFileName.getNodeValue());
            logger.debug("File name pattern for received files: \"" + this.configuration.getReceivedFileNamePattern() + "\".");
            Node url = node.getAttributes().getNamedItem("url");
            this.configuration.setDbUrl(url.getNodeValue());
            logger.debug("Url for connect to database ': \"" + this.configuration.getDbUrl() + "\".");
            Node user = node.getAttributes().getNamedItem("user");
            this.configuration.setDbUser(user.getNodeValue());
            logger.debug("User for connect to database ': \"" + this.configuration.getDbUser() + "\".");
            Node pass = node.getAttributes().getNamedItem("password");
            this.configuration.setDbPassword(pass.getNodeValue());
            logger.debug("Password for connect to database ': \"" + this.configuration.getDbPassword() + "\".");

            ArrayList<String> fields = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE)
                    if (childNode.getNodeName().equals("field"))
                        fields.add(childNode.getTextContent());
            }
            this.configuration.setFieldsForSend(fields);
            logger.debug(fields.size() == 0 ? "Fields for sending settings are not defined." : "Fields for sending settings defined: " + fields.toString());
        }
    }

    private void setFolder(String type, String path) {
        logger.debug("Folder for '" + type + "': \"" + path + "\".");
        if (type.equals(SOURCE_FOLDER_TYPE)) {
            this.configuration.setSourceFolder(path);
            return;
        }
        if (type.equals(ARCHIVE_FOLDER_TYPE)) {
            this.configuration.setArchiveFolder(path);
            return;
        }
        if (type.equals(FAILED_FOLDER_TYPE)) {
            this.configuration.setFailedFolder(path);
        }
    }
}
