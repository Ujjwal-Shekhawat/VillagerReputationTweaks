package com.kami.events;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.entity.npc.EntityVillager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

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
            logVillagerNBT(villager);
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

    private void logVillagerNBT(Villager villager) {
        try {
            // Convert Bukkit Villager to NMS Villager
            CraftVillager craftVillager = (CraftVillager) villager;
            EntityVillager nmsVillager = (EntityVillager) craftVillager.getHandle();

            // Create NBT tag compound
            NBTTagCompound nbtTag = new NBTTagCompound();
            nmsVillager.saveWithoutId(nbtTag, true); // Save villager NBT data

            // Log NBT Data
            plugin.getLogger().log(Level.INFO, "Villager NBT Data: " + nbtTag);

            Set<String> whatsThis = nbtTag.e();

            for (String s : whatsThis) {
                if (s.equalsIgnoreCase("Gossips")) {
                    plugin.getLogger().info(s);

                    // This printed out the compund value
                    String somethingFinally = nbtTag.c(s).u_();


                    plugin.getLogger().info(somethingFinally);

                    // This gets type
                    plugin.getLogger().info(nbtTag.c(s).c().a());
                    // This gets alternate type?
                    plugin.getLogger().info(nbtTag.c(s).c().b());

                    plugin.getLogger().info(nbtTag.c(s).getClass().descriptorString());

                    plugin.getLogger().info(nbtTag.p(s).getClass().descriptorString());

                    // Inferred this cast from above two logs
                    NBTTagList gossipList = (NBTTagList) nbtTag.c(s);

                    plugin.getLogger().info("Logging NbtTagList" + gossipList.size());
                    for (int i = 0; i < gossipList.size(); i++) {
                        NBTTagCompound compound = gossipList.a(i);
                        Set<String> gossipData = compound.e();
                        for (String gd : gossipData) {
                            plugin.getLogger().info(gd + ":" + compound.c(gd).u_());
                            if (compound.c(gd) instanceof NBTTagInt) {
                                plugin.getLogger().info("Instance of NBTTagInt");
                                compound.a(gd, 50);
                                plugin.getLogger().info("Tried to modify the value here:");
                                plugin.getLogger().info(gd + ":" + compound.c(gd).u_());
                            } else {
                                plugin.getLogger().info("Instance of " + compound.c(gd).getClass().descriptorString());
                            }
                        }
                    }


                }
            }

            plugin.getLogger().info("Trying to save NBTData now");
            nmsVillager.a(nbtTag);

            NBTTagCompound newNbtTag = new NBTTagCompound();
            nmsVillager.saveWithoutId(newNbtTag, true); // Save villager NBT data

            plugin.getLogger().log(Level.INFO, "Villager New NBT Data: " + newNbtTag);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get Villager NBT Data!", e);
        }
    }
}
