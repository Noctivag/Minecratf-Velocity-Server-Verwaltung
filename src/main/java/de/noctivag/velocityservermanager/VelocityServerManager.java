package de.noctivag.velocityservermanager;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "velocityservermanager",
    name = "Velocity Server Manager",
    version = "1.0.0",
    description = "Manages and starts subservers for different minigames",
    authors = {"Noctivag"}
)
public class VelocityServerManager {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager configManager;
    private ServerManager serverManager;

    @Inject
    public VelocityServerManager(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Velocity Server Manager...");
        
        // Load configuration
        configManager = new ConfigManager(dataDirectory, logger);
        configManager.loadConfig();
        
        // Initialize server manager
        serverManager = new ServerManager(server, configManager, logger);
        
        // Start all configured servers after proxy initialization
        logger.info("Starting all configured subservers...");
        serverManager.startAllServers();
        
        // Register commands
        CommandManager commandManager = new CommandManager(serverManager, logger);
        server.getCommandManager().register("servermanager", commandManager, "sm");
        
        logger.info("Velocity Server Manager initialized successfully!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutting down Velocity Server Manager...");
        if (serverManager != null) {
            serverManager.stopAllServers();
        }
        logger.info("Velocity Server Manager shut down successfully!");
    }
}
