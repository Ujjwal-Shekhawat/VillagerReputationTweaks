package com.kami;

import com.kami.commands.ChangeReputationMode;
import com.kami.commands.CommandTabCompleter;
import com.kami.events.PlayerVillagerEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerReputationTweaks extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerVillagerEvents(this), this);
        getCommand("trademode").setExecutor(new ChangeReputationMode(this));
        getCommand("trademode").setTabCompleter(new CommandTabCompleter());
    }
}