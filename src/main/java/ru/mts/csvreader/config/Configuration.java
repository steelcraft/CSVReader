package ru.mts.csvreader.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nezhinsky Konstantin
 * @version 1.0
 * <p>
 * Configuration interface.
 */
public class Configuration {
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String INT_TYPE = "Integer";
    public static final String TEXT_TYPE = "Text";
    public static final String DATE_TYPE = "Date";
    public static final String FLOAT_TYPE = "Float";

    private String sourceFolder;
    private String archiveFolder;
    private String failedFolder;
    private List<String> masks;
    private char delimiter;
    private boolean title;
    private Map<String, String> fields;

    //received application config
    private String receivedFolder;
    private String receivedFileNamePattern;
    private List<String> fieldsForSend;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public String getSourceFolder() {
        return sourceFolder != null ? sourceFolder : "";
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public String getArchiveFolder() {
        return archiveFolder != null ? archiveFolder : "";
    }

    public void setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
    }

    public String getFailedFolder() {
        return failedFolder != null ? failedFolder : "";
    }

    public void setFailedFolder(String failedFolder) {
        this.failedFolder = failedFolder;
    }

    public List<String> getMasks() {
        return masks;
    }

    public void setMasks(List<String> masks) {
        this.masks = masks;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char splitter) {
        this.delimiter = splitter;
    }

    public boolean hasTitle() {
        return title;
    }

    public void setTitle(boolean title) {
        this.title = title;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getReceivedFolder() {
        return receivedFolder;
    }

    public void setReceivedFolder(String receivedFolder) {
        this.receivedFolder = receivedFolder;
    }

    public String getReceivedFileNamePattern() {
        return receivedFileNamePattern;
    }

    public void setReceivedFileNamePattern(String receivedFileNamePattern) {
        this.receivedFileNamePattern = receivedFileNamePattern;
    }

    public List<String> getFieldsForSend() {
        return fieldsForSend;
    }

    public void setFieldsForSend(List<String> fieldsForSend) {
        this.fieldsForSend = fieldsForSend;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
}
