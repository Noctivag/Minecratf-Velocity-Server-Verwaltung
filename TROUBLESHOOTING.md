# Troubleshooting Guide

## Common Issues

### 1. Plugin Not Loading

**Symptoms:**
- No messages about Velocity Server Manager in logs
- Plugin not listed in `/velocity plugins`

**Possible Causes:**
- Plugin not in the correct directory
- Velocity version incompatibility
- Corrupted JAR file

**Solutions:**
1. Verify the plugin is in the `plugins/` folder
2. Check that you're running Velocity 3.1.1 or higher
3. Re-download the plugin JAR
4. Check Velocity logs for any error messages during startup

### 2. Servers Not Auto-Starting

**Symptoms:**
- Velocity starts but no servers are launched
- `/sm list` shows no running servers

**Possible Causes:**
- Configuration errors
- `autoStart` set to `false`
- Incorrect working directory paths
- Java not found in PATH

**Solutions:**
1. Check `plugins/velocityservermanager/config.json` exists
2. Verify `autoStart: true` for servers you want to auto-start
3. Ensure `workingDirectory` paths are correct (use absolute paths if relative paths don't work)
4. Test the `startCommand` manually in a terminal to ensure it works
5. Check plugin logs for specific error messages

### 3. Server Process Starts but Crashes

**Symptoms:**
- Server appears in `/sm list` briefly then disappears
- Server logs show crashes or errors

**Possible Causes:**
- Insufficient memory
- Missing server files (eula.txt, world files, etc.)
- Port conflicts
- Corrupted world or config files

**Solutions:**
1. Check the server's own logs in its `workingDirectory`
2. Increase memory allocation in `startCommand`
3. Ensure `eula.txt` exists and `eula=true`
4. Verify all required server files are present
5. Check for port conflicts with `netstat -tuln | grep <port>`

### 4. Server Not Registering with Proxy

**Symptoms:**
- Server running (`/sm list` shows it)
- But players can't connect
- Server not in Velocity's server list

**Possible Causes:**
- Server hasn't finished starting (still loading)
- Wrong IP/port configuration
- Firewall blocking connection
- `registerServersWithProxy` disabled

**Solutions:**
1. Wait at least 30 seconds after server start
2. Verify `host` and `port` match the server's `server.properties`
3. Check `registerServersWithProxy: true` in config
4. Test connection manually: `telnet <host> <port>`
5. Review Velocity logs for registration messages

### 5. Permission Denied Errors

**Symptoms:**
- Commands don't work
- "You don't have permission" messages

**Possible Causes:**
- Missing `velocityservermanager.admin` permission
- Velocity permissions not configured

**Solutions:**
1. Give yourself admin permissions in Velocity's config
2. Add to `velocity.toml`:
   ```toml
   [permissions]
   "velocityservermanager.admin" = ["YourUsername"]
   ```
3. Or use a permissions plugin like LuckPerms

### 6. Servers Starting Too Quickly

**Symptoms:**
- System overload
- Some servers fail to start
- High CPU usage

**Possible Causes:**
- `startupDelay` too short
- Too many servers starting at once

**Solutions:**
1. Increase `startupDelay` in config (e.g., from 5000 to 10000)
2. Set some servers to `autoStart: false`
3. Stagger server starts by starting them in groups

### 7. Configuration Not Loading

**Symptoms:**
- Changes to config.json not taking effect
- Plugin uses default configuration

**Possible Causes:**
- JSON syntax errors
- File not saved
- Caching issues

**Solutions:**
1. Validate JSON syntax using a JSON validator
2. Check for common errors:
   - Missing commas between objects
   - Trailing commas
   - Unquoted strings
3. Ensure file is saved before restarting proxy
4. Delete config and let plugin regenerate it

### 8. Memory Issues

**Symptoms:**
- OutOfMemoryError in logs
- Servers crashing randomly
- System slowdown

**Possible Causes:**
- Insufficient system RAM
- Too many servers running
- Memory leaks

**Solutions:**
1. Reduce number of running servers
2. Increase system RAM
3. Adjust individual server memory allocations
4. Monitor with `top` or `htop` to see actual usage
5. Consider using Docker with memory limits

### 9. Plugin Commands Not Working

**Symptoms:**
- Commands show "Unknown command"
- No response to `/sm` commands

**Possible Causes:**
- Plugin not loaded
- Command conflicts
- Console vs player execution

**Solutions:**
1. Check plugin is loaded: `/velocity plugins`
2. Try alternate alias: `/servermanager` instead of `/sm`
3. Check if you have permission
4. Try from both console and in-game

### 10. Servers Not Stopping on Proxy Shutdown

**Symptoms:**
- Servers keep running after Velocity stops
- Have to manually kill processes

**Possible Causes:**
- Plugin not handling shutdown event
- Servers not responding to stop signals
- Forceful proxy termination

**Solutions:**
1. Give servers time to stop (up to 30 seconds)
2. Use `/sm stopall` before stopping proxy
3. Avoid killing the proxy process forcefully
4. Check server logs to see if they received stop signal

## Debugging Tips

### Enable Verbose Logging

Check Velocity's logging configuration and ensure it's set to capture plugin messages.

### Test Manually

Try starting a server manually using the exact command from your config:

```bash
cd /path/to/server
java -Xmx1G -Xms1G -jar server.jar nogui
```

If this works, the plugin should be able to start it too.

### Check Permissions

Make sure the user running Velocity has:
- Read access to the plugin JAR
- Read/write access to the config directory
- Execute permissions for Java
- Read/write access to server directories

### Monitor System Resources

Use these commands to monitor your system:

```bash
# CPU and memory usage
top

# Disk space
df -h

# Check what's using ports
netstat -tuln | grep 255

# Watch logs in real-time
tail -f velocity/logs/latest.log
```

### Test Network Connectivity

Test if servers are actually listening:

```bash
# Test if a server is accepting connections
nc -zv 127.0.0.1 25565

# Or with telnet
telnet 127.0.0.1 25565
```

## Getting Help

If you're still having issues:

1. Check the plugin logs in Velocity's log file
2. Check individual server logs in their directories
3. Create an issue on GitHub with:
   - Your configuration file (remove sensitive info)
   - Relevant log excerpts
   - Velocity version
   - Java version
   - Operating system
4. Include what you've already tried

## Useful Commands Reference

```bash
# Check Java version
java -version

# Check running Java processes
ps aux | grep java

# Kill a stuck server process
kill -9 <PID>

# Check what's using a port
lsof -i :<port>

# Monitor plugin logs
tail -f plugins/velocityservermanager/logs/*.log

# Validate JSON config
python -m json.tool config.json
```
