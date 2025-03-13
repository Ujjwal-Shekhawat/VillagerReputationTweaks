package com.kami.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            Optional<String> requiredPermission = Optional.ofNullable(command.getPermission());
            boolean higherPrivilege = player.isOp() || requiredPermission.map(player::hasPermission).orElse(false);

            List<String> completions = switch (args.length) {
                case 1 -> getFirstArgs(args, higherPrivilege);
                case 2 -> getSecondArgs(args, higherPrivilege);
                case 3 -> getThirdArgs(args, higherPrivilege);
                default -> List.of();
            };
            return completions;
        }

        return List.of();
    }

    public List<String> getFirstArgs(String[] args, boolean higherPrivilege) {
        List<String> arguments = new java.util.ArrayList<>(List.of("get"));
        if (higherPrivilege) {
            arguments.addAll(List.of("set", "reload", "toggle", "admin"));
        }
        return arguments.stream().filter(e -> e.startsWith(args[0].toLowerCase())).toList();
    }

    public List<String> getSecondArgs(String[] args, boolean higherPrivilege) {
        if (args[0].equals("set") && higherPrivilege) {
            return Stream.of("best-trades", "worst-trades", "shared-trades", "one-time-trades").filter(e -> e.startsWith(args[1].toLowerCase())).toList();
        }
        if (args[0].equals("admin") && higherPrivilege) {
            return Stream.of("add", "remove").filter(e -> e.startsWith(args[1].toLowerCase())).toList();
        }

        return List.of();
    }

    public List<String> getThirdArgs(String[] args, boolean higherPrivilege) {
        if (higherPrivilege && args[0].equals("admin") && (args[1].equals("add") | args[1].equals("remove"))) {
            return Bukkit.getOnlinePlayers().stream().map(e -> e.getName()).filter(e -> e.startsWith(args[2])).toList();
        }
        return List.of();
    }
}
