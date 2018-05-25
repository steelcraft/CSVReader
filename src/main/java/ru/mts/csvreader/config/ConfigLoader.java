package ru.mts.csvreader.config;

/**
 * @author Nezhinsky Konstantin
 * @version 1.0
 * <p>
 * Configuration loader interface.
 */
public interface ConfigLoader {
    Configuration load(String fileName) throws ConfigException;
}
