package com.kami;

import com.kami.events.PlayerVillagerEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerReputationTweaks extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerVillagerEvents(this), this);
    }
}