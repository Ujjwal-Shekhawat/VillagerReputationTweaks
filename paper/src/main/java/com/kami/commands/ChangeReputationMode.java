package com.kami.commands;

import com.kami.config.VillagerReputationTweaksConfig;
import com.kami.utils.ConfigEnumTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ChangeReputationMode implements CommandExecutor {

    private final VillagerReputationTweaksConfig villagerReputationTweaksConfig;
    private final Plugin plugin;
    private final String permissionName = "kami.sama";

    public ChangeReputationMode(Plugin plugin) {
        this.plugin = plugin;
        this.villagerReputationTweaksConfig = VillagerReputationTweaksConfig.getPluginConfig(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Setup the command. I really hate the plugin.yml method
        command.setDescription("Gets the trademode currently being used on the server, thanks");
        command.setUsage("/trademode get | set <best-trades|worst-trades|shared-trades|one-time-trades> | reload | toggle | admin <add|remove> <player_name>");
        command.setPermission(permissionName);

        if (sender instanceof Player player) {
            boolean higherPrivilege = player.isOp() || player.hasPermission(permissionName);
            if (!higherPrivilege) {
                player.sendMessage(Component.text("You do not have permission to use this command").color(NamedTextColor.RED));
                return true;
            }

            if (args.length == 0) {
                return false;
            }

            switch (args[0]) {
                case "get" -> handleGet(player);
                case "set" -> handleSet(args, player, command);
                case "reload" -> handleReload(player);
                case "toggle" -> handleToggle(player);
                case "admin" -> handleAdmin(args, player, command);
                default ->
                        player.sendMessage(Component.text("Incorrect command usage.\nUsage: " + command.getUsage()).color(NamedTextColor.RED));
            }

            return true;
        }

        return false;
    }

    private void handleGet(Player player) {
        ConfigEnumTypes tradeMode = villagerReputationTweaksConfig.getTradeMode();
        boolean isEnabled = villagerReputationTweaksConfig.isEnabled();
        if (isEnabled) {
            player.sendMessage(Component.text("Reputation mode: " + tradeMode).color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Plugin is disabled.\nrun /trademode toggle to enable it.").color(NamedTextColor.GREEN));
        }
    }

    private void handleSet(String[] args, Player player, Command command) {
        switch (args[1]) {
            case "best-trades" -> {
                villagerReputationTweaksConfig.setTradeMode(ConfigEnumTypes.BEST_TRADES);
                player.sendMessage(Component.text("Set the server tade mode to: " + villagerReputationTweaksConfig.getTradeMode()).color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
            }
            case "worst-trades" -> {
                villagerReputationTweaksConfig.setTradeMode(ConfigEnumTypes.WORST_TRADES);
                player.sendMessage(Component.text("Set the server tade mode to: " + villagerReputationTweaksConfig.getTradeMode()).color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
            }
            case "shared-trades" -> {
                villagerReputationTweaksConfig.setTradeMode(ConfigEnumTypes.SHARED_TRADES);
                player.sendMessage(Component.text("Set the server tade mode to: " + villagerReputationTweaksConfig.getTradeMode()).color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
            }
            case "one-time-trades" -> {
                villagerReputationTweaksConfig.setTradeMode(ConfigEnumTypes.ONE_TIME_TRADES);
                player.sendMessage(Component.text("Set the server tade mode to: " + villagerReputationTweaksConfig.getTradeMode()).color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
            }
            default ->
                    player.sendMessage(Component.text("Incorrect command usage.\nUsage: " + command.getUsage()).color(NamedTextColor.RED));
        }
    }

    private void handleReload(Player player) {
        player.sendMessage(Component.text("Reloaded config from file").color(NamedTextColor.YELLOW));
        villagerReputationTweaksConfig.reloadConfig();
    }

    private void handleToggle(Player player) {
        villagerReputationTweaksConfig.setEnabled(!villagerReputationTweaksConfig.isEnabled());
        player.sendMessage(Component.text("Toggled: " + villagerReputationTweaksConfig.isEnabled()).color(NamedTextColor.GREEN));
    }

    private void handleAdmin(String[] args, Player player, Command command) {
        if (!(args.length == 3 && (args[1].equals("add") || args[1].equals("remove")))) {
            player.sendMessage(Component.text("Incorrect command usage.\nUsage: " + command.getUsage()).color(NamedTextColor.RED));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[2]);
        if (targetPlayer == null) {
            player.sendMessage(Component.text("Player not found or not online").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
            return;
        }

        boolean adding = args[1].equals("add");

        if (adding) {
            targetPlayer.addAttachment(plugin, permissionName, true);
            player.sendMessage(Component.text("Added player to admin list").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
        } else {
            targetPlayer.getEffectivePermissions().stream().filter(e -> e.getPermission().equals(permissionName)).findFirst().ifPresent(p -> targetPlayer.removeAttachment(p.getAttachment()));
            player.sendMessage(Component.text("Removed player from admin list").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
        }
    }
}
