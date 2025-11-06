package de.noctivag.velocityservermanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;
    private final Gson gson;
    private Config config;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void loadConfig() {
        try {
            // Create data directory if it doesn't exist
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.json");
            
            if (!Files.exists(configFile)) {
                logger.info("Config file not found, creating default configuration...");
                createDefaultConfig();
                saveConfig();
            } else {
                // Load existing config
                try (Reader reader = new FileReader(configFile.toFile())) {
                    config = gson.fromJson(reader, Config.class);
                    logger.info("Configuration loaded successfully!");
                }
            }
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        config = new Config();
        config.servers = new ArrayList<>();
        
        // Add example minigame servers
        ServerConfig bedwars = new ServerConfig();
        bedwars.name = "bedwars";
        bedwars.minigameType = "bedwars";
        bedwars.startCommand = "java -Xmx1G -Xms1G -jar server.jar";
        bedwars.workingDirectory = "./servers/bedwars";
        bedwars.autoStart = true;
        bedwars.host = "127.0.0.1";
        bedwars.port = 25566;
        
        ServerConfig skywars = new ServerConfig();
        skywars.name = "skywars";
        skywars.minigameType = "skywars";
        skywars.startCommand = "java -Xmx1G -Xms1G -jar server.jar";
        skywars.workingDirectory = "./servers/skywars";
        skywars.autoStart = true;
        skywars.host = "127.0.0.1";
        skywars.port = 25567;
        
        ServerConfig lobby = new ServerConfig();
        lobby.name = "lobby";
        lobby.minigameType = "lobby";
        lobby.startCommand = "java -Xmx512M -Xms512M -jar server.jar";
        lobby.workingDirectory = "./servers/lobby";
        lobby.autoStart = true;
        lobby.host = "127.0.0.1";
        lobby.port = 25565;
        
        config.servers.add(lobby);
        config.servers.add(bedwars);
        config.servers.add(skywars);
        
        config.startupDelay = 5000; // 5 seconds delay between server starts
        config.registerServersWithProxy = true;
    }

    public void saveConfig() {
        try {
            Path configFile = dataDirectory.resolve("config.json");
            try (Writer writer = new FileWriter(configFile.toFile())) {
                gson.toJson(config, writer);
                logger.info("Configuration saved successfully!");
            }
        } catch (IOException e) {
            logger.error("Error saving configuration", e);
        }
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {
        public List<ServerConfig> servers;
        public int startupDelay;
        public boolean registerServersWithProxy;
    }

    public static class ServerConfig {
        public String name;
        public String minigameType;
        public String startCommand;
        public String workingDirectory;
        public boolean autoStart;
        public String host;
        public int port;
    }
}
