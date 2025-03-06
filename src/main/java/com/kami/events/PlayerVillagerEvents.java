package com.kami.events;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import com.destroystokyo.paper.utils.PaperPluginLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import java.util.Map;
import java.util.UUID;

/**
 * TODO:
 * PaperPluginLogger.getLogger(plugin.getName()): do clean rewrite (if willing write a logger class)
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
        event.getPlayer().sendMessage(Component.text("Hallo, " + event.getPlayer().getName() + "!"));
        PaperPluginLogger.getLogger(plugin.getName()).info("Player Joined the server");
    }

    @EventHandler
    public void onPlayerInteractWithVillager(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Villager villager) {
            event.getPlayer().sendMessage("You interacted with: ", entity.getName());

            Map<UUID, Reputation> reps = villager.getReputations();

            UUID bestTradeUUID = event.getPlayer().getUniqueId();
            int bestMajorPositive = -1;
            int bestTrading = -1;
            boolean majorPositiveExists = false;

            if (!reps.isEmpty()) {
                for (Map.Entry<UUID, Reputation> rep : reps.entrySet()) {
                    if (rep.getValue().hasReputationSet(ReputationType.TRADING)) {
                        PaperPluginLogger.getLogger(plugin.getName()).info("Reputation TRADING");
                        if (rep.getValue().getReputation(ReputationType.TRADING) > bestTrading && !majorPositiveExists) {
                            bestTrading = rep.getValue().getReputation(ReputationType.TRADING);
                            bestTradeUUID = rep.getKey();
                        }
                    }
                    if (rep.getValue().hasReputationSet(ReputationType.MAJOR_POSITIVE) || rep.getValue().hasReputationSet(ReputationType.MINOR_POSITIVE)) {
                        PaperPluginLogger.getLogger(plugin.getName()).info("Reputation MAJOR_POSITIVE");

                        if (rep.getValue().getReputation(ReputationType.MAJOR_POSITIVE) > bestMajorPositive) {
                            bestMajorPositive = rep.getValue().getReputation(ReputationType.MAJOR_POSITIVE);
                            bestTradeUUID = rep.getKey();
                        }
                    }
                    if (rep.getValue().hasReputationSet(ReputationType.MAJOR_NEGATIVE) || rep.getValue().hasReputationSet(ReputationType.MINOR_NEGATIVE)) {
                        PaperPluginLogger.getLogger(plugin.getName()).info("Reputation MAJOR_NEGATIVE");
                    }
                }
            } else {
                villagerReputationDebugData(villager, "null", -9999, "NONE");
                PaperPluginLogger.getLogger(plugin.getName()).info("Reputation NONE");
            }

            villagerReputationDebugData(villager, bestTradeUUID + " " + Bukkit.getOfflinePlayer(bestTradeUUID).getName(), villager.getReputation(bestTradeUUID).getReputation(ReputationType.TRADING), "TRADING");
            villagerReputationDebugData(villager, bestTradeUUID + " " + Bukkit.getOfflinePlayer(bestTradeUUID).getName(), villager.getReputation(bestTradeUUID).getReputation(ReputationType.MAJOR_POSITIVE), "MAJOR_POSITIVE");
            villagerReputationDebugData(villager, bestTradeUUID + " " + Bukkit.getOfflinePlayer(bestTradeUUID).getName(), villager.getReputation(bestTradeUUID).getReputation(ReputationType.MINOR_POSITIVE), "MINOR_POSITIVE");
            villagerReputationDebugData(villager, bestTradeUUID + " " + Bukkit.getOfflinePlayer(bestTradeUUID).getName(), villager.getReputation(bestTradeUUID).getReputation(ReputationType.MAJOR_NEGATIVE), "MAJOR_NEGATIVE");
            villagerReputationDebugData(villager, bestTradeUUID + " " + Bukkit.getOfflinePlayer(bestTradeUUID).getName(), villager.getReputation(bestTradeUUID).getReputation(ReputationType.MINOR_NEGATIVE), "MINOR_NEGATIVE");

            villager.setReputation(event.getPlayer().getUniqueId(), villager.getReputation(bestTradeUUID));
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
            PaperPluginLogger.getLogger(plugin.getName()).severe("Failed to save " + plugin.getName() + " config file");
        }
    }
}
