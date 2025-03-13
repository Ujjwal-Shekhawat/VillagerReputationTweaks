# Villager Reputation Manager

## Description:

Villager Reputation Manager is a PaperMC plugin that gives server owners control over how villager trading reputations
affect players. It allows configuring whether reputation changes, like curing a villager, apply only to the curing
player or benefit the entire server. With customizable trade behavior modes, server admins can enable options such as
best or worst trades for all players, shared reputation-based trading, and one-time trade bonuses after curing a
villager.

### Features:

- Tracks and logs villager reputation per player, including TRADING, MAJOR_POSITIVE, MINOR_POSITIVE, MAJOR_NEGATIVE, and
  MINOR_NEGATIVE standings.
- Supports customizable trading behavior, such as:
    - Best trades for all players (**Default**)
    - Worst trades for all players
    - Shared reputation-based trading (good/bad actions of a single player effects the trades for all players)
    - One-time bonuses after curing a villager (gives the villager curing bonus to all players, after that each player
      has their own reputation to keep)
    - Logs reputation data to a debug file for analysis. (**Incomplete**, planned for future)

##### Planned support for Bukkit, Spigot, and Folia.

## Planned Enhancements:

- [ ] Custom Permissions: Enable server admins to manage who can switch between different trade modes (Will require
  permission management plugins).
- [ ] Expanded Compatibility: Ensure support for Bukkit, Spigot, and Folia.
- [ ] Add functions for effortless switching between trade modes.
- [ ] Restrict the best trades per team (Not sure about this yet though)