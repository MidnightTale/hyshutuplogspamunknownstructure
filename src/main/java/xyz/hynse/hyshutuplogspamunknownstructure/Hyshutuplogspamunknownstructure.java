package xyz.hynse.hyshutuplogspamunknownstructure;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Hyshutuplogspamunknownstructure extends JavaPlugin implements Listener {
    private final LogAggregator logAggregator = new LogAggregator();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this); // Register the outer class as a listener
    }

    private class LogAggregator implements Runnable {
        private final Map<String, Integer> logCounts = new HashMap<>();
        private final Map<String, Long> logTimestamps = new HashMap<>();
        private final long aggregationInterval = 3000; // 3 seconds
        private boolean aggregating = false;

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String logMessage = event.getCommand();
            String pattern = getLogPattern(logMessage);

            if (pattern != null) {
                logCounts.put(pattern, logCounts.getOrDefault(pattern, 0) + 1);
                logTimestamps.put(pattern, System.currentTimeMillis());

                if (!aggregating) {
                    FoliaScheduler.runGlobalDelay(Hyshutuplogspamunknownstructure.this, this, aggregationInterval);
                    aggregating = true;
                }
            }
        }

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();

            while (currentTime - logTimestamps.values().stream().min(Long::compare).orElse(currentTime) < aggregationInterval) {
                try {
                    Thread.sleep(aggregationInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                currentTime = System.currentTimeMillis();
            }

            for (Map.Entry<String, Integer> entry : logCounts.entrySet()) {
                String pattern = entry.getKey();
                int count = entry.getValue();
                long timestamp = logTimestamps.getOrDefault(pattern, 0L);

                if ((currentTime - timestamp) >= aggregationInterval) {
                    Bukkit.getLogger().info(pattern + " (x" + count + ") in " + (currentTime - timestamp) + " ms");
                    logCounts.remove(pattern);
                    logTimestamps.remove(pattern);
                }
            }

            aggregating = false;
        }

        private String getLogPattern(String logMessage) {
            String[] patterns = {
                    "Unknown structure start",
                    "Found reference to unknown structure"
                    // Add more patterns here as needed
            };

            for (String pattern : patterns) {
                if (logMessage.contains(pattern)) {
                    return pattern;
                }
            }

            return null;
        }
    }
}
