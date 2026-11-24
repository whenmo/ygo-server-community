package com.whenmo.server_community;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataDirectoryInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        Path dataDirectory = Path.of(environment.getProperty("app.data.directory", "."))
            .toAbsolutePath().normalize();
        createDirectory(dataDirectory.toString());
        createDirectory(environment.getProperty("app.cache.directory", dataDirectory.resolve("cache").toString()));
        createDirectory(environment.getProperty("app.decks.directory", dataDirectory.resolve("decks").toString()));
        createDatabase(environment.getProperty("spring.datasource.url",
            "jdbc:sqlite:" + dataDirectory.resolve("instance/config.db")));
    }

    private void createDirectory(String directory) {
        try {
            Files.createDirectories(Path.of(directory).toAbsolutePath().normalize());
        } catch (IOException exception) {
            throw new IllegalStateException("無法建立資料目錄: " + directory, exception);
        }
    }

    private void createDatabase(String databaseUrl) {
        String sqlitePrefix = "jdbc:sqlite:";
        if (!databaseUrl.startsWith(sqlitePrefix)) {
            return;
        }

        String databaseLocation = databaseUrl.substring(sqlitePrefix.length());
        if (databaseLocation.isBlank() || databaseLocation.equals(":memory:")) {
            return;
        }

        Path databasePath = Path.of(databaseLocation).toAbsolutePath().normalize();
        Path parent = databasePath.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(databasePath)) {
                Files.createFile(databasePath);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("無法建立 SQLite 資料庫: " + databasePath, exception);
        }
    }
}