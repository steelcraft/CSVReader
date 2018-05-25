package ru.mts.csvreader.reciever;

import ru.mts.csvreader.config.Configuration;

import java.io.FileWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FileReceiver {
    private static final String TABLE_NAME = "sales";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String INT_TYPE = "INTEGER";
    private static final String TEXT_TYPE = "VARCHAR(50)";
    private static final String DATE_TYPE = " DATE";
    private static final String FLOAT_TYPE = "DOUBLE";

    private Configuration configuration;
    private FileWriter resultFile;
    private Connection connection;

    public FileReceiver(Configuration configuration) throws ReceiveFileException {
        this.configuration = configuration;
        try {
            this.resultFile = new FileWriter(configuration.getReceivedFolder() + "/"
                    + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_").format(System.currentTimeMillis()) + configuration.getReceivedFileNamePattern());
            Class.forName("org.hsqldb.jdbcDriver");
            this.connection = DriverManager.getConnection("jdbc:hsqldb:file:" + this.configuration.getDbUrl(), this.configuration.getDbUser(), this.configuration.getDbPassword());
            Statement statement = this.connection.createStatement();
            StringBuilder fieldSet = new StringBuilder();
            for (String field : this.configuration.getFieldsForSend()) {
                fieldSet.append(field).append(" ").append(setType(this.configuration.getFields().get(field))).append(", ");
            }
            fieldSet.append("id IDENTITY");
            String query = "CREATE TABLE " + TABLE_NAME + " (" + fieldSet + ")";
            try {
                statement.executeUpdate(query);
            } catch (SQLException ignored) {
            }
        } catch (Exception e) {
            throw new ReceiveFileException(e);
        }
    }

    private String setType(String type) {
        if (type.equalsIgnoreCase(Configuration.INT_TYPE))
            return INT_TYPE;
        if (type.equalsIgnoreCase(Configuration.FLOAT_TYPE))
            return FLOAT_TYPE;
        if (type.equalsIgnoreCase(Configuration.DATE_TYPE))
            return DATE_TYPE;
        return TEXT_TYPE;
    }

    public void send(String[] values) throws ReceiveFileException {
        char delimetr = this.configuration.getDelimiter();
        String[] feldsForSend = this.configuration.getFieldsForSend().toArray(new String[this.configuration.getFieldsForSend().size()]);
        StringBuilder stringFields = new StringBuilder();
        StringBuilder stringValues = new StringBuilder();
        try {
            String type;
            int i = 0;
            while (i < values.length - 1) {
                this.resultFile.write(values[i] + delimetr);
                stringFields.append(feldsForSend[i]).append(", ");
                type = setType(this.configuration.getFields().get(feldsForSend[i]));
                stringValues.append(castValue(type, values[i]));
                stringValues.append(", ");
                i++;
            }
            this.resultFile.write(values[i] + "\n");
            stringFields.append(feldsForSend[i]);
            type = setType(this.configuration.getFields().get(feldsForSend[i]));
            stringValues.append(castValue(type, values[i]));
            Statement statement = this.connection.createStatement();
            String query = "INSERT INTO " + TABLE_NAME + " (" + stringFields + ") VALUES (" + stringValues + ")";
            statement.executeUpdate(query);
        } catch (Exception e) {
            throw new ReceiveFileException(e);
        }
    }

    private String castValue(String type, String value) throws ParseException {
        if (type.equalsIgnoreCase(TEXT_TYPE))
            return "'" + value + "'";
        if (type.equalsIgnoreCase(DATE_TYPE))
            return "'" + new SimpleDateFormat(DATE_FORMAT).format(new  SimpleDateFormat(Configuration.DATE_FORMAT).parse(value)) + "'";
        return value;
    }

    public void finish() throws ReceiveFileException {
        try {
            this.resultFile.close();
            String query = "SHUTDOWN";
            this.connection.createStatement().execute(query);
            this.connection.close();
        } catch (Exception e) {
            throw new ReceiveFileException(e);
        }
    }
}
