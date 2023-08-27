package xyz.hynse.hyshutuplogspamunknownstructure;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Hyshutuplogspamunknownstructure extends JavaPlugin {
    private final Map<String, Integer> logCounts = new HashMap<>();
    private final Map<String, Long> logTimestamps = new HashMap<>();
    private final long aggregationInterval = 2 * 1000;

    @Override
    public void onEnable() {
        // Register the event listener
        getServer().getPluginManager().registerEvents(new LogFilterListener(), this);
    }

    private class LogFilterListener implements org.bukkit.event.Listener {
        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String logMessage = event.getCommand();

            // Define the patterns for different log messages you want to handle
            String[] patterns = {
                    "Unknown structure start",
                    "Found reference to unknown structure"
            };

            for (String pattern : patterns) {
                if (logMessage.contains(pattern)) {
                    logCounts.put(pattern, logCounts.getOrDefault(pattern, 0) + 1);
                    logTimestamps.put(pattern, System.currentTimeMillis());

                    // Schedule a task to check and display stacked messages after the aggregation interval
                    FoliaScheduler.runGlobalDelay(Hyshutuplogspamunknownstructure.this, () -> {
                        int count = logCounts.getOrDefault(pattern, 0);
                        long timestamp = logTimestamps.getOrDefault(pattern, 0L);
                        long currentTime = System.currentTimeMillis();

                        if (count > 0 && (currentTime - timestamp) < aggregationInterval) {
                            Bukkit.getLogger().info(pattern + " (x" + count + ") in " + (currentTime - timestamp) + " ms");
                            logCounts.remove(pattern);
                            logTimestamps.remove(pattern);
                        }
                    }, aggregationInterval / 50); // Convert interval to server ticks

                    break; // No need to check other patterns
                }
            }
        }
    }
}
