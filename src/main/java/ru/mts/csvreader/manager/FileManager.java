package ru.mts.csvreader.manager;

import org.apache.log4j.Logger;
import ru.mts.csvreader.config.Configuration;
import ru.mts.csvreader.reciever.FileReceiver;
import ru.mts.csvreader.reciever.ReceiveFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {
    private final Logger logger = Logger.getLogger(FileManager.class);
    private Configuration configuration;

    public FileManager(Configuration config) {
        this.configuration = config;
    }

    public void parse() throws ParseFolderException {
        File sourceFolder = new File(this.configuration.getSourceFolder());
        File[] unhandledFiles = sourceFolder.listFiles((dir, name) -> {
            if (this.configuration.getMasks().size() == 0)
                return true;
            for (String mask : this.configuration.getMasks()) {
                Pattern p = Pattern.compile(mask.replaceAll("\\?", ".")
                        .replaceAll("\\*", ".*").toLowerCase());
                Matcher m = p.matcher(name.toLowerCase());
                if (m.matches())
                    return true;
            }
            return false;
        });
        if (unhandledFiles == null) {
            throw new ParseFolderException("Directory '" + sourceFolder.getAbsolutePath() + "' does not exist.");
        }
        for (File file : unhandledFiles) {
            try {
                handleFile(file);
            } catch (IOException e) {
                throw new ParseFolderException("Error reading file '" + file.getAbsolutePath() + "'.");
            }
        }
    }

    private void handleFile(File file) throws IOException {
        try {
            if (isValidCsvData(file)) {
                sendFile(file);
                logger.info("The file '" + file.getName() + "' was processed successfully.");
                moveFile(this.configuration.getArchiveFolder(), file);
            }
        } catch (ReceiveFileException e) {
            logger.error("Failed to send data: " + e.getMessage());
        } catch (CsvValidationException e) {
            logger.error("Validation of file '" + file.getName() + "' failed: " + e.getMessage());
            moveFile(this.configuration.getFailedFolder(), file);
        }
    }

    private void moveFile(String dirName, File file) {
        File dir = new File(dirName);
        String newFileName = getStampedFileName(file.getName());
        if (file.renameTo(new File(dir, newFileName)))
            logger.info("The file '" + file.getAbsolutePath() + "' was moved to '" + dir.getAbsolutePath() + "\\" + newFileName + "'.");
    }

    private boolean isValidCsvData(File file) throws CsvValidationException, IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            String[] fields = this.configuration.getFields().keySet().toArray(new String[this.configuration.getFields().size()]);
            if (this.configuration.hasTitle()) {
                String[] title = reader.readLine().split(String.valueOf(this.configuration.getDelimiter()));
                if (title.length < fields.length)
                    throw new CsvValidationException("Fields count is not valid.");
                for (int i = 0; i < fields.length; i++)
                    if (!fields[i].equalsIgnoreCase(title[i]))
                        throw new CsvValidationException("Fields set is not valid.");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (!isValidValue(line.split(String.valueOf(this.configuration.getDelimiter()))))
                    throw new CsvValidationException("Values of fields is not valid in line: '" + line + "'.");
            }
        }
        return true;
    }

    private boolean isValidValue(String[] values) {
        String[] fieldTypes = this.configuration.getFields().values().toArray(new String[this.configuration.getFields().size()]);
        for (int i = 0; i < fieldTypes.length; i++) {
            String type = fieldTypes[i];
            try {
                String value = values[i];
                if (type.equals(Configuration.INT_TYPE))
                    Integer.valueOf(value);
                if (type.equals(Configuration.FLOAT_TYPE))
                    Float.valueOf(value);
                if (type.equals(Configuration.DATE_TYPE))
                    new SimpleDateFormat(Configuration.DATE_FORMAT).parse(value);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void sendFile(File file) throws IOException, ReceiveFileException {
        FileReceiver receiver = new FileReceiver(this.configuration);
        String[] fieldTypes = this.configuration.getFields().keySet().toArray(new String[this.configuration.getFields().size()]);
        String[] feldsForSend = this.configuration.getFieldsForSend().toArray(new String[this.configuration.getFieldsForSend().size()]);
        try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            if (this.configuration.hasTitle())
                reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(String.valueOf(this.configuration.getDelimiter()));
                String[] sendingData = new String[feldsForSend.length];
                for (int i = 0; i < sendingData.length; i++) {
                    sendingData[i] = values[Arrays.asList(fieldTypes).indexOf(feldsForSend[i])];
                }
                receiver.send(sendingData);
            }
        } finally {
            receiver.finish();
        }
    }

    private String getStampedFileName(String fileName) {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_").format(System.currentTimeMillis()) + fileName;
    }
}
