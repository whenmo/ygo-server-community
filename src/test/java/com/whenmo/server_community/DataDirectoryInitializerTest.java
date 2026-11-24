package com.whenmo.server_community;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DataDirectoryInitializerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsConfiguredDirectoriesAndSqliteDatabase() {
        Path cacheDirectory = temporaryDirectory.resolve("cache");
        Path decksDirectory = temporaryDirectory.resolve("decks");
        Path databasePath = temporaryDirectory.resolve("instance/config.db");

        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "app.data.directory", temporaryDirectory.toString())));

            new DataDirectoryInitializer().initialize(context);
        }

        assertTrue(Files.isDirectory(cacheDirectory));
        assertTrue(Files.isDirectory(decksDirectory));
        assertTrue(Files.isRegularFile(databasePath));
    }
}