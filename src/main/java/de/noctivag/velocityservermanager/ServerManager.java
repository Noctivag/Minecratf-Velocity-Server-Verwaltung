package de.noctivag.velocityservermanager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerManager {

    private final ProxyServer proxyServer;
    private final ConfigManager configManager;
    private final Logger logger;
    private final Map<String, Process> serverProcesses;
    private final Map<String, RegisteredServer> registeredServers;

    public ServerManager(ProxyServer proxyServer, ConfigManager configManager, Logger logger) {
        this.proxyServer = proxyServer;
        this.configManager = configManager;
        this.logger = logger;
        this.serverProcesses = new HashMap<>();
        this.registeredServers = new HashMap<>();
    }

    /**
     * Starts all servers configured with autoStart=true
     */
    public void startAllServers() {
        ConfigManager.Config config = configManager.getConfig();
        
        if (config.servers == null || config.servers.isEmpty()) {
            logger.warn("No servers configured!");
            return;
        }

        logger.info("Starting {} configured servers...", config.servers.size());
        
        for (ConfigManager.ServerConfig serverConfig : config.servers) {
            if (serverConfig.autoStart) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(config.startupDelay);
                        startServer(serverConfig);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while waiting to start server: " + serverConfig.name, e);
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    /**
     * Starts a specific server
     */
    public boolean startServer(ConfigManager.ServerConfig serverConfig) {
        if (serverProcesses.containsKey(serverConfig.name)) {
            logger.warn("Server {} is already running!", serverConfig.name);
            return false;
        }

        try {
            logger.info("Starting server: {} ({})", serverConfig.name, serverConfig.minigameType);
            
            // Prepare the working directory
            File workingDir = new File(serverConfig.workingDirectory);
            if (!workingDir.exists()) {
                logger.warn("Working directory {} does not exist! Creating it...", serverConfig.workingDirectory);
                workingDir.mkdirs();
            }

            // Parse and execute the start command
            // Use simple split for basic commands, users can provide pre-split commands if needed
            String[] commandArray = serverConfig.startCommand.trim().split("\\s+");
            ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
            processBuilder.directory(workingDir);
            processBuilder.redirectErrorStream(true);
            
            // Start the process
            Process process = processBuilder.start();
            serverProcesses.put(serverConfig.name, process);
            
            logger.info("Server {} started successfully!", serverConfig.name);

            // Register server with proxy if configured
            if (configManager.getConfig().registerServersWithProxy) {
                registerServerWithProxy(serverConfig);
            }

            // Monitor the process
            CompletableFuture.runAsync(() -> monitorProcess(serverConfig.name, process));

            return true;
        } catch (IOException e) {
            logger.error("Failed to start server: " + serverConfig.name, e);
            return false;
        }
    }

    /**
     * Register the server with the Velocity proxy
     */
    private void registerServerWithProxy(ConfigManager.ServerConfig serverConfig) {
        CompletableFuture.runAsync(() -> {
            try {
                // Wait for server to start up (configurable delay)
                int registrationDelay = configManager.getConfig().serverRegistrationDelay;
                Thread.sleep(registrationDelay);
                
                InetSocketAddress address = new InetSocketAddress(serverConfig.host, serverConfig.port);
                ServerInfo serverInfo = new ServerInfo(serverConfig.name, address);
                
                RegisteredServer registeredServer = proxyServer.registerServer(serverInfo);
                registeredServers.put(serverConfig.name, registeredServer);
                
                logger.info("Registered server {} with proxy at {}:{}", 
                    serverConfig.name, serverConfig.host, serverConfig.port);
            } catch (InterruptedException e) {
                logger.error("Interrupted while registering server: " + serverConfig.name, e);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Monitor a server process and log when it stops
     */
    private void monitorProcess(String serverName, Process process) {
        try {
            int exitCode = process.waitFor();
            logger.warn("Server {} has stopped with exit code: {}", serverName, exitCode);
            serverProcesses.remove(serverName);
        } catch (InterruptedException e) {
            logger.error("Interrupted while monitoring server: " + serverName, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stops a specific server
     */
    public boolean stopServer(String serverName) {
        Process process = serverProcesses.get(serverName);
        if (process == null) {
            logger.warn("Server {} is not running!", serverName);
            return false;
        }

        try {
            logger.info("Stopping server: {}", serverName);
            process.destroy();
            
            // Wait for graceful shutdown
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                logger.warn("Server {} did not stop gracefully, forcing shutdown...", serverName);
                process.destroyForcibly();
            }
            
            serverProcesses.remove(serverName);
            registeredServers.remove(serverName);
            logger.info("Server {} stopped successfully!", serverName);
            return true;
        } catch (InterruptedException e) {
            logger.error("Interrupted while stopping server: " + serverName, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Stops all running servers
     */
    public void stopAllServers() {
        logger.info("Stopping all running servers...");
        
        // Create a copy of the key set to avoid ConcurrentModificationException
        for (String serverName : new java.util.ArrayList<>(serverProcesses.keySet())) {
            stopServer(serverName);
        }
    }

    /**
     * Gets the status of a server
     */
    public boolean isServerRunning(String serverName) {
        Process process = serverProcesses.get(serverName);
        return process != null && process.isAlive();
    }

    /**
     * Gets all running servers
     */
    public Map<String, Process> getRunningServers() {
        return new HashMap<>(serverProcesses);
    }
}
