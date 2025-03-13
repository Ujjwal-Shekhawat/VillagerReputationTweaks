package com.kami.events;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import com.kami.config.VillagerReputationTweaksConfig;
import com.kami.utils.ConfigEnumTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO:
 * Write a logger class and if willing provide a config options to print/save logs
 * refactor commands and implement config change via commands
 * Release for bukkit purpur spigot folia
 **/

public class PlayerVillagerEvents implements Listener {
    private final JavaPlugin plugin;
    private final VillagerReputationTweaksConfig villagerReputationTweaksConfig;

    public PlayerVillagerEvents(JavaPlugin plugin) {
        this.plugin = plugin;
        this.villagerReputationTweaksConfig = VillagerReputationTweaksConfig.getPluginConfig(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
    }

    @EventHandler
    public void onPlayerInteractWithVillager(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Villager villager && villagerReputationTweaksConfig.isEnabled()) {

            Player player = event.getPlayer();

            Map<UUID, Reputation> reps = new HashMap<>(villager.getReputations());

            ConfigEnumTypes tradeMode = villagerReputationTweaksConfig.getTradeMode();

            Optional<Map.Entry<UUID, Reputation>> applyTrades;

            switch (tradeMode) {
                case BEST_TRADES -> {
                    // Fix for a bug which caused one bad reputation to be applied for all interacting players
                    // Scenario: Villager has no trades so reps is empty when a player hit the villager the only rep is negative one, but a player other than that should have a zero rep not negative for best rep considering that
                    // Test this with another player thanks
                    if (!reps.containsKey(player.getUniqueId())) {
                        reps.put(player.getUniqueId(), new Reputation());
                    }
                    applyTrades = getBestTrades(reps);
                }
                case SHARED_TRADES -> {
                    Reputation averageReputation = getAverageReputation(reps);
                    Arrays.stream(Bukkit.getOfflinePlayers()).forEach(p -> villager.setReputation(p.getUniqueId(), averageReputation));
                    return;
                }
                case ONE_TIME_TRADES -> {
                    // TODO: Think about new joiners (Probably use PDC for this mode)
                    // Debatable for now the benefits are only for the players that were there before and at the time villager was cured new players joining after the villager has been cured wont be getting any reputations
                    return;
                }
                case WORST_TRADES -> {
                    // Fix for a bug which caused one bad reputation to be applied for all interacting players
                    // Scenario: Villager has no trades so reps is empty when a player hit the villager the only rep is negative one, but a player other than that should have a zero rep not negative for best rep considering that
                    // Test this with another player thanks
                    // debatable should this be the case here too, thanks
                    if (!reps.containsKey(player.getUniqueId())) {
                        reps.put(player.getUniqueId(), new Reputation());
                    }
                    applyTrades = getWorstTrades(reps);
                }
                case null, default -> {
                    return;
                }
            }

            applyTrades.ifPresentOrElse(
                    entry -> {
                        if (player.getUniqueId() == entry.getKey()) {
                            return;
                        }
                        villager.setReputation(player.getUniqueId(), entry.getValue());
                    }, () -> {
                    }
            );


        }
    }

