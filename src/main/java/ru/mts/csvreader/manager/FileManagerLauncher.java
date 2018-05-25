package ru.mts.csvreader.manager;

import ru.mts.csvreader.config.Configuration;

public class FileManagerLauncher implements Runnable {
    private FileManager fileManager;

    public FileManagerLauncher(Configuration configuration) {
        this.fileManager = new FileManager(configuration);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                fileManager.parse();
            } catch (ParseFolderException e) {
                e.getMessage();
            }
        }
    }
}
