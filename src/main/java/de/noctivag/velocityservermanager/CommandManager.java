package de.noctivag.velocityservermanager;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandManager implements SimpleCommand {

    private final ServerManager serverManager;
    private final Logger logger;

    public CommandManager(ServerManager serverManager, Logger logger) {
        this.serverManager = serverManager;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length == 0) {
            showHelp(invocation);
            return;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                listServers(invocation);
                break;
            case "stop":
                if (args.length < 2) {
                    invocation.source().sendMessage(
                        Component.text("Usage: /sm stop <server>", NamedTextColor.RED)
                    );
                    return;
                }
                stopServer(invocation, args[1]);
                break;
            case "stopall":
                stopAllServers(invocation);
                break;
            case "help":
            default:
                showHelp(invocation);
                break;
        }
    }

    private void showHelp(Invocation invocation) {
        invocation.source().sendMessage(Component.text("=== Velocity Server Manager ===", NamedTextColor.GOLD));
        invocation.source().sendMessage(Component.text("/sm list - List all running servers", NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/sm stop <server> - Stop a specific server", NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/sm stopall - Stop all running servers", NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/sm help - Show this help message", NamedTextColor.YELLOW));
    }

    private void listServers(Invocation invocation) {
        Map<String, Process> runningServers = serverManager.getRunningServers();
        
        if (runningServers.isEmpty()) {
            invocation.source().sendMessage(
                Component.text("No servers are currently running.", NamedTextColor.YELLOW)
            );
            return;
        }

        invocation.source().sendMessage(
            Component.text("Running servers (" + runningServers.size() + "):", NamedTextColor.GOLD)
        );
        
        for (String serverName : runningServers.keySet()) {
            invocation.source().sendMessage(
                Component.text("  - " + serverName, NamedTextColor.GREEN)
            );
        }
    }

    private void stopServer(Invocation invocation, String serverName) {
        if (!serverManager.isServerRunning(serverName)) {
            invocation.source().sendMessage(
                Component.text("Server '" + serverName + "' is not running!", NamedTextColor.RED)
            );
            return;
        }

        boolean success = serverManager.stopServer(serverName);
        if (success) {
            invocation.source().sendMessage(
                Component.text("Server '" + serverName + "' stopped successfully!", NamedTextColor.GREEN)
            );
        } else {
            invocation.source().sendMessage(
                Component.text("Failed to stop server '" + serverName + "'!", NamedTextColor.RED)
            );
        }
    }

    private void stopAllServers(Invocation invocation) {
        Map<String, Process> runningServers = serverManager.getRunningServers();
        
        if (runningServers.isEmpty()) {
            invocation.source().sendMessage(
                Component.text("No servers are currently running.", NamedTextColor.YELLOW)
            );
            return;
        }

        invocation.source().sendMessage(
            Component.text("Stopping all servers...", NamedTextColor.YELLOW)
        );
        
        serverManager.stopAllServers();
        
        invocation.source().sendMessage(
            Component.text("All servers have been stopped!", NamedTextColor.GREEN)
        );
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 0 || args.length == 1) {
            suggestions.add("list");
            suggestions.add("stop");
            suggestions.add("stopall");
            suggestions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("stop")) {
            suggestions.addAll(serverManager.getRunningServers().keySet());
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocityservermanager.admin");
    }
}