    @EventHandler
    public void villagerCuringEvent(EntityTransformEvent event) {
        if (event.getEntity() instanceof ZombieVillager && event.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
            Villager villager = (Villager) event.getTransformedEntity();

            OfflinePlayer curingPlayer = ((ZombieVillager) event.getEntity()).getConversionPlayer();

            if (curingPlayer != null) {
                Reputation curingPlayerReputation = villager.getReputation(curingPlayer.getUniqueId());
                Bukkit.getOnlinePlayers().forEach(p -> villager.setReputation(p.getUniqueId(), curingPlayerReputation));

                PersistentDataContainer villagerPDC = villager.getPersistentDataContainer();
                NamespacedKey oneTimeKey = NamespacedKey.fromString("beneficiaries", plugin);

                String pdcValue = serializeVillagerPDCData(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getUniqueId).toList(), curingPlayerReputation);

                if (oneTimeKey != null) {
                    villagerPDC.set(oneTimeKey, PersistentDataType.STRING, pdcValue);
                } else {
                    plugin.getLogger().severe("Namespace key init failed");
                }
            } else {
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("A villager was cured without a curer, interesting").color(NamedTextColor.GOLD)));
            }
        }
    }

    private Optional<Map.Entry<UUID, Reputation>> getBestTrades(Map<UUID, Reputation> reps) {
        // Formula reference from https://minecraft.fandom.com/wiki/Villager#Gossiping
        Optional<Map.Entry<UUID, Reputation>> bestTradeDetails = reps.entrySet().stream()
                .filter(e -> Bukkit.getOfflinePlayer(e.getKey()).hasPlayedBefore())
                .max(Comparator.<Map.Entry<UUID, Reputation>>comparingInt(e -> {
                    Reputation rep = e.getValue();
                    return (5 * rep.getReputation(ReputationType.MAJOR_POSITIVE))
                            + (rep.getReputation(ReputationType.MINOR_POSITIVE))
                            + (rep.getReputation(ReputationType.TRADING))
                            - (rep.getReputation(ReputationType.MINOR_NEGATIVE))
                            - (5 * rep.getReputation(ReputationType.MAJOR_NEGATIVE));
                }));

        return bestTradeDetails;
    }

    private Optional<Map.Entry<UUID, Reputation>> getWorstTrades(Map<UUID, Reputation> reps) {
        // Formula reference from https://minecraft.fandom.com/wiki/Villager#Gossiping
        Optional<Map.Entry<UUID, Reputation>> worstTradeDetails = reps.entrySet().stream()
                .filter(e -> Bukkit.getOfflinePlayer(e.getKey()).hasPlayedBefore())
                .min(Comparator.<Map.Entry<UUID, Reputation>>comparingInt(e -> {
                    Reputation rep = e.getValue();
                    return (5 * rep.getReputation(ReputationType.MAJOR_POSITIVE))
                            + (rep.getReputation(ReputationType.MINOR_POSITIVE))
                            + (rep.getReputation(ReputationType.TRADING))
                            - (rep.getReputation(ReputationType.MINOR_NEGATIVE))
                            - (5 * rep.getReputation(ReputationType.MAJOR_NEGATIVE));
                }));

        return worstTradeDetails;
    }

    private Reputation getAverageReputation(Map<UUID, Reputation> reps) {
        Reputation averageReputation = new Reputation();

        Arrays.stream(ReputationType.values()).forEach(repType -> {
            double avgValue = reps.entrySet().stream()
                    .filter(e -> Bukkit.getOfflinePlayer(e.getKey()).hasPlayedBefore())
                    .filter(e -> Arrays.stream(ReputationType.values()).map(f -> e.getValue().getReputation(f)).reduce(Integer::sum).orElse(0) != 0)
                    .collect(Collectors.averagingInt(rep -> rep.getValue().getReputation(repType))); // Compute average
            averageReputation.setReputation(repType, (int) avgValue); // Store rounded average in the new Reputation object
        });

        return averageReputation;
    }

    private String serializeVillagerPDCData(List<UUID> uuids, Reputation reputation) {
        StringBuilder stringBuilder = new StringBuilder();

        String reducedUUID = uuids.stream().map(UUID::toString).reduce((x, y) -> x + ":" + y).orElse("null");
        stringBuilder.append(reducedUUID);
        stringBuilder.append("|");

        String reputationString = Arrays.stream(ReputationType.values()).map(e -> e.toString() + "." + reputation.getReputation(e)).reduce((x, y) -> x + ":" + y).orElse("null");
        stringBuilder.append(reputationString);

        return stringBuilder.toString();
    }

    private Map.Entry<List<UUID>, Reputation> deserializeVillagerPDCData(String pdcDataString) {
        List<UUID> uuids = Arrays.stream(pdcDataString.split("\\|")[0].split(":")).map(UUID::fromString).toList();
        Reputation newReputation = new Reputation();
        Arrays.stream(pdcDataString.split("\\|")[1].split(":")).forEach(e -> {
            String[] parts = e.split("\\.");
            if (parts.length == 2) {
                newReputation.setReputation(
                        ReputationType.valueOf(parts[0]),
                        Integer.parseInt(parts[1])
                );
            }
        });
        return new AbstractMap.SimpleEntry<>(uuids, newReputation);
    }
}
