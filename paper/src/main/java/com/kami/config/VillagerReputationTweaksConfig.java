package com.kami.config;

import com.kami.utils.ConfigEnumTypes;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class VillagerReputationTweaksConfig {

    private YamlConfiguration customConfig;
    private final String fileName = "reputation-modes.yml";
    private volatile ConfigEnumTypes tradeMode;
    private volatile boolean enabled;
    private final Plugin plugin;
    private final ReentrantLock lock = new ReentrantLock();

    private VillagerReputationTweaksConfig(Plugin plugin) {
        this.plugin = plugin;
        loadCustomConfig();
        validateCustomConfig(); // Validate only once during initialization
        tradeMode = readTradeMode();
        enabled = readEnabled();
    }

    private static class SingletonHelper {
        private static volatile VillagerReputationTweaksConfig INSTANCE;
    }

    public static VillagerReputationTweaksConfig getPluginConfig(Plugin plugin) {
        if (SingletonHelper.INSTANCE == null) {
            synchronized (VillagerReputationTweaksConfig.class) {
                if (SingletonHelper.INSTANCE == null) {
                    SingletonHelper.INSTANCE = new VillagerReputationTweaksConfig(plugin);
                }
            }
        }
        return SingletonHelper.INSTANCE;
    }

    private void createCustomConfigFile() {
        File configFile = new File(plugin.getDataFolder(), fileName);

        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
            plugin.getLogger().info(fileName + " was missing. Copied from resources.");
        }
    }

    private void loadCustomConfig() {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.getLogger().warning(fileName + " not found! Copying default...");
            createCustomConfigFile();
        }

        customConfig = YamlConfiguration.loadConfiguration(file);
    }

    private void validateCustomConfig() {
        boolean updated = false;

        if (!customConfig.contains("enabled")) {
            customConfig.set("enabled", true);
            plugin.getLogger().warning("Missing key: custom-setting. Resetting to default.");
            updated = true;
        }

        if (!customConfig.contains("reputation-type.trade-mode")) {
            customConfig.set("reputation-type.trade-mode", "best-trades");
            plugin.getLogger().warning("Missing key: reputation-type.best-trades. Resetting to default");
            updated = true;
        }

        // Check value type
        if (!customConfig.isBoolean("enabled")) {
            customConfig.set("enabled", true);
            plugin.getLogger().warning("Invalid value for 'enabled'. Resetting to true.");
            updated = true;
        }

        if (!customConfig.isString("reputation-type.trade-mode")) {
            customConfig.set("reputation-type.trade-mode", "best-trades");
            plugin.getLogger().warning("Invalid value for 'reputation-type.trade-mode'. Resetting to 'best-trades'");
            updated = true;
        }

        boolean correctMode = Arrays.stream(ConfigEnumTypes.values())
                .anyMatch(e -> e.getValue().equalsIgnoreCase(customConfig.getString("reputation-type.trade-mode")));

        if (!correctMode) {
            customConfig.set("reputation-type.trade-mode", "best-trades");
            plugin.getLogger().warning("Invalid value for 'reputation-type.trade-mode'. Resetting to 'best-trades'");
            updated = true;
        }

        if (updated) {
            saveCustomConfigFile();
            plugin.getLogger().info(fileName + " updated with missing/default values.");
        }
    }

    private void saveCustomConfigFile() {
        try {
            customConfig.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + fileName + "!");
            e.printStackTrace();
        }
    }

    private ConfigEnumTypes readTradeMode() {
        String configTradeMode = customConfig.getString("reputation-type.trade-mode");
        return Arrays.stream(ConfigEnumTypes.values())
                .filter(e -> e.getValue().equalsIgnoreCase(configTradeMode))
                .findAny()
                .orElse(ConfigEnumTypes.BEST_TRADES);
    }

    private boolean readEnabled() {
        return customConfig.getBoolean("enabled");
    }

    public ConfigEnumTypes getTradeMode() {
        return tradeMode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        lock.lock();
        try {
            customConfig.set("enabled", enabled);
            saveCustomConfigFile();
            this.enabled = enabled;
        } finally {
            lock.unlock();
        }
    }

    public void setTradeMode(ConfigEnumTypes tMode) {
        lock.lock();
        try {
            customConfig.set("reputation-type.trade-mode", tMode.getValue());
            saveCustomConfigFile();
            this.tradeMode = tMode;
        } finally {
            lock.unlock();
        }
    }

    public void reloadConfig() {
        lock.lock();
        try {
            loadCustomConfig();
            validateCustomConfig();
            tradeMode = readTradeMode();
            enabled = readEnabled();
            plugin.getLogger().info("Config reloaded");
        } finally {
            lock.unlock();
        }
    }
}
