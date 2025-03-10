package com.kami.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO:
 * write a separate function for logging best trade information
 * maybe write the best trade selection efficiently
 * instead of calling the methods for getting players, villages, Reputation use variables
 * Introduce server config for different modes of trading example: best for all, worst for all, shared average, one time after curing, enable disable and other config functions with custom permissions
 * Release for bukkit purpur spigot folia
 **/

public class PlayerVillagerEvents implements Listener {
    private final JavaPlugin plugin;

    public PlayerVillagerEvents(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Hallo, " + event.getPlayer().getName() + "!");
        plugin.getLogger().info("Player Joined the server");

    }

    @EventHandler
    public void onPlayerInteractWithVillager(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Villager villager) {
            event.getPlayer().sendMessage("You interacted with: ", entity.getName());
            Optional<OfflinePlayer> bestReputationPLayer = Arrays.stream(Bukkit.getOfflinePlayers()).max(Comparator.comparingInt(e -> villager.getReputation(e.getUniqueId(), Villager.ReputationType.MAJOR_POSITIVE)));
            bestReputationPLayer.ifPresentOrElse(
                    player -> {
                        if (player.getUniqueId() == event.getPlayer().getUniqueId()) return;

                        int majorRep = villager.getReputation(player.getUniqueId(), Villager.ReputationType.MAJOR_POSITIVE);
                        int minorRep = villager.getReputation(player.getUniqueId(), Villager.ReputationType.MINOR_POSITIVE);
                        int majorRepN = villager.getReputation(player.getUniqueId(), Villager.ReputationType.MAJOR_NEGATIVE);
                        int minorRepN = villager.getReputation(player.getUniqueId(), Villager.ReputationType.MINOR_NEGATIVE);

                        villager.setReputation(event.getPlayer().getUniqueId(), Villager.ReputationType.MAJOR_POSITIVE, majorRep);
                        villager.setReputation(event.getPlayer().getUniqueId(), Villager.ReputationType.MINOR_POSITIVE, minorRep);
                        villager.setReputation(event.getPlayer().getUniqueId(), Villager.ReputationType.MAJOR_NEGATIVE, majorRepN);
                        villager.setReputation(event.getPlayer().getUniqueId(), Villager.ReputationType.MINOR_NEGATIVE, minorRepN);
                    },
                    () -> {
                    }
            );
        }
    }

    private void villagerReputationDebugData(Villager villager, String player_info, Integer reputation, String reputation_type) {
        File dataFolder = new File(plugin.getDataFolder().getParentFile(), plugin.getName());
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Failed to create data folder: " + dataFolder.getAbsolutePath());
            } else {
                plugin.getLogger().info("Data folder created at: " + dataFolder.getAbsolutePath());
            }
        }

        File file = new File(dataFolder, "villager_reputation_debug_data.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        UUID villagerId = villager.getUniqueId();

        config.set("villagers." + villagerId + "." + reputation_type + ".name", villagerId + " " + villager.getName());
        config.set("villagers." + villagerId + "." + reputation_type + ".player", player_info);
        config.set("villagers." + villagerId + "." + reputation_type + ".reputation", reputation);
        config.set("villagers." + villagerId + "." + reputation_type + ".reputation_type", reputation_type);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + plugin.getName() + " config file");
        }
    }
}
