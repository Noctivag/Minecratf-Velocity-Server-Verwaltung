# Setup Guide - Velocity Server Manager

This guide will help you set up the Velocity Server Manager plugin to automatically start your minigame servers.

## Prerequisites

- Velocity proxy server (version 3.1.1 or higher)
- Java 11 or higher installed on your server
- Individual Minecraft server instances for each minigame

## Step-by-Step Setup

### 1. Install the Plugin

1. Download `VelocityServerManager.jar` from the releases
2. Place it in your Velocity proxy's `plugins/` folder
3. Start the Velocity proxy once to generate the default configuration
4. Stop the Velocity proxy

### 2. Prepare Your Server Structure

Create a directory structure for your servers. Example:

```
velocity-proxy/
├── plugins/
│   └── velocityservermanager/
│       └── config.json
└── servers/
    ├── lobby/
    │   └── server.jar
    ├── bedwars-1/
    │   └── server.jar
    ├── bedwars-2/
    │   └── server.jar
    ├── skywars-1/
    │   └── server.jar
    └── skywars-2/
        └── server.jar
```

### 3. Configure Your Servers

Edit `plugins/velocityservermanager/config.json`:

```json
{
  "servers": [
    {
      "name": "lobby",
      "minigameType": "lobby",
      "startCommand": "java -Xmx512M -Xms512M -jar server.jar nogui",
      "workingDirectory": "./servers/lobby",
      "autoStart": true,
      "host": "127.0.0.1",
      "port": 25565
    },
    {
      "name": "bedwars-1",
      "minigameType": "bedwars",
      "startCommand": "java -Xmx1G -Xms1G -jar server.jar nogui",
      "workingDirectory": "./servers/bedwars-1",
      "autoStart": true,
      "host": "127.0.0.1",
      "port": 25566
    }
  ],
  "startupDelay": 5000,
  "registerServersWithProxy": true,
  "serverRegistrationDelay": 10000
}
```

#### Configuration Field Explanations:

- **name**: Unique identifier for the server (used in commands and logs)
- **minigameType**: Category of minigame (bedwars, skywars, lobby, etc.)
- **startCommand**: Full command to start the server
  - Add `nogui` flag to prevent GUI from opening
  - Adjust memory flags (`-Xmx`, `-Xms`) based on your server's needs
  - Use `\\s+` regex-compatible spacing (avoid paths with spaces in simple configs)
- **workingDirectory**: Path to the server files (relative or absolute)
- **autoStart**: Set to `true` to start automatically, `false` to start manually
- **host**: IP address the server will bind to (usually 127.0.0.1 for local)
- **port**: Unique port for each server
- **startupDelay**: Time in milliseconds to wait between starting each server
- **registerServersWithProxy**: Automatically add servers to Velocity's server list
- **serverRegistrationDelay**: Time in milliseconds to wait after server start before registering with proxy (allows server warmup)

### 4. Configure Server Ports

Make sure each server has a unique port in their `server.properties`:

**servers/lobby/server.properties:**
```properties
server-port=25565
```

**servers/bedwars-1/server.properties:**
```properties
server-port=25566
```

And so on for each server.

### 5. Set Up Velocity Configuration

Edit your Velocity `velocity.toml` to include a try list and default server:

```toml
[servers]
# The try list defines the order in which servers are tried
try = [
    "lobby"
]

# Servers are registered automatically by the plugin
# But you can add them manually here if needed
lobby = "127.0.0.1:25565"
```

### 6. Start Everything Up

1. Start the Velocity proxy
2. The plugin will automatically:
   - Load the configuration
   - Start all servers with `autoStart: true`
   - Wait the configured delay between each server start
   - Register servers with the Velocity proxy after they're running
3. Monitor the logs to ensure all servers start correctly

### 7. Verify Server Status

Use the plugin commands to check server status:

```
/sm list
```

This will show all running servers.

## Common Issues and Solutions

### Server Not Starting

**Problem**: Server shows as not starting in logs

**Solutions**:
1. Check that the `workingDirectory` path is correct
2. Verify that `server.jar` exists in the working directory
3. Ensure Java is installed and accessible from the command line
4. Check that the port is not already in use
5. Review the server's own logs in its directory

### Port Already in Use

**Problem**: Error about port being in use

**Solutions**:
1. Make sure each server has a unique port in both `config.json` and `server.properties`
2. Check that no other applications are using the ports
3. Use `netstat -an | grep <port>` to see what's using a port

### Server Not Registered with Proxy

**Problem**: Players can't connect to a server

**Solutions**:
1. Wait 10 seconds after server start (automatic registration delay)
2. Check Velocity logs for registration messages
3. Verify server is actually running (`/sm list`)
4. Check that `registerServersWithProxy` is set to `true`

### Memory Issues

**Problem**: Server crashes or runs slowly

**Solutions**:
1. Increase memory allocation in `startCommand` (e.g., `-Xmx2G` for 2GB)
2. Make sure your host machine has enough RAM for all servers
3. Consider reducing the number of simultaneously running servers

## Advanced Configuration

### Using Different Java Versions

If you need different Java versions for different servers:

```json
{
  "name": "modern-server",
  "startCommand": "/usr/lib/jvm/java-17/bin/java -Xmx1G -jar server.jar nogui",
  ...
}
```

### Remote Servers

To start servers on remote machines, you'll need to:
1. Set `autoStart: false` for those servers
2. Start them manually on their respective machines
3. Configure them in Velocity's `velocity.toml` directly

### Custom JVM Arguments

Add custom JVM arguments to optimize performance:

```json
{
  "startCommand": "java -Xmx2G -Xms2G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -jar server.jar nogui",
  ...
}
```

## Next Steps

1. Configure your minigame plugins on each server
2. Set up player balancing or lobby systems
3. Test failover and server restart scenarios
4. Monitor server performance and adjust memory as needed

## Support

For issues or questions, check the main README or open an issue on the GitHub repository.
