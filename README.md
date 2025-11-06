# Velocity Server Manager

A Velocity plugin that automatically manages and starts subservers for different minigames in your Minecraft network.

## Features

- **Automatic Server Startup**: Starts all configured subservers when the Velocity proxy initializes
- **Minigame Support**: Configure different subservers for different minigame types (Bedwars, Skywars, Lobby, etc.)
- **Process Management**: Monitors server processes and handles graceful shutdown
- **Proxy Registration**: Automatically registers started servers with the Velocity proxy
- **Admin Commands**: Control servers through in-game commands

## Requirements

- Velocity 3.1.1 or higher
- Java 11 or higher

## Installation

1. Download the latest `VelocityServerManager.jar` from the releases
2. Place it in your Velocity proxy's `plugins` folder
3. Start the proxy - a default configuration will be created
4. Stop the proxy and configure your servers in `plugins/velocityservermanager/config.json`
5. Restart the proxy

## Configuration

The plugin creates a `config.json` file in `plugins/velocityservermanager/`:

```json
{
  "servers": [
    {
      "name": "lobby",
      "minigameType": "lobby",
      "startCommand": "java -Xmx512M -Xms512M -jar server.jar",
      "workingDirectory": "./servers/lobby",
      "autoStart": true,
      "host": "127.0.0.1",
      "port": 25565
    },
    {
      "name": "bedwars",
      "minigameType": "bedwars",
      "startCommand": "java -Xmx1G -Xms1G -jar server.jar",
      "workingDirectory": "./servers/bedwars",
      "autoStart": true,
      "host": "127.0.0.1",
      "port": 25566
    }
  ],
  "startupDelay": 5000,
  "registerServersWithProxy": true
}
```

### Configuration Options

- **servers**: Array of server configurations
  - **name**: Unique server identifier
  - **minigameType**: Type of minigame (e.g., bedwars, skywars, lobby)
  - **startCommand**: Command to start the server
  - **workingDirectory**: Directory where the server files are located
  - **autoStart**: Whether to start this server automatically on proxy startup
  - **host**: Server IP address
  - **port**: Server port
- **startupDelay**: Delay in milliseconds between starting each server (default: 5000)
- **registerServersWithProxy**: Automatically register servers with Velocity (default: true)

## Commands

- `/sm list` - List all running servers
- `/sm stop <server>` - Stop a specific server
- `/sm stopall` - Stop all running servers
- `/sm help` - Show help message

**Permission**: `velocityservermanager.admin` (for all commands)

## How It Works

1. When the Velocity proxy starts, the plugin loads the configuration
2. All servers with `autoStart: true` are started sequentially with the configured delay
3. Each server process is monitored by the plugin
4. After a 10-second warmup period, servers are registered with the Velocity proxy
5. When the proxy shuts down, all server processes are gracefully stopped

## Building from Source

**Note**: Building this plugin requires internet access to download the Velocity API from the Paper repository. If you're in a restricted network environment, you may need to configure a proxy or use a mirror repository.

```bash
./gradlew build
```

The compiled plugin will be in `build/libs/VelocityServerManager.jar`

### Build Requirements

- Java 11 or higher
- Internet access to download dependencies from:
  - https://repo.papermc.io/repository/maven-public/ (Velocity API)
  - https://repo1.maven.org/maven2/ (Maven Central for Gson)

If the Paper repository is not accessible, you can try using alternative repositories or downloading the Velocity API manually.

## Support

For issues or feature requests, please open an issue on the GitHub repository.
